/*-------------------------------------------------------------------------
|   RXTX License v 2.1 - LGPL v 2.1 + Linking Over Controlled Interface.
|   RXTX is a native interface to serial ports in java.
|   Copyright 1997-2007 by Trent Jarvi tjarvi@qbang.org and others who
|   actually wrote it.  See individual source files for more information.
|
|   A copy of the LGPL v 2.1 may be found at
|   http://www.gnu.org/licenses/lgpl.txt on March 4th 2007.  A copy is
|   here for your convenience.
|
|   This library is free software; you can redistribute it and/or
|   modify it under the terms of the GNU Lesser General Public
|   License as published by the Free Software Foundation; either
|   version 2.1 of the License, or (at your option) any later version.
|
|   This library is distributed in the hope that it will be useful,
|   but WITHOUT ANY WARRANTY; without even the implied warranty of
|   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
|   Lesser General Public License for more details.
|
|   An executable that contains no derivative of any portion of RXTX, but
|   is designed to work with RXTX by being dynamically linked with it,
|   is considered a "work that uses the Library" subject to the terms and
|   conditions of the GNU Lesser General Public License.
|
|   The following has been added to the RXTX License to remove
|   any confusion about linking to RXTX.   We want to allow in part what
|   section 5, paragraph 2 of the LGPL does not permit in the special
|   case of linking over a controlled interface.  The intent is to add a
|   Java Specification Request or standards body defined interface in the 
|   future as another exception but one is not currently available.
|
|   http://www.fsf.org/licenses/gpl-faq.html#LinkingOverControlledInterface
|
|   As a special exception, the copyright holders of RXTX give you
|   permission to link RXTX with independent modules that communicate with
|   RXTX solely through the Sun Microsytems CommAPI interface version 2,
|   regardless of the license terms of these independent modules, and to copy
|   and distribute the resulting combined work under terms of your choice,
|   provided that every copy of the combined work is accompanied by a complete
|   copy of the source code of RXTX (the version of RXTX used to produce the
|   combined work), being distributed under the terms of the GNU Lesser General
|   Public License plus this exception.  An independent module is a
|   module which is not derived from or based on RXTX.
|
|   Note that people who make modified versions of RXTX are not obligated
|   to grant this special exception for their modified versions; it is
|   their choice whether to do so.  The GNU Lesser General Public License
|   gives permission to release a modified version without this exception; this
|   exception also makes it possible to release a modified version which
|   carries forward this exception.
|
|   You should have received a copy of the GNU Lesser General Public
|   License along with this library; if not, write to the Free
|   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
|   All trademarks belong to their respective owners.
--------------------------------------------------------------------------*/
package gnu.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TooManyListenersException;

import gnu.io.factory.RxTxPortCreator;

public class NRSerialPort
{

    private RXTXPort serial;
    private String port = null;
    private boolean connected = false;
    private int baud = 115200;

    /**
     * Class Constructor for a NRSerialPort with a given port and baudrate.
     * 
     * @param port the port to connect to (i.e. COM6 or /dev/ttyUSB0)
     * @param baud the baudrate to use (i.e. 9600 or 115200)
     */
    public NRSerialPort(String port, int baud)
    {
    	setPort(port);
        setBaud(baud);
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
        	serial = new RxTxPortCreator().createPort(port);
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

        this.baud = baud;
        return;

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
     * Gets the {@link SerialPort} instance.
     * This will return null until {@link #connect()} is successfully called.
     * @return The {@link SerialPort} instance or null.
     */
    public RXTXPort getSerialPortInstance()
    {
        return serial;
    }

}
