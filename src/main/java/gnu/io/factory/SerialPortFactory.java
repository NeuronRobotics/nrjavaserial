package gnu.io.factory;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

public interface SerialPortFactory {

	/**
	 * Creates a {@link SerialPort} instance out of the given <code>portName</code>.
	 * @param portName The port's name to parse out whether to create a serial connection or a remote (rfc2217) connection.
	 * @param expectedClass The {@link SerialPort} class that is expected to return.
	 * @return The newly created and opened SerialPort.
	 * @throws PortInUseException
	 * @throws NoSuchPortException
	 * @throws UnsupportedCommOperationException
	 */
	<T extends SerialPort> T createSerialPort(String portName, Class<T> expectedClass)
			throws PortInUseException, NoSuchPortException, UnsupportedCommOperationException;

	/**
	 * Creates a {@link SerialPort} instance out of the given <code>portName</code>.
	 * @param portName The port's name to parse out whether to create a serial connection or a remote (rfc2217) connection.
	 * @return The newly created and opened SerialPort.
	 * @throws PortInUseException
	 * @throws NoSuchPortException
	 * @throws UnsupportedCommOperationException
	 */
	SerialPort createSerialPort(String portName)
			throws PortInUseException, NoSuchPortException, UnsupportedCommOperationException;
}