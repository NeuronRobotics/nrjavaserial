package gnu.io;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Test the read/write functionality of the {@link SerialPort} implementation.
 * <p>
 * This test is nonspecific as to <em>which</em> implementation; it exercises
 * only the public interface of the Java Communications API. Ports are opened
 * by the {@link SerialPortExtension} test extension, presumably by way of
 * {@link CommPortIdentifier}.
 */
class SerialPortReadWriteTest
{
	private static final Logger log = Logger.getLogger(SerialPortReadWriteTest.class.getName());

	private static final String START = "Testing read/write via %s at %d baud...";
	private static final String FINISH = "Completed read/write via %s at %d baud in %.3fs.";

	private static final String NO_DATA_READ = "Didn't read any data at all – are you sure the ports are connected via "
			+ "a null cable?";
	private static final String NOT_ENOUGH_READ = "Didn't read enough data. Maybe your operating system buffer isn't "
			+ "big enough.";
	private static final String DATA_MISMATCH = "Read the expected amount of data, but it doesn't match what was sent.";
	private static final String SLOW_YOUR_ROLL = "Block read/write at %d baud completed successfully, but much %s than "
			+ "expected. Maybe your serial ports only pretend to support this baud rate.";

	@RegisterExtension
	SerialPortExtension ports = new SerialPortExtension();

	/** How long we want each read/write test to take, in milliseconds. */
	private static final int TARGET_DURATION = 1_000;
	/**
	 * The inter-byte read timeout for the read/write test.
	 * <p>
	 * Note that the test as a whole can take longer to run than this timeout,
	 * because it only limits the maximum amount of read time elapsed between
	 * two successive byte reads.
	 */
	private static final int TIMEOUT = SerialPortReadWriteTest.TARGET_DURATION * 2;
	/**
	 * The maximum time a read/write test should take. Log a warning if it
	 * takes any longer.
	 * <p>
	 * The volume of data used in the block read/write tests corresponds to the
	 * baud rate being used, so the amount of time consumed should stay fairly
	 * close to one second. If it takes dramatically more or less time, then
	 * it's possible that some other baud rate is being used instead. This
	 * could otherwise go unnoticed if both test ports use the same hardware
	 * or driver, and misinterpret the setting similarly.
	 */
	private static final int LOW_SPEED_TRAP = 1250;
	/**
	 * The minimum time a read/write test should take. Log a warning if it
	 * takes any less.
	 *
	 * @see SerialPortReadWriteTest#LOW_SPEED_TRAP
	 */
	private static final int HIGH_SPEED_TRAP = 750;

	/** The baud rates at which to test reading/writing. */
	private static final int[] BAUDS = new int[] {
			150, 200, 300, 600,
			1_200, 2_400, 4_800, 9_600,
			19_200, 38_400, 57_600, 115_200
	};

	/**
	 * Low-speed baud rates.
	 * <p>
	 * FIXME: Defined on all supported platforms, but not actually functional
	 * on macOS.
	 */
	private static final int[] LOW_SPEED_BAUDS = new int[] {
			50, 75, 110, 134
	};

	/**
	 * Whether the high-speed baud rates should also be tested.
	 * <p>
	 * Even when supported by the driver and hardware, you may encounter errors
	 * at high baud rates due to anomalies in the physical connection.
	 * High-speed baud rates work best over short connections and communication
	 * may fail through no fault of the software – due to dodgy cabling, or
	 * connectors, or interference.
	 *
	 * @see SerialPortReadWriteTest#HIGH_SPEED_BAUDS
	 */
	private static final boolean INCLUDE_HIGH_SPEED_BAUDS = false;

	/**
	 * High-speed baud rates.
	 * <p>
	 * Common, but much less common than those found in
	 * {@link SerialPortReadWriteTest#BAUDS}; not tested by default.
	 */
	private static final int[] HIGH_SPEED_BAUDS = new int[] {
			230_400, 460_800, 921_600
	};

	/**
	 * Generates a set of tests to exchange blocks of data between two serial
	 * ports. This is intended to exercise the
	 * {@link OutputStream#write(byte[])} and {@link InputStream#read(byte[])}
	 * methods of the underlying streams on the {@link CommPort}.
	 * <p>
	 * <code>baud / 10</code> bytes are transferred; this should take about one
	 * second.
	 *
	 * @return a set of block read/write tests
	 */
	@TestFactory
	@Tag("slow")
	Stream<DynamicTest> testAllReadWriteBlocks()
	{
		return this
				.getTestBauds()
				.mapToObj(baud -> dynamicTest(String.format("testReadWrite(%d, BlockCopy)", baud),
						() -> this.testReadWrite(baud, baud / 10, new BlockCopy())));
	}

	/**
	 * Generates a set of tests to exchange individual bytes of data between
	 * two serial ports. This is intended to exercise the
	 * {@link OutputStream#write(int)} and {@link InputStream#read()} methods
	 * of the underlying streams on the {@link CommPort}.
	 * <p>
	 * 64B is transferred regardless of baud rate; this should take about one
	 * second.
	 *
	 * @return a set of byte-by-byte read/write tests
	 */
	@TestFactory
	@Tag("slow")
	Stream<DynamicTest> testAllReadWriteBytes()
	{
		return this
				.getTestBauds()
				.mapToObj(baud -> dynamicTest(String.format("testReadWrite(%d, ByteCopy)", baud),
						() -> this.testReadWrite(baud, 64, new ByteCopy())));
	}

