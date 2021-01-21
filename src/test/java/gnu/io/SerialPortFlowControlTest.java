package gnu.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Test the ability of the {@link SerialPort} implementation to mediate data
 * exchange with flow control.
 * <p>
 * This test is nonspecific as to <em>which</em> implementation; it exercises
 * only the public interface of the Java Communications API. Ports are opened
 * by the {@link SerialPortExtension} test extension, presumably by way of
 * {@link CommPortIdentifier}.
 */
public class SerialPortFlowControlTest
{
	private static final Logger log = Logger.getLogger(SerialPortFlowControlTest.class.getName());

	private static final String WROTE_WITHOUT_CTS = "Port A wrote data even though it wasn't clear to send";
	private static final String MISSING_CTS_WRITE = "Port A didn't write buffered data after port B asserted RTS";

	private static final String ERRONEOUS_CTS = "Port A is still asserting RTS even though its input buffer should be full";
	private static final String FILLED_INPUT_BUFFER = "Filled the input buffer of port A with %d bytes.";

	private static final String MISSING_INITIAL_WRITE = "Port A didn't write data before XOFF was sent";
	private static final String WROTE_WITH_XOFF = "Port A wrote data after XOFF was sent";
	private static final String MISSING_XON_WRITE = "Port A didn't write buffered data after being cleared to do so";

	private static final String MISSING_XOFF = "Port A never sent XOFF even though its input buffer should be full";

	/**
	 * How long to wait (in milliseconds) for changes to control line states
	 * on one port to affect the other port.
	 */
	private static final int STATE_WAIT = 50;
	/**
	 * How long to wait (in milliseconds) for data sent from one port to arrive
	 * at the other.
	 */
	private static final int TIMEOUT = 50;

	/** The XON character for software flow control. */
	private static final byte XON = 0x11;
	/** The XOFF character for software flow control. */
	private static final byte XOFF = 0x13;

	/**
	 * The baud rate at which to run the flow control read tests.
	 * <p>
	 * Because those tests require filling the port input buffer, this should
	 * be as fast as possible to minimize test runtime.
	 */
	private static final int READ_BAUD = 115_200;

	/**
	 * The size of the input buffer is unknown, and
	 * {@link CommPort#getInputBufferSize()} does not purport to report it
	 * accurately. To test port behaviour upon filling it, we'll try to send
	 * this much data, and hope that we hit the limit.
	 */
	private static final int INPUT_BUFFER_MAX = 128 * 1024;
	/**
	 * Write in chunks of this size when attempting to hit the input buffer
	 * limit so that we can return early after hitting it.
	 */
	private static final int INPUT_BUFFER_CHUNK = 4 * 1024;

	@RegisterExtension
	SerialPortExtension ports = new SerialPortExtension();

	/**
	 * Test that hardware flow control (aka RTS/CTS) correctly restricts
	 * writing.
	 * <p>
	 * This test works by enabling hardware flow control on one port while
	 * leaving it disabled on the other. The control lines of the second port
	 * can then be manually toggled as necessary to verify flow control
	 * behaviour on the first port.
	 *
	 * @throws UnsupportedCommOperationException if the flow control mode is
	 *                                           unsupported by the driver
	 * @throws InterruptedException              if the test is interrupted
	 *                                           while waiting for serial port
	 *                                           activity
	 * @throws IOException                       if an error occurs while
	 *                                           writing to or reading from one
	 *                                           of the ports
	 */
	@Test
	void testHardwareFlowControlWrite() throws UnsupportedCommOperationException, InterruptedException, IOException
	{
		/* On Windows, RTS is off by default when opening the port. On other
		 * platforms, it's on. We'll explicitly turn it off for consistency. */
		this.ports.b.setRTS(false);

		this.ports.a.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);

		this.ports.b.enableReceiveTimeout(SerialPortFlowControlTest.TIMEOUT);

