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

import gnu.io.factory.RFC2217PortCreator;
import gnu.io.factory.RxTxPortCreator;
import gnu.io.rfc2217.TelnetSerialPort;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TooManyListenersException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NRSerialPort {
	private static final Logger log = LoggerFactory.getLogger(NRSerialPort.class);

	private SerialPort serial;
	private String port = null;
	private boolean connected = false;
	private int baud = 115200;
	private int parity = SerialPort.PARITY_NONE;
	private int dataBits = SerialPort.DATABITS_8;
	private int stopBits = SerialPort.STOPBITS_1;

	/**
	 * Class Constructor for a NRSerialPort with a given port and baudrate.
	 * 
	 * @param port
	 *            the port to connect to (i.e. COM6 or /dev/ttyUSB0)
	 */

	public NRSerialPort(String port) {
		setPort(port);
	}

	public NRSerialPort(String port, int baud) {
		setPort(port);
		setBaud(baud);
	}

	public NRSerialPort(String port, int baud, int parity) {
		setPort(port);
		setBaud(baud);
		setParity(parity);
	}

	public NRSerialPort(String port, int baud, int parity, int dataBits) {
		setPort(port);
		setBaud(baud);
		setParity(parity);
		setDataBits(dataBits);
	}

	public NRSerialPort(String port, int baud, int parity, int dataBits, int stopBits) {
		setPort(port);
		setBaud(baud);
		setParity(parity);
		setDataBits(dataBits);
		setStopBits(stopBits);
	}

	public boolean connect() {
		if (isConnected()) {
			log.warn(port + " is already connected.");
			return true;
		}

		try {
			if (port.toLowerCase().startsWith("rfc2217"))
				serial = new RFC2217PortCreator().createPort(port);
			else
				serial = new RxTxPortCreator().createPort(port);
			serial.setSerialPortParams(getBaud(), getDataBits(), getStopBits(), getParity());
			setConnected(true);
		} catch (NativeResourceException e) {
			throw new NativeResourceException(e.getMessage());
		} catch (Exception e) {
			log.error("Failed to connect on port: " + port, e);
			setConnected(false);
		}

		if (isConnected()) {
			serial.notifyOnDataAvailable(true);
		}
		return isConnected();
	}

	public InputStream getInputStream() {
		try {
			return serial.getInputStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public OutputStream getOutputStream() {
		try {
			return serial.getOutputStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Set the port to use (i.e. COM6 or /dev/ttyUSB0)
	 * 
	 * @param port
	 *            the serial port to use
	 */
	private void setPort(String port) {
		this.port = port;
	}

	public void disconnect() {
		try {
			try {
				getInputStream().close();
				getOutputStream().close();
				serial.close();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			serial = null;
			setConnected(false);
		} catch (UnsatisfiedLinkError e) {
			throw new NativeResourceException(e.getMessage());
		}
	}

	public static Set<String> getAvailableSerialPorts() {
		Set<String> available = new HashSet<String>();
		try {
			RXTXCommDriver d = new RXTXCommDriver();
			Set<String> av = d.getPortIdentifiers();
			ArrayList<String> strs = new ArrayList<String>();
			for (String s : av) {
				strs.add(0, s);
			}
			for (String s : strs) {
				available.add(s);
			}
		} catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
			throw new NativeResourceException(e.getMessage());
		}

		return available;
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		if (this.connected == connected)
			return;
		this.connected = connected;
	}

	public void setBaud(int baud) {

		this.baud = baud;
		return;

	}

	public int getBaud() {
		return baud;
	}

	public void setParity(int parity) {
		this.parity = parity;
	}

	public int getParity() {
		return this.parity;
	}

	public void setStopBits(int stopBits) {
		this.stopBits = stopBits;
	}

	public int getStopBits() {
		return this.stopBits;
	}

	public void setDataBits(int dataBits) {
		this.dataBits = dataBits;
	}

	public int getDataBits() {
		return this.dataBits;
	}

	/**
	 * Enables RS485 half-duplex bus communication for Linux. The Linux kernel uses
	 * the RTS pin as bus enable. If you use a device that is configured via the
	 * Linux device tree, take care to add "uart-has-rtscts" and to configure the
	 * RTS GPIO correctly.
	 *
	 * Before enabling RS485, the serial port must be connected/opened.
	 *
	 * See also:
	 * <ul>
	 * <li>https://www.kernel.org/doc/Documentation/serial/serial-rs485.txt
	 * <li>https://www.kernel.org/doc/Documentation/devicetree/bindings/serial/serial.txt
	 * </ul>
	 *
	 * @param busEnableActiveLow
	 *            true, if the bus enable signal (RTS) shall be low during
	 *            transmission
	 * @param delayBusEnableBeforeSendMs
	 *            delay of bus enable signal (RTS) edge to first data edge in ms
	 *            (not supported by all serial drivers)
	 * @param delayBusEnableAfterSendMs
	 *            delay of bus enable signal (RTS) edge after end of transmission in
	 *            ms (not supported by all serial drivers)
	 * @return the ioctl() return value
	 */
	public int enableRs485(boolean busEnableActiveLow, int delayBusEnableBeforeSendMs, int delayBusEnableAfterSendMs) {
		if (serial == null)
			return -1;
		if (RXTXPort.class.isInstance(serial))
			return ((RXTXPort) serial).enableRs485(busEnableActiveLow, delayBusEnableBeforeSendMs,
					delayBusEnableAfterSendMs);
		return -1;
	}

	public void notifyOnDataAvailable(boolean b) {
		serial.notifyOnDataAvailable(b);
	}

	public void addEventListener(SerialPortEventListener lsnr) throws TooManyListenersException {
		serial.addEventListener(lsnr);
	}

	public void removeEventListener() {
		serial.removeEventListener();
	}

	/**
	 * Gets the {@link SerialPort} instance. This will return null until
	 * {@link #connect()} is successfully called.
	 * 
	 * @return The {@link SerialPort} instance or null.
	 */
	public RXTXPort getSerialPortInstance() {
		if (RXTXPort.class.isInstance(serial))
			return (RXTXPort) serial;
		return null;
	}
	/**
	 * Gets the {@link SerialPort} instance. This will return null until
	 * {@link #connect()} is successfully called.
	 * 
	 * @return The {@link SerialPort} instance or null.
	 */
	public TelnetSerialPort getTelnetSerialPortInstance() {
		if (TelnetSerialPort.class.isInstance(serial))
			return (TelnetSerialPort) serial;
		return null;
	}
}
