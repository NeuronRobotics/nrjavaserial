package gnu.io.factory;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

public class DefaultSerialPortFactory implements SerialPortFactory {

	private SerialPortRegistry portRegistry;
	
	public DefaultSerialPortFactory() {
		this.portRegistry = new SerialPortRegistry();
	}
	
	/* (non-Javadoc)
	 * @see gnu.io.factory.SerialPortFactory#createSerialPort(java.lang.String)
	 */
	@Override
	public SerialPort createSerialPort(String portName) throws PortInUseException, NoSuchPortException, UnsupportedCommOperationException{
		SerialPortCreator portCreator = this.portRegistry.getPortCreatorForPortName(portName);
		if(portCreator != null) {
			return portCreator.createPort(portName);
		}
		throw new NoSuchPortException(portName + " can not be opened.");
	}
	
}