		try (OutputStream out = this.ports.a.getOutputStream();
				InputStream in = this.ports.b.getInputStream())
		{
			/* Because we haven't enabled flow control for port B, port A should be
			 * waiting to send. */
			assertFalse(this.ports.a.isCTS());

			out.write(0x00);
			assertEquals(0, in.available(), SerialPortFlowControlTest.WROTE_WITHOUT_CTS);

			this.ports.b.setRTS(true);
			Thread.sleep(SerialPortFlowControlTest.STATE_WAIT);

			/* Port A should send once port B unblocks it. */
			assertTrue(this.ports.a.isCTS());
			assertNotEquals(-1, in.read(), SerialPortFlowControlTest.MISSING_CTS_WRITE);
		}
	}

	/**
	 * Test that hardware flow control (aka RTS/CTS) is correctly asserted when
	 * receiving data.
	 * <p>
	 * This test works by enabling hardware flow control on one port while
	 * leaving it disabled on the other. The flow control behaviour of the
	 * first port can then be verified by observing its control lines from the
	 * second port.
	 *
	 * @throws UnsupportedCommOperationException if the flow control mode is
	 *                                           unsupported by the driver
	 * @throws IOException                       if an error occurs while
	 *                                           writing to or reading from one
	 *                                           of the ports
	 */
	@Test
	void testHardwareFlowControlRead() throws UnsupportedCommOperationException, IOException
	{
		this.ports.a.setSerialPortParams(
				SerialPortFlowControlTest.READ_BAUD,
				SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);
		this.ports.b.setSerialPortParams(
				SerialPortFlowControlTest.READ_BAUD,
				SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);
		this.ports.a.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);

		byte[] buffer = new byte[SerialPortFlowControlTest.INPUT_BUFFER_CHUNK];

		try (OutputStream out = this.ports.b.getOutputStream())
		{
			assertTrue(this.ports.b.isCTS());

			/* Port A should deassert RTS once its input buffer is full. How
			 * big is its input buffer? `CommPort.getInputBufferSize()` can't
			 * be trusted to tell us. We'll have to just keep blasting data at
			 * it until it starts rejecting it. */
			int written;
			for (written = 0; written < SerialPortFlowControlTest.INPUT_BUFFER_MAX
					&& this.ports.b.isCTS(); written += buffer.length)
			{
				out.write(buffer);
			}

			assertFalse(this.ports.b.isCTS(), SerialPortFlowControlTest.ERRONEOUS_CTS);
			log.info(String.format(SerialPortFlowControlTest.FILLED_INPUT_BUFFER, written));
		}
	}

	/**
	 * Test that software flow control (aka XON/XOFF) correctly restricts
	 * writing.
	 * <p>
	 * This test works by enabling software flow control on one port while
	 * leaving it disabled on the other. The control characters can then be
	 * manually sent from the second port as necessary to verify flow control
	 * behaviour on the first port.
	 *
	 * @throws UnsupportedCommOperationException if the flow control mode is
	 *                                           unsupported by the driver
	 * @throws IOException                       if an error occurs while
	 *                                           writing to or reading from one
	 *                                           of the ports
	 */
	@Test
	void testSoftwareFlowControlWrite() throws UnsupportedCommOperationException, IOException
	{
		this.ports.a.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_IN | SerialPort.FLOWCONTROL_XONXOFF_OUT);

		this.ports.b.enableReceiveTimeout(SerialPortFlowControlTest.TIMEOUT);

		try (OutputStream outA = this.ports.a.getOutputStream();
				OutputStream outB = this.ports.a.getOutputStream();
				InputStream in = this.ports.b.getInputStream())
		{
			/* We should be able to write normally... */
			outA.write(0x00);
			assertNotEquals(-1, in.read(), SerialPortFlowControlTest.MISSING_INITIAL_WRITE);

			/* ...until XOFF is sent from the receiver... */
			outB.write(SerialPortFlowControlTest.XOFF);
			outA.write(0x00);
			assertEquals(0, in.available(), SerialPortFlowControlTest.WROTE_WITH_XOFF);

			/* ...and life should resume upon XON. */
			outB.write(SerialPortFlowControlTest.XON);
			assertNotEquals(-1, in.read(), SerialPortFlowControlTest.MISSING_XON_WRITE);
		}
	}

	/**
	 * Test that software flow control (aka XON/XOFF) control characters are
	 * generated when receiving data.
	 * <p>
	 * This test works by enabling software flow control on one port while
	 * leaving it disabled on the other. The generation of flow control
	 * characters by first port can then be verified by reading from the second
	 * port.
	 * <p>
	 * FIXME: On macOS (tested 10.15), I never received the XOFF even after
	 * passing multiple megabytes of data.
	 *
	 * @throws UnsupportedCommOperationException if the flow control mode is
	 *                                           unsupported by the driver
	 * @throws IOException                       if an error occurs while
	 *                                           writing to or reading from one
	 *                                           of the ports
	 */
	@Test
	@DisabledOnOs(OS.MAC)
	void testSoftwareFlowControlRead() throws UnsupportedCommOperationException, IOException
	{
		this.ports.a.setSerialPortParams(
				SerialPortFlowControlTest.READ_BAUD,
				SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);
		this.ports.b.setSerialPortParams(
				SerialPortFlowControlTest.READ_BAUD,
				SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);
		this.ports.a.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_IN | SerialPort.FLOWCONTROL_XONXOFF_OUT);

		byte[] buffer = new byte[SerialPortFlowControlTest.INPUT_BUFFER_CHUNK];

		try (OutputStream out = this.ports.b.getOutputStream();
				InputStream in = this.ports.b.getInputStream())
		{
			assertEquals(0, in.available());

			/* Port A should send XOFF once its input buffer is full. See
			 * `SerialPortFlowControlTest.testHardwareFlowControlRead()` for
			 * details. */
			int written;
			for (written = 0; written < SerialPortFlowControlTest.INPUT_BUFFER_MAX
					&& in.available() == 0; written += buffer.length)
			{
				out.write(buffer);
			}

			assertEquals(1, in.available(), SerialPortFlowControlTest.MISSING_XOFF);
			log.info(String.format(SerialPortFlowControlTest.FILLED_INPUT_BUFFER, written));
		}
	}
}