	/**
	 * @return a stream of baud rates to test at
	 */
	private IntStream getTestBauds()
	{
		IntStream bauds;
		if (OS.MAC.isCurrentOs())
		{
			bauds = IntStream.empty();
		}
		else
		{
			bauds = Arrays.stream(LOW_SPEED_BAUDS);
		}
		bauds = IntStream.concat(
					bauds,
					Arrays.stream(SerialPortReadWriteTest.BAUDS));
		if (SerialPortReadWriteTest.INCLUDE_HIGH_SPEED_BAUDS)
		{
			bauds = IntStream.concat(
					bauds,
					Arrays.stream(SerialPortReadWriteTest.HIGH_SPEED_BAUDS));
		}
		return bauds;
	}

	/**
	 * Represents a strategy for copying data from one buffer to another via
	 * a pair of streams, which are internally connected to each other.
	 */
	@FunctionalInterface
	private static interface CopyFunction
	{
		/**
		 * Given a pair of streams which are connected to each other, copy as
		 * much of the data from the input buffer <code>writeBuffer</code> to
		 * the output buffer <code>readBuffer</code> as possible, and return
		 * the number of bytes transferred.
		 *
		 * @param writeBuffer data to be written to the output stream
		 * @param out         the write target
		 * @param readBuffer  data read from the input stream
		 * @param in          the read source
		 * @return the total number of bytes read
		 * @throws IOException
		 */
		int copy(byte[] writeBuffer, OutputStream out, byte[] readBuffer, InputStream in) throws IOException;
	}

	/**
	 * A block copier. The entire write buffer is written in a single pass, and
	 * reads consume as much from the input stream as possible, up to and
	 * including the entire read buffer.
	 */
	private static class BlockCopy implements CopyFunction
	{
		@Override
		public int copy(byte[] writeBuffer, OutputStream out, byte[] readBuffer, InputStream in) throws IOException
		{
			out.write(writeBuffer);
			int pos = 0;
			while (pos < writeBuffer.length)
			{
				int read = in.read(readBuffer, pos, readBuffer.length - pos);
				if (read <= 0)
				{
					break;
				}
				else
				{
					pos += read;
				}
			}
			return pos;
		}
	}

	/**
	 * A byte copier. Individual bytes are written and read one at a time.
	 */
	private static class ByteCopy implements CopyFunction
	{
		@Override
		public int copy(byte[] writeBuffer, OutputStream out, byte[] readBuffer, InputStream in) throws IOException
		{
			int pos = 0;
			for (; pos < writeBuffer.length; ++pos)
			{
				out.write(writeBuffer[pos]);
				int read = in.read();
				if (read < 0)
				{
					break;
				}
				else
				{
					readBuffer[pos] = (byte) read;
				}
			}
			return pos;
		}
	}

	/**
	 * Framework for testing the exchange of data between two serial ports.
	 * Actual reading/writing is performed by a callback.
	 * <p>
	 * Data is exchanged unidirectionally from port A to B. Both ports are
	 * configured to use the given baud rate at 8-N-1 with no flow control. The
	 * read and write buffers are allocated based on the size given by the
	 * <code>bufferSize</code> argument, and the write buffer is populated. The
	 * <code>copy</code> callback is invoked to perform the transfer; then the
	 * results are checked.
	 *
	 * @param baud       the baud rate to test at
	 * @param bufferSize the amount of data to transfer
	 * @param copy       the strategy to use for transferring the data
	 * @throws UnsupportedCommOperationException if the baud rate or receive
	 *                                           timeout is unsupported by one
	 *                                           of the ports
	 * @throws IOException                       if an error occurs while
	 *                                           writing to or reading from one
	 *                                           of the ports
	 */
	void testReadWrite(
			int baud,
			int bufferSize,
			CopyFunction copy)
			throws UnsupportedCommOperationException, IOException
	{
		this.ports.b.enableReceiveTimeout(SerialPortReadWriteTest.TIMEOUT);

		log.info(String.format(SerialPortReadWriteTest.START, copy.getClass().getSimpleName(), baud));
		this.ports.a.setSerialPortParams(
				baud,
				SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);
		this.ports.b.setSerialPortParams(
				baud,
				SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);

		byte[] writeBuffer = new byte[bufferSize];
		byte[] readBuffer = new byte[bufferSize];
		int pos = 0;

		SerialPortReadWriteTest.fillBuffer(writeBuffer);
		long start = System.currentTimeMillis();
		try (OutputStream out = this.ports.a.getOutputStream();
				InputStream in = this.ports.b.getInputStream())
		{
			pos = copy.copy(writeBuffer, out, readBuffer, in);
		}
		long finish = System.currentTimeMillis();
		log.info(String.format(SerialPortReadWriteTest.FINISH, copy.getClass().getSimpleName(), baud,
				(finish - start) / 1000.0));

		assertNotEquals(0, pos, SerialPortReadWriteTest.NO_DATA_READ);
		assertEquals(writeBuffer.length, pos, SerialPortReadWriteTest.NOT_ENOUGH_READ);
		assertArrayEquals(writeBuffer, readBuffer, SerialPortReadWriteTest.DATA_MISMATCH);

		long elapsed = finish - start;
		if (elapsed < SerialPortReadWriteTest.HIGH_SPEED_TRAP)
		{
			log.warning(String.format(SerialPortReadWriteTest.SLOW_YOUR_ROLL, baud, "faster"));
		}
		else if (elapsed > SerialPortReadWriteTest.LOW_SPEED_TRAP)
		{
			log.warning(String.format(SerialPortReadWriteTest.SLOW_YOUR_ROLL, baud, "slower"));
		}
	}

	/**
	 * Fill a buffer with a repeating byte sequence of 0x00–0xFF.
	 *
	 * @param buffer the buffer to fill
	 */
	static void fillBuffer(byte[] buffer)
	{
		for (int i = 0; i < buffer.length; ++i)
		{
			buffer[i] = (byte) (i % 0xFF);
		}
	}
}
