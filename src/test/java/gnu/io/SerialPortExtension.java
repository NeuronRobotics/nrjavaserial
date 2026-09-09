package gnu.io;

import java.io.Closeable;
import java.io.IOException;
import java.util.logging.Logger;

import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Provisioning of serial ports for functionality testing.
 * <p>
 * This test extension automates the acquisition and resetting of two serial
 * ports for functional testing. Tests which use these ports assume that
 * they're connected to each other with a null modem cable so that data can be
 * exchanged between them. Then, so that this class will use them, set the
 * <code>NRJS_TEST_PORT_A</code> and <code>NRJS_TEST_PORT_B</code> environment
 * variables to the names of the ports prior to executing tests.
 * <p>
 * For example:
 *
 * <pre>
 * NRJS_TEST_PORT_A=/dev/ttyS0 NRJS_TEST_PORT_B=/dev/ttyS1 ./gradlew test
 * </pre>
 *
 * Or, on Windows:
 *
 * <pre>
 * set NRJS_TEST_PORT_A=COM1
 * set NRJS_TEST_PORT_B=COM2
 * gradlew.bat test
 * </pre>
 */
class SerialPortExtension implements Closeable, ExecutionCondition, BeforeEachCallback, AfterEachCallback
{
	private static final Logger log = Logger.getLogger(SerialPortExtension.class.getName());

	/** The environment variable holding the name of the first test port. */
	static final String A_PORT_ENV = "NRJS_TEST_PORT_A";
	/** The environment variable holding the name of the second test port. */
	static final String B_PORT_ENV = "NRJS_TEST_PORT_B";

	/**
	 * The owner name to pass to {@link CommPortIdentifier#open(String, int)}.
	 */
	private static final String PORT_OWNER = "NRJavaSerial - SerialPortExtension";
	/** The timeout to pass to {@link CommPortIdentifier#open(String, int)}. */
	private static final int OPEN_TIMEOUT = 1_000;

	/**
	 * When the test port configuration is sane, this is returned to indicate
	 * that tests relying on serial ports should be enabled.
	 */
	private static final ConditionEvaluationResult HAS_PORTS = ConditionEvaluationResult
			.enabled("Two serial ports are available, and could be opened.");
	/**
	 * When test ports aren't configured, this is returned to indicate that
	 * tests relying on serial ports should be disabled.
	 */
	private static final ConditionEvaluationResult MISSING_PORTS = ConditionEvaluationResult
			.disabled("The system does not have two serial ports, or they could not be opened.");

	/** Whether both test port identifiers have been populated. */
	final boolean hasIds;
	/**
	 * The first test port identifier.
	 * <p>
	 * Populated at construction by searching for ports matching the name given
	 * via the {@link SerialPortExtension#A_PORT_ENV} environment variable.
	 */
	final CommPortIdentifier aId;
	/**
	 * The second test port identifier.
	 * <p>
	 * Populated at construction by searching for ports matching the name given
	 * via the {@link SerialPortExtension#B_PORT_ENV} environment variable.
	 */
	final CommPortIdentifier bId;
	/**
	 * The first test port.
	 * <p>
	 * Ports are opened from their corresponding identifiers prior to each
	 * test, and are closed afterwards. This field will always be null outside
	 * of the context of a test.
	 */
	SerialPort a = null;
	/**
	 * The second test port.
	 * <p>
	 * Ports are opened from their corresponding identifiers prior to each
	 * test, and are closed afterwards. This field will always be null outside
	 * of the context of a test.
	 */
	SerialPort b = null;

	public SerialPortExtension()
	{
		String aName = null;
		String bName = null;
		try
		{
			aName = System.getenv(SerialPortExtension.A_PORT_ENV);
			bName = System.getenv(SerialPortExtension.B_PORT_ENV);
		}
		catch (SecurityException e)
		{
			log.severe("Failed a security check while accessing the environment variable containing the port name: "
					+ e.getMessage());
		}

		if (aName == null || bName == null)
		{
			String gradlew;
			String setter;
			String[] ports;
			if (OS.WINDOWS.isCurrentOs())
			{
				gradlew = "gradlew.bat";
				setter = "set";
				ports = new String[] { "COM1", "COM2" };
			}
			else if (OS.MAC.isCurrentOs())
			{
				gradlew = "./gradlew";
				setter = "export";
				ports = new String[] { "/dev/tty.usbserial-a", "/dev/tty.usbserial-b" };
			}
			else
			{
				gradlew = "./gradlew";
				setter = "export";
				ports = new String[] { "dev/ttyUSB0", "/dev/ttyUSB1" };
			}
			log.severe("The serial port functionality tests require the use of two ports. These should be connected to "
					+ "each other with a null modem cable. Then set the environment variables "
					+ SerialPortExtension.A_PORT_ENV + " and " + SerialPortExtension.B_PORT_ENV + " to the names of "
					+ "the ports, restart the Gradle daemon, and re-run the tests. For example:\n\n\t" + setter + " "
					+ SerialPortExtension.A_PORT_ENV + "=" + ports[0] + " " + SerialPortExtension.B_PORT_ENV + "="
					+ ports[1] + "\n\t" + gradlew + " --stop\n\t" + gradlew + " cleanTest test --no-build-cache "
					+ "--info");

			this.hasIds = false;
			this.aId = null;
			this.bId = null;
			return;
		}

		CommPortIdentifier aId;
		CommPortIdentifier bId;
		try
		{
			aId = CommPortIdentifier.getPortIdentifier(aName);
			bId = CommPortIdentifier.getPortIdentifier(bName);
		}
		catch (NoSuchPortException e)
		{
			log.severe("No such port: " + e.getMessage());

			this.hasIds = false;
			this.aId = null;
			this.bId = null;
			return;
		}

		log.info("Will attempt to use ports " + aId.getName() + " and " + bId.getName() + " for serial port "
				+ "functionality tests.");
		this.hasIds = true;
		this.aId = aId;
		this.bId = bId;
	}

	@Override
	public void close()
	{
		if (this.a != null)
		{
			a.close();
			a = null;
		}
		if (this.b != null)
		{
			b.close();
			b = null;
		}
	}

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context)
	{
		return this.hasIds
				? SerialPortExtension.HAS_PORTS
				: SerialPortExtension.MISSING_PORTS;
	}

	@Override
	public void beforeEach(ExtensionContext context) throws PortInUseException, NoSuchPortException
	{
		try
		{
			this.a = this.aId.open(SerialPortExtension.PORT_OWNER, SerialPortExtension.OPEN_TIMEOUT);
			this.b = this.bId.open(SerialPortExtension.PORT_OWNER, SerialPortExtension.OPEN_TIMEOUT);
		}
		catch (PortInUseException e)
		{
			log.severe("Port is in use: " + e.getMessage());
			this.close();
			throw e;
		}
	}

	@Override
	public void afterEach(ExtensionContext context) throws IOException
	{
		this.close();
	}
}
