package gnu.io.factory;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

public class SerialPortRegistry {

	private Collection<SerialPortCreator> portCreators;
	
	public SerialPortRegistry() {
		this.portCreators = new TreeSet<SerialPortCreator>(new Comparator<SerialPortCreator>() {

			@Override
			public int compare(SerialPortCreator o1, SerialPortCreator o2) {
				if(o1.getProtocol().equals(SerialPortCreator.LOCAL)) {
					return 1;
				}
				if(o2.getProtocol().equals(SerialPortCreator.LOCAL)) {
					return -1;
				}
				return o1.getProtocol().compareTo(o2.getProtocol());
			}
		});
		
		registerSerialPortCreator(new RxTxPortCreator());
		registerSerialPortCreator(new RFC2217PortCreator());
	}
	
	/**
	 * Registers a {@link SerialPortCreator}.
	 * @param creator
	 */
	public void registerSerialPortCreator(SerialPortCreator creator) {
		this.portCreators.add(creator);
	}
	
	/**
	 * Gets the best applicable {@link SerialPortCreator} for the given <code>portName</code>
	 * @param portName The port's name.
	 * @return A found {@link SerialPortCreator} or null if none could be found.
	 */
	public SerialPortCreator getPortCreatorForPortName(String portName) {
		for(SerialPortCreator creator : this.portCreators) {
			try {
				if(creator.isApplicable(portName))
					return creator;
			} catch(Exception e) {
				System.err.println("Error for SerialPortCreator#isApplicable: " + creator.getClass()+"; " + creator.getProtocol() +" -> " + e.getMessage());
			}
		}
		return null;
	}
}
