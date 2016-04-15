package gnu.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TooManyListenersException;

import gnu.io.factory.DefaultSerialPortFactory;
import gnu.io.factory.SerialPortFactory;

public class NRSerialPort
{

    private SerialPort serial;
    private String port = null;
    private boolean connected = false;
    private int baud = 115200;
    private SerialPortFactory serialPortFactory;

    /**
     * Class Constructor for a NRSerialPort with a given port and baudrate.
     * 
     * @param port the port to connect to (i.e. COM6 or /dev/ttyUSB0)
     * @param baud the baudrate to use (i.e. 9600 or 115200)
     */
    public NRSerialPort(String port, int baud)
    {
    	this(port, baud, new DefaultSerialPortFactory());
    }
    
    /**
     * Class Constructor for a NRSerialPort with a given port and baudrate.
     * The factory can be used to create the serial port.
     * 
     * @param port the port to connect to (i.e. COM6 or /dev/ttyUSB0)
     * @param baud the baudrate to use (i.e. 9600 or 115200)
     */
    public NRSerialPort(String port, int baud, SerialPortFactory factory)
    {
        setPort(port);
        setBaud(baud);
        this.serialPortFactory = factory;
    }


    public boolean connect()
    {
        if (isConnected())
        {
            System.err.println(port + " is already connected.");
            return true;
        }

        try
        {
        	serial = this.serialPortFactory.createSerialPort(port, SerialPort.class);
            serial.setSerialPortParams(getBaud(), SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            setConnected(true);
        }
        catch (NativeResourceException e)
        {
            throw new NativeResourceException(e.getMessage());
        }
        catch (Exception e)
        {
            System.err.println("Failed to connect on port: " + port + " exception: ");
            e.printStackTrace();
            setConnected(false);
        }

        if (isConnected())
        {
            serial.notifyOnDataAvailable(true);
        }
        return isConnected();
    }


    public InputStream getInputStream()
    {
        try {
			return serial.getInputStream();
		} catch (IOException e) {
			throw new RuntimeException("InputStream cannot be accessed", e);
		}
    }


    public OutputStream getOutputStream()
    {
        try {
			return serial.getOutputStream();
		} catch (IOException e) {
			throw new RuntimeException("OutputStream cannot be accessed", e);
		}
    }


    /**
     * Set the port to use (i.e. COM6 or /dev/ttyUSB0)
     * 
     * @param port the serial port to use
     */
    private void setPort(String port)
    {
        this.port = port;
    }


    public void disconnect()
    {
        try
        {
            try
            {
                getInputStream().close();
                getOutputStream().close();
                serial.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            serial = null;
            setConnected(false);
        }
        catch (UnsatisfiedLinkError e)
        {
            throw new NativeResourceException(e.getMessage());
        }
    }


    public static Set<String> getAvailableSerialPorts()
    {
        Set<String> available = new HashSet<String>();
        try
        {
            RXTXCommDriver d = new RXTXCommDriver();
            Set<String> av = d.getPortIdentifiers();
            ArrayList<String> strs = new ArrayList<String>();
            for (String s : av)
            {
                strs.add(0, s);
            }
            for (String s : strs)
            {
                available.add(s);
            }
        }
        catch (UnsatisfiedLinkError e)
        {
            e.printStackTrace();
            throw new NativeResourceException(e.getMessage());
        }

        return available;
    }


    public boolean isConnected()
    {
        return connected;
    }


    public void setConnected(boolean connected)
    {
        if (this.connected == connected)
            return;
        this.connected = connected;
    }


    public void setBaud(int baud)
    {
        switch (baud)
        {
            case 2400:
            case 4800:
            case 9600:
            case 14400:
            case 19200:
            case 28800:
            case 38400:
            case 57600:
            case 76800:
            case 115200:
            case 230400:
                this.baud = baud;
                return;
            default:
                throw new RuntimeException("Invalid baudrate! " + baud);
        }
    }


    public int getBaud()
    {
        return baud;
    }


    public void notifyOnDataAvailable(boolean b)
    {
        serial.notifyOnDataAvailable(b);
    }


    public void addEventListener(SerialPortEventListener lsnr) throws TooManyListenersException
    {
        serial.addEventListener(lsnr);
    }


    public void removeEventListener()
    {
        serial.removeEventListener();
    }


    /**
     * @deprecated please use {@link #getSerialPort()} as the return type can be another implementation.
     * @return
     */
    @Deprecated
    public RXTXPort getSerialPortInstance()
    {
        return (RXTXPort) serial;
    }
    /**
     * Gets the {@link SerialPort} instance.
     * This will return null until {@link #connect()} is successfully called.
     * @return The {@link SerialPort} instance or null.
     */
    public SerialPort getSerialPort() {
    	return serial;
    }
}
