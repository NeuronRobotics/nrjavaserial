package gnu.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NRSerialPort {
	private RXTXPort serial;
	private String port=null;
	private boolean connected=false;
	private int baud = 115200;
	/**
	 * Class Constructor for a NRSerialPort with a given port and baudrate.
	 * 
	 * @param port the port to connect to (i.e. COM6 or /dev/ttyUSB0)
	 * @param baud the baudrate to use (i.e. 9600 or 115200)
	 */
	public NRSerialPort(String port, int baud) {
		setPort(port);
		setBaud(baud);
	}
	public boolean connect() {
		if(isConnected()) {
			System.err.println(port + " is already connected.");
			return true;
		}
		
		try 
		{
			RXTXPort comm = null;
			CommPortIdentifier ident = null;
			if((System.getProperty("os.name").toLowerCase().indexOf("linux")!=-1)){
				if (port.contains("rfcomm")||port.contains("ttyUSB") ||port.contains("ttyS")|| port.contains("ACM") || port.contains("Neuron_Robotics")||port.contains("NR")||port.contains("FTDI")||port.contains("ftdi")){
					System.setProperty("gnu.io.rxtx.SerialPorts", port);
				}
			}
			
			ident = CommPortIdentifier.getPortIdentifier(port);

			try{
				comm = ident.open("NRSerialPort", 2000);
			}catch (PortInUseException e) {
				System.err.println("This is a bug, passed the ownership test above: " + e.getMessage());
				return false;
			}
			
			if ( !(comm instanceof RXTXPort) ) {
				throw new UnsupportedCommOperationException("Non-serial connections are unsupported.");
			}
			
			serial = (RXTXPort) comm;
			serial.enableReceiveTimeout(100);
			serial.setSerialPortParams(getBaud(), SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);				
			setConnected(true);
		}catch(NativeResourceException e){
			throw new NativeResourceException(e.getMessage());
        }catch (Exception e) {
			System.err.println("Failed to connect on port: "+port+" exception: ");
			e.printStackTrace();
			setConnected(false);
		}
		
		if(isConnected()) {
			serial.notifyOnDataAvailable(true);
		}
		return isConnected();	
	}
	public InputStream getInputStream()
	{
		return serial.getInputStream();
	}
	public OutputStream getOutputStream()
	{
		return serial.getOutputStream();
	}
	/**
	 * Set the port to use (i.e. COM6 or /dev/ttyUSB0)
	 * 
	 * @param port the serial port to use
	 */
	private void setPort(String port) {
		this.port = port;
	}
	
	
	public void disconnect() {
		try{
			try{
				getInputStream().close();
				getOutputStream().close();
				serial.close();
			}catch(Exception e){
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			serial = null;
			setConnected(false);
		} catch(UnsatisfiedLinkError e) {
			throw new NativeResourceException(e.getMessage());
        }
	}
	public static Set<String> getAvailableSerialPorts() {
       Set<String> available;
        try{
        	RXTXCommDriver d = new RXTXCommDriver();
        	available=d.getPortIdentifiers();
        }catch( UnsatisfiedLinkError e){
        	e.printStackTrace();
        	throw new NativeResourceException(e.getMessage());
        }  
        return available;
    }
	
	
	public boolean isConnected(){
		return connected;
	}
	public void setConnected(boolean connected) {
		if(this.connected == connected)
			return;
		this.connected = connected;
	}
	public void setBaud(int baud) {
		switch(baud){
		case   2400:
		case   4800:
		case   9600:
		case  14400:
		case  19200:
		case  28800:
		case  38400:
		case  57600:
		case  76800:
		case 115200:
		case 230400:
			this.baud = baud;
			return;
		default:
			throw new RuntimeException("Invalid baudrate! "+baud);
		}
	}
	public int getBaud() {
		return baud;
	}
	public void notifyOnDataAvailable(boolean b) {
		serial.notifyOnDataAvailable(b);
	}
}
