/*-------------------------------------------------------------------------
|   RXTX License v 2.1 - LGPL v 2.1 + Linking Over Controlled Interface.
|   RXTX is a native interface to serial ports in java.
|   Copyright 1997-2008 by Trent Jarvi tjarvi@qbang.org and others who
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;
import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An extension of gnu.io.SerialPort
 * 
 * @see gnu.io.SerialPort
 */

public class RXTXPort extends SerialPort {
	private static final Logger log = LoggerFactory.getLogger(RXTXPort.class);

	/*
	 * I had a report that some JRE's complain when MonitorThread tries to access
	 * private variables
	 */
	/**
	*/
	class MonitorThread extends Thread {
		/**
		 * Note: these have to be separate boolean flags because the SerialPortEvent
		 * constants are NOT bit-flags, they are just defined as integers from 1 to 10
		 * -DPL
		 */
		private volatile boolean CTS = false;
		private volatile boolean DSR = false;
		private volatile boolean RI = false;
		private volatile boolean CD = false;
		private volatile boolean OE = false;
		private volatile boolean PE = false;
		private volatile boolean FE = false;
		private volatile boolean BI = false;
		private volatile boolean Data = false;
		private volatile boolean Output = false;

		/**
		 * run the thread and call the event loop.
		 */
		public void run() {
			try {
				log.trace("RXTXPort:MonitorThread:run()");
				monThreadisInterrupted = false;
				eventLoop();
				eis = 0;
				log.trace("eventLoop() finished");
			} catch (Throwable ex) {
				HARDWARE_FAULT = true;
				sendEvent(SerialPortEvent.HARDWARE_ERROR, true);
			}
		}
		protected void finalize() throws Throwable {
			log.trace("RXTXPort:MonitorThread exiting");
		}
	}
	protected boolean HARDWARE_FAULT = false;

	private static Zystem z;

	static {
		try {
			z = new Zystem(Zystem.SILENT_MODE);
		} catch (Exception e) {
		}

		log.trace("RXTXPort {}");
		// System.loadLibrary( "rxtxSerial" );
		SerialManager.getInstance();

		Initialize();
	}

	/** Initialize the native library */
	private native static void Initialize();
	boolean MonitorThreadAlive = false;

	/**
	 * Open the named port
	 * 
	 * @param name
	 *            the name of the device to open PortInUseException
	 * @see gnu.io.SerialPort
	 */
	public RXTXPort(String name) throws PortInUseException {
		log.trace("RXTXPort:RXTXPort(" + name + ") called");
		/*
		 * commapi/javadocs/API_users_guide.html specifies that whenever an application
		 * tries to open a port in use by another application the PortInUseException
		 * will be thrown
		 * 
		 * I know some didnt like it this way but I'm not sure how to avoid it. We will
		 * just be writing to a bogus fd if we catch the exeption
		 * 
		 * Trent
		 */
		// try {
		fd = open(name);
		this.name = name;

		MonitorThreadLock = true;
		monThread = new MonitorThread();
		monThread.setName("RXTXPortMonitor(" + name + ")");
		monThread.start();
		waitForTheNativeCodeSilly();
		MonitorThreadAlive = true;
		// } catch ( PortInUseException e ){}
		timeout = -1; /* default disabled timeout */
		log.trace("RXTXPort:RXTXPort(" + name + ") returns with fd = " + fd);
	}
	private native synchronized int open(String name) throws PortInUseException;

	/* dont close the file while accessing the fd */
	private final java.util.concurrent.locks.ReentrantReadWriteLock IOLockedMutex = new java.util.concurrent.locks.ReentrantReadWriteLock(
			true);

	/** File descriptor */
	private int fd = 0;

	/**
	 * a pointer to the event info structure used to share information between
	 * threads so write threads can send output buffer empty from a pthread if need
	 * be.
	 * 
	 * long for 64 bit pointers.
	 */
	long eis = 0;
	/** pid for lock files */
	int pid = 0;

	/** DSR flag **/
	static boolean dsrFlag = false;

	/** Output stream */
	private final SerialOutputStream out = new SerialOutputStream();
	/**
	 * get the OutputStream
	 * 
	 * @return OutputStream
	 */
	public OutputStream getOutputStream() {
		return out;
	}

	/** Input stream */
	private final SerialInputStream in = new SerialInputStream();
	/**
	 * get the InputStream
	 * 
	 * @return InputStream
	 * @see java.io.InputStream
	 */
	public InputStream getInputStream() {
		return in;
	}

	/**
	 * Set the SerialPort parameters 1.5 stop bits requires 5 databits
	 * 
	 * @param b
	 *            baudrate
	 * @param d
	 *            databits
	 * @param s
	 *            stopbits
	 * @param p
	 *            parity UnsupportedCommOperationException
	 * @see gnu.io.UnsupportedCommOperationException
	 * 
	 *      If speed is not a predifined speed it is assumed to be the actual speed
	 *      desired.
	 */
	private native int nativeGetParity(int fd);
	private native int nativeGetFlowControlMode(int fd);
	public synchronized void setSerialPortParams(int b, int d, int s, int p) throws UnsupportedCommOperationException {
		log.trace("RXTXPort:setSerialPortParams(" + b + " " + d + " " + s + " " + p + ") called");
		if (nativeSetSerialPortParams(b, d, s, p))
			throw new UnsupportedCommOperationException("Invalid Parameter");
		speed = b;
		if (s == STOPBITS_1_5)
			dataBits = DATABITS_5;
		else
			dataBits = d;
		stopBits = s;
		parity = p;
		log.trace("RXTXPort:setSerialPortParams(" + b + " " + d + " " + s + " " + p + ") returning");
	}

	/**
	 * Set the native serial port parameters If speed is not a predifined speed it
	 * is assumed to be the actual speed desired.
	 */
	private native boolean nativeSetSerialPortParams(int speed, int dataBits, int stopBits, int parity)
			throws UnsupportedCommOperationException;

	/** Line speed in bits-per-second */
	private int speed = 9600;
	/**
	 * @return int representing the baudrate This will not behave as expected with
	 *         custom speeds
	 */
	public int getBaudRate() {
		return speed;
	}

	/** Data bits port parameter */
	private int dataBits = DATABITS_8;
	/**
	 * @return int representing the databits
	 */
	public int getDataBits() {
		return dataBits;
	}

	/** Stop bits port parameter */
	private int stopBits = SerialPort.STOPBITS_1;
	/**
	 * @return int representing the stopbits
	 */
	public int getStopBits() {
		return stopBits;
	}

	/** Parity port parameter */
	private int parity = SerialPort.PARITY_NONE;
	/**
	 * @return int representing the parity
	 */
	public int getParity() {
		return parity;
	}

	/** Flow control */
	private int flowmode = SerialPort.FLOWCONTROL_NONE;
	/**
	 * @param flowcontrol
	 *            FLOWCONTROL_NONE is default
	 * @see gnu.io.SerialPort#FLOWCONTROL_NONE
	 */
	public void setFlowControlMode(int flowcontrol) {
		log.trace("RXTXPort:setFlowControlMode( " + flowcontrol + " ) called");
		if (monThreadisInterrupted) {
			log.trace("RXTXPort:setFlowControlMode MonThread was interrupted");
			return;
		}
		try {
			setflowcontrol(flowcontrol);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		flowmode = flowcontrol;
		log.trace("RXTXPort:setFlowControlMode( " + flowcontrol + " ) returning");
	}
	/**
	 * @return int representing the flowmode
	 */
	public int getFlowControlMode() {
		return flowmode;
	}
	native void setflowcontrol(int flowcontrol) throws IOException;

	/*
	 * linux/drivers/char/n_hdlc.c? FIXME taj@www.linux.org.uk
	 */
	/**
	 * Receive framing control
	 * 
	 * @param f
	 *            framming UnsupportedCommOperationException
	 */
	public void enableReceiveFraming(int f) throws UnsupportedCommOperationException {
		throw new UnsupportedCommOperationException("Not supported");
	}
	/** 
	*/
	public void disableReceiveFraming() {
		log.trace("RXTXPort:disableReceiveFramming() called and returning (noop)");
	}
	/**
	 * @return true if framing is enabled
	 */
	public boolean isReceiveFramingEnabled() {
		return false;
	}
	/**
	 * @return int representing the framing byte
	 */
	public int getReceiveFramingByte() {
		return 0;
	}

	/** Receive timeout control */
	private int timeout;

	/**
	 * @return int the timeout
	 */
	public native int NativegetReceiveTimeout();
	/**
	 * @return bloolean true if recieve timeout is enabled
	 */
	private native boolean NativeisReceiveTimeoutEnabled();
	/**
	 * @param time
	 * @param threshold
	 * @param InputBuffer
	 */
	private native void NativeEnableReceiveTimeoutThreshold(int time, int threshold, int InputBuffer);
	/** 
	*/
	public void disableReceiveTimeout() {
		log.trace("RXTXPort:disableReceiveTimeout() called");
		timeout = -1;
		NativeEnableReceiveTimeoutThreshold(timeout, threshold, InputBuffer);
		log.trace("RXTXPort:disableReceiveTimeout() returning");
	}
	/**
	 * @param time
	 */
	public void enableReceiveTimeout(int time) {
		// log.trace("Enabling receive timeout: "+time);
		log.trace("RXTXPort:enableReceiveTimeout() called");
		if (time >= 0) {
			timeout = time;
			NativeEnableReceiveTimeoutThreshold(time, threshold, InputBuffer);
		} else {
			throw new IllegalArgumentException("Unexpected negative timeout value");
		}
		log.trace("RXTXPort:enableReceiveTimeout() returning");
	}
	/**
	 * @return boolean true if recieve timeout is enabled
	 */
	public boolean isReceiveTimeoutEnabled() {
		return (NativeisReceiveTimeoutEnabled());
	}
	/**
	 * @return int the timeout
	 */
	public int getReceiveTimeout() {
		return (NativegetReceiveTimeout());
	}

	/** Receive threshold control */

	private int threshold = 0;

	/**
	 * @param thresh
	 *            threshold
	 */
	public void enableReceiveThreshold(int thresh) {
		log.trace("RXTXPort:enableReceiveThreshold( " + thresh + " ) called");
		if (thresh >= 0) {
			threshold = thresh;
			NativeEnableReceiveTimeoutThreshold(timeout, threshold, InputBuffer);
		} else /* invalid thresh */
		{
			throw new IllegalArgumentException("Unexpected negative threshold value");
		}
		log.trace("RXTXPort:enableReceiveThreshold( " + thresh + " ) returned");
	}
	/** 
	*/
	public void disableReceiveThreshold() {
		enableReceiveThreshold(0);
	}
	/**
	 * @return int the recieve threshold
	 */
	public int getReceiveThreshold() {
		return threshold;
	}
	/**
	 * @return boolean true if receive threshold is enabled
	 */
	public boolean isReceiveThresholdEnabled() {
		return (threshold > 0);
	}

	/** Input/output buffers */
	/**
	 * FIXME I think this refers to FOPEN(3)/SETBUF(3)/FREAD(3)/FCLOSE(3)
	 * taj@www.linux.org.uk
	 * 
	 * These are native stubs...
	 */
	private int InputBuffer = 0;
	private int OutputBuffer = 0;
	/**
	 * @param size
	 */
	public void setInputBufferSize(int size) {
		log.trace("RXTXPort:setInputBufferSize( " + size + ") called");
		if (size < 0)
			throw new IllegalArgumentException("Unexpected negative buffer size value");
		else
			InputBuffer = size;
		log.trace("RXTXPort:setInputBufferSize( " + size + ") returning");
	}
	/** 
	*/
	public int getInputBufferSize() {
		return (InputBuffer);
	}
	/**
	 * @param size
	 */
	public void setOutputBufferSize(int size) {
		log.trace("RXTXPort:setOutputBufferSize( " + size + ") called");
		if (size < 0)
			throw new IllegalArgumentException("Unexpected negative buffer size value");
		else
			OutputBuffer = size;
		log.trace("RXTXPort:setOutputBufferSize( " + size + ") returned");

	}
	/**
	 * @return in the output buffer size
	 */
	public int getOutputBufferSize() {
		return (OutputBuffer);
	}

	/* =================== cleaned messages to here */

	/**
	 * Line status methods
	 */
	/**
	 * @return true if DTR is set
	 */
	public native boolean isDTR();
	/**
	 * @param state
	 */
	public native void setDTR(boolean state);
	/**
	 * @param state
	 */
	public native void setRTS(boolean state);
	private native void setDSR(boolean state);
	/**
	 * @return boolean true if CTS is set
	 */
	public native boolean isCTS();
	/**
	 * @return boolean true if DSR is set
	 */
	public native boolean isDSR();
	/**
	 * @return boolean true if CD is set
	 */
	public native boolean isCD();
	/**
	 * @return boolean true if RI is set
	 */
	public native boolean isRI();
	/**
	 * @return boolean true if RTS is set
	 */
	public native boolean isRTS();

	/**
	 * Write to the port
	 * 
	 * @param duration
	 */
	public native void sendBreak(int duration);
	protected native void writeByte(int b, boolean i) throws IOException;
	protected native void writeArray(byte b[], int off, int len, boolean i) throws IOException;
	protected native boolean nativeDrain(boolean i) throws IOException;

	/** RXTXPort read methods */
	protected native int nativeavailable() throws IOException;
	protected native int readByte() throws IOException;
	protected native int readArray(byte b[], int off, int len) throws IOException;
	protected native int readTerminatedArray(byte b[], int off, int len, byte t[]) throws IOException;

	/** Serial Port Event listener */
	private SerialPortEventListener SPEventListener;

	/** Thread to monitor data */
	private MonitorThread monThread;

	/** Process SerialPortEvents */
	native void eventLoop();

	/**
	 * @return boolean true if monitor thread is interrupted
	 */
	boolean monThreadisInterrupted = true;
	private native void interruptEventLoop();
	public boolean checkMonitorThread() {
		log.trace("RXTXPort:checkMonitorThread()");
		if (monThread != null) {
			log.trace("monThreadisInterrupted = " + monThreadisInterrupted);
			return monThreadisInterrupted;
		}
		log.trace("monThread is null ");
		return (true);
	}

	/**
	 * @param event
	 * @param state
	 * @return boolean true if the port is closing
	 */
	public boolean sendEvent(int event, boolean state) {
		/* Let the native side know its time to die */

		if (fd == 0 || SPEventListener == null || monThread == null) {
			return (true);
		}

		switch (event) {
			case SerialPortEvent.HARDWARE_ERROR :
				log.warn("RXTXPort:sendEvent: HARDWARE_ERROR " + monThread.Data + ")");
				break;
			case SerialPortEvent.DATA_AVAILABLE :
				log.trace("RXTXPort:sendEvent: DATA_AVAILABLE " + monThread.Data);
				break;
			case SerialPortEvent.OUTPUT_BUFFER_EMPTY :
				log.trace("RXTXPort:sendEvent: OUTPUT_BUFFER_EMPTY " + monThread.Output);
				break;
			case SerialPortEvent.CTS :
				log.trace("RXTXPort:sendEvent: CTS " + monThread.CTS);
				break;
			case SerialPortEvent.DSR :
				log.trace("RXTXPort:sendEvent: DSR " + monThread.Output);
				break;
			case SerialPortEvent.RI :
				log.trace("RXTXPort:sendEvent: RI " + monThread.RI);
				break;
			case SerialPortEvent.CD :
				log.trace("RXTXPort:sendEvent: CD " + monThread.CD);
				break;
			case SerialPortEvent.OE :
				log.trace("RXTXPort:sendEvent: OE " + monThread.OE);
				break;
			case SerialPortEvent.PE :
				log.trace("RXTXPort:sendEvent: PE " + monThread.PE);
				break;
			case SerialPortEvent.FE :
				log.trace("RXTXPort:sendEvent: FE " + monThread.FE);
				break;
			case SerialPortEvent.BI :
				log.trace("RXTXPort:sendEvent: BI " + monThread.BI);
				break;
			default :
				log.debug("RXTXPort:sendEvent: " + event);
				break;
		}

		switch (event) {
			case SerialPortEvent.DATA_AVAILABLE :
				if (monThread.Data)
					break;
				return (false);
			case SerialPortEvent.OUTPUT_BUFFER_EMPTY :
				if (monThread.Output)
					break;
				return (false);
			case SerialPortEvent.CTS :
				if (monThread.CTS)
					break;
				return (false);
			case SerialPortEvent.DSR :
				if (monThread.DSR)
					break;
				return (false);
			case SerialPortEvent.RI :
				if (monThread.RI)
					break;
				return (false);
			case SerialPortEvent.CD :
				if (monThread.CD)
					break;
				return (false);
			case SerialPortEvent.OE :
				if (monThread.OE)
					break;
				return (false);
			case SerialPortEvent.PE :
				if (monThread.PE)
					break;
				return (false);
			case SerialPortEvent.FE :
				if (monThread.FE)
					break;
				return (false);
			case SerialPortEvent.BI :
				if (monThread.BI)
					break;
				return (false);
			case SerialPortEvent.HARDWARE_ERROR :
				break;
			default :
				return (false);
		}
		SerialPortEvent e = new SerialPortEvent(this, event, !state, state);
		if (monThreadisInterrupted) {
			return (true);
		}
		if (SPEventListener != null) {
			SPEventListener.serialEvent(e);
		}

		if (fd == 0 || SPEventListener == null || monThread == null) {
			return (true);
		} else {
			return (false);
		}
	}

	/**
	 * Add an event listener
	 * 
	 * @param lsnr
	 *            SerialPortEventListener TooManyListenersException
	 */

	boolean MonitorThreadLock = true;

	public void addEventListener(SerialPortEventListener lsnr) throws TooManyListenersException {
		/*
		 * Don't let and notification requests happen until the Eventloop is ready
		 */

		log.trace("RXTXPort:addEventListener()");
		if (SPEventListener != null) {
			throw new TooManyListenersException();
		}
		SPEventListener = lsnr;
		if (!MonitorThreadAlive) {
			MonitorThreadLock = true;
			monThread = new MonitorThread();
			monThread.setName("RXTXPortMonitor(" + name + ")");
			monThread.start();
			waitForTheNativeCodeSilly();
			MonitorThreadAlive = true;
		}
		log.trace("RXTXPort:Interrupt=false");
	}
	/**
	 * Remove the serial port event listener
	 */
	public void removeEventListener() {
		log.trace("RXTXPort:removeEventListener() called");
		waitForTheNativeCodeSilly();
		// if( monThread != null && monThread.isAlive() )
		if (monThreadisInterrupted == true) {
			log.debug("	RXTXPort:removeEventListener() already interrupted");
			monThread = null;
			SPEventListener = null;
			return;
		} else if (monThread != null && monThread.isAlive() && !HARDWARE_FAULT) {
			log.trace("	RXTXPort:Interrupt=true");
			monThreadisInterrupted = true;
			/*
			 * Notify all threads in this PID that something is up They will call back to
			 * see if its their thread using isInterrupted().
			 */
			log.trace("	RXTXPort:calling interruptEventLoop");
			interruptEventLoop();

			log.trace("	RXTXPort:calling monThread.join()");
			try {

				// wait a reasonable moment for the death of the monitor thread
				monThread.join(3000);
			} catch (InterruptedException ex) {
				// somebody called interrupt() on us (ie wants us to abort)
				// we dont propagate InterruptedExceptions so lets re-set the flag
				Thread.currentThread().interrupt();
				return;
			}

			if (monThread.isAlive()) {
				log.warn("Failed to cancel monitor thread");
			}

		}
		monThread = null;
		SPEventListener = null;
		MonitorThreadLock = false;
		MonitorThreadAlive = false;
		monThreadisInterrupted = true;
		log.trace("RXTXPort:removeEventListener() returning");
	}
	/**
	 * Give the native code a chance to start listening to the hardware or should we
	 * say give the native code control of the issue.
	 *
	 * This is important for applications that flicker the Monitor thread while
	 * keeping the port open. In worst case test cases this loops once or twice
	 * every time.
	 */

	protected void waitForTheNativeCodeSilly() {
		while (MonitorThreadLock) {
			try {
				Thread.sleep(5);
			} catch (Exception e) {
			}
		}
	}
	/**
	 * @param enable
	 */
	private native void nativeSetEventFlag(int fd, int event, boolean flag);
	public void notifyOnDataAvailable(boolean enable) {
		waitForTheNativeCodeSilly();

		MonitorThreadLock = true;
		nativeSetEventFlag(fd, SerialPortEvent.DATA_AVAILABLE, enable);
		monThread.Data = enable;
		MonitorThreadLock = false;
	}

	/**
	 * @param enable
	 */
	public void notifyOnOutputEmpty(boolean enable) {
		waitForTheNativeCodeSilly();
		MonitorThreadLock = true;
		nativeSetEventFlag(fd, SerialPortEvent.OUTPUT_BUFFER_EMPTY, enable);
		monThread.Output = enable;
		MonitorThreadLock = false;
	}

	/**
	 * @param enable
	 */
	public void notifyOnCTS(boolean enable) {
		waitForTheNativeCodeSilly();
		MonitorThreadLock = true;
		nativeSetEventFlag(fd, SerialPortEvent.CTS, enable);
		monThread.CTS = enable;
		MonitorThreadLock = false;
	}
	/**
	 * @param enable
	 */
	public void notifyOnDSR(boolean enable) {
		waitForTheNativeCodeSilly();
		MonitorThreadLock = true;
		nativeSetEventFlag(fd, SerialPortEvent.DSR, enable);
		monThread.DSR = enable;
		MonitorThreadLock = false;
	}
	/**
	 * @param enable
	 */
	public void notifyOnRingIndicator(boolean enable) {
		waitForTheNativeCodeSilly();
		MonitorThreadLock = true;
		nativeSetEventFlag(fd, SerialPortEvent.RI, enable);
		monThread.RI = enable;
		MonitorThreadLock = false;
	}
	/**
	 * @param enable
	 */
	public void notifyOnCarrierDetect(boolean enable) {
		waitForTheNativeCodeSilly();
		MonitorThreadLock = true;
		nativeSetEventFlag(fd, SerialPortEvent.CD, enable);
		monThread.CD = enable;
		MonitorThreadLock = false;
	}
	/**
	 * @param enable
	 */
	public void notifyOnOverrunError(boolean enable) {
		waitForTheNativeCodeSilly();
		MonitorThreadLock = true;
		nativeSetEventFlag(fd, SerialPortEvent.OE, enable);
		monThread.OE = enable;
		MonitorThreadLock = false;
	}
	/**
	 * @param enable
	 */
	public void notifyOnParityError(boolean enable) {
		waitForTheNativeCodeSilly();
		MonitorThreadLock = true;
		nativeSetEventFlag(fd, SerialPortEvent.PE, enable);
		monThread.PE = enable;
		MonitorThreadLock = false;
	}
	/**
	 * @param enable
	 */
	public void notifyOnFramingError(boolean enable) {
		waitForTheNativeCodeSilly();
		MonitorThreadLock = true;
		nativeSetEventFlag(fd, SerialPortEvent.FE, enable);
		monThread.FE = enable;
		MonitorThreadLock = false;
	}
	/**
	 * @param enable
	 */
	public void notifyOnBreakInterrupt(boolean enable) {
		waitForTheNativeCodeSilly();
		MonitorThreadLock = true;
		nativeSetEventFlag(fd, SerialPortEvent.BI, enable);
		monThread.BI = enable;
		MonitorThreadLock = false;
	}

	/** Close the port */
	private native void nativeClose(String name);

	public void close() {
		log.trace("RXTXPort:close( " + this.name + " )");
		log.debug("Closing port: {}", name);

		try {
			while (!IOLockedMutex.writeLock().tryLock(500, TimeUnit.MILLISECONDS)) {
				log.debug("IO is locked " + IOLockedMutex.getReadLockCount());
			}

			if (fd <= 0) {
				log.debug("RXTXPort:close detected bad File Descriptor");
				return;
			}
			disableRs485();
			if (!HARDWARE_FAULT)
				setDTR(false);
			if (!HARDWARE_FAULT)
				setDSR(false);
			log.trace("RXTXPort:close( " + this.name + " ) setting monThreadisInterrupted");
			if (!monThreadisInterrupted) {
				removeEventListener();
			}
			log.trace("RXTXPort:close( " + this.name + " ) calling nativeClose");
			nativeClose(this.name);
			log.trace("RXTXPort:close( " + this.name + " ) calling super.close");
			super.close();
			fd = 0;
			log.trace("RXTXPort:close( " + this.name + " ) leaving");
		} catch (InterruptedException ie) {
			// somebody called interrupt() on us
			// we obey and return without closing the socket
			Thread.currentThread().interrupt();
			return;
		} finally {
			if (IOLockedMutex.writeLock().isHeldByCurrentThread()) {
				IOLockedMutex.writeLock().unlock();
			}
			log.debug("Closed port: {}", name);
		}

	}

	/** Finalize the port */
	protected void finalize() {
		log.trace("RXTXPort:finalize()");
		if (fd > 0) {
			log.trace("RXTXPort:calling close()");
			close();
		}
		z.finalize();
	}

	private String getParityAsString() {
		switch (parity) {
			case PARITY_NONE :
				return "N";
			case PARITY_ODD :
				return "O";
			case PARITY_EVEN :
				return "E";
			case PARITY_MARK :
				return "M";
			case PARITY_SPACE :
				return "S";
			default :
				return "?";
		}
	}

	private String getStopBitsAsString() {
		switch (stopBits) {
			case STOPBITS_1 :
				return "1";
			case STOPBITS_1_5 :
				return "1.5";
			case STOPBITS_2 :
				return "2";
			default :
				return "?";
		}
	}

	@Override
	public String toString() {
		return speed + " " + dataBits + getParityAsString() + getStopBitsAsString();
	}

	/** Inner class for SerialOutputStream */
	class SerialOutputStream extends OutputStream {
		/**
		 * @param b
		 *            IOException
		 */
		public void write(int b) throws IOException {
			log.trace("RXTXPort:SerialOutputStream:write(int)");
			if (speed == 0)
				return;
			if (monThreadisInterrupted == true) {
				return;
			}
			IOLockedMutex.readLock().lock();
			try {
				waitForTheNativeCodeSilly();
				if (fd == 0) {
					log.error("File Descriptor for prot zero!!");
					throw new IOException();
				}
				writeByte(b, monThreadisInterrupted);
				log.trace("Leaving RXTXPort:SerialOutputStream:write( int )");
			} finally {
				IOLockedMutex.readLock().unlock();
			}
		}
		/**
		 * @param b[]
		 *            IOException
		 */
		public void write(byte b[]) throws IOException {
			log.trace("Entering RXTXPort:SerialOutputStream:write(" + b.length + ") "/* + new String(b) */ );
			if (speed == 0)
				return;
			if (monThreadisInterrupted == true) {
				return;
			}
			if (fd == 0)
				throw new IOException();
			IOLockedMutex.readLock().lock();
			try {
				waitForTheNativeCodeSilly();
				writeArray(b, 0, b.length, monThreadisInterrupted);
				log.trace("Leaving RXTXPort:SerialOutputStream:write(" + b.length + ")");
			} finally {
				IOLockedMutex.readLock().unlock();
			}

		}
		/**
		 * @param b[]
		 * @param off
		 * @param len
		 *            IOException
		 */
		public void write(byte b[], int off, int len) throws IOException {
			if (speed == 0)
				return;
			if (off + len > b.length) {
				throw new IndexOutOfBoundsException("Invalid offset/length passed to read");
			}

			byte send[] = new byte[len];
			System.arraycopy(b, off, send, 0, len);
			log.trace("Entering RXTXPort:SerialOutputStream:write(" + send.length + " " + off + " " + len + " "
					+ ") " /* + new String(send) */ );
			if (fd == 0)
				throw new IOException();
			if (monThreadisInterrupted == true) {
				return;
			}
			IOLockedMutex.readLock().lock();
			try {
				waitForTheNativeCodeSilly();
				writeArray(send, 0, len, monThreadisInterrupted);
				log.trace("Leaving RXTXPort:SerialOutputStream:write(" + send.length + " " + off + " " + len + " "
						+ ") " /* + new String(send) */ );
			} finally {
				IOLockedMutex.readLock().unlock();
			}
		}
		/**
		*/
		public void flush() throws IOException {
			log.trace("RXTXPort:SerialOutputStream:flush() enter");
			if (speed == 0)
				return;
			if (fd == 0)
				throw new IOException();
			if (monThreadisInterrupted == true) {
				log.trace("RXTXPort:SerialOutputStream:flush() Leaving Interrupted");
				return;
			}
			IOLockedMutex.readLock().lock();
			try {
				waitForTheNativeCodeSilly();
				/*
				 * this is probably good on all OS's but for now just sendEvent from java on Sol
				 */
				if (nativeDrain(monThreadisInterrupted))
					sendEvent(SerialPortEvent.OUTPUT_BUFFER_EMPTY, true);
				log.trace("RXTXPort:SerialOutputStream:flush() leave");
			} finally {
				IOLockedMutex.readLock().unlock();
			}
		}
	}

	/** Inner class for SerialInputStream */
	class SerialInputStream extends InputStream {
		/**
		 * @return int the int read IOException
		 * @see java.io.InputStream
		 *
		 *      timeout threshold Behavior
		 *      ------------------------------------------------------------------------
		 *      0 0 blocks until 1 byte is available timeout > 0, threshold = 0, blocks
		 *      until timeout occurs, returns -1 on timeout >0 >0 blocks until timeout,
		 *      returns - 1 on timeout, magnitude of threshold doesn't play a role. 0 >0
		 *      Blocks until 1 byte, magnitude of threshold doesn't play a role
		 */
		public synchronized int read() throws IOException {
			if (fd == 0)
				throw new IOException();
			if (monThreadisInterrupted) {
				log.debug("read(): monThreadisInterrupted");
			}
			IOLockedMutex.readLock().lock();
			try {
				waitForTheNativeCodeSilly();
				int result = readByte();
				return (result);
			} finally {
				IOLockedMutex.readLock().unlock();
			}
		}
		/**
		 * @param b[]
		 * @return int number of bytes read IOException
		 *
		 *         timeout threshold Behavior
		 *         ------------------------------------------------------------------------
		 *         0 0 blocks until 1 byte is available >0 0 blocks until timeout
		 *         occurs, returns 0 on timeout >0 >0 blocks until timeout or reads
		 *         threshold bytes, returns 0 on timeout 0 >0 blocks until reads
		 *         threshold bytes
		 */
		public synchronized int read(byte b[]) throws IOException {
			int result;
			if (monThreadisInterrupted == true) {
				return (0);
			}
			IOLockedMutex.readLock().lock();
			try {
				waitForTheNativeCodeSilly();
				result = read(b, 0, b.length);
				return (result);
			} finally {
				IOLockedMutex.readLock().unlock();
			}
		}
		/*
		 * read(byte b[], int, int) Documentation is at
		 * http://java.sun.com/products/jdk/1.2/docs/api/java/io/InputStream.html#read(
		 * byte[], int, int)
		 */
		/**
		 * @param b[]
		 * @param off
		 * @param len
		 * @return int number of bytes read IOException
		 *
		 *         timeout threshold Behavior
		 *         ------------------------------------------------------------------------
		 *         0 0 blocks until 1 byte is available >0 0 blocks until timeout
		 *         occurs, returns 0 on timeout >0 >0 blocks until timeout or reads
		 *         threshold bytes, returns 0 on timeout 0 >0 blocks until either
		 *         threshold # of bytes or len bytes, whichever was lower.
		 */
		public synchronized int read(byte b[], int off, int len) throws IOException {
			int result;
			/*
			 * Some sanity checks
			 */
			if (fd == 0) {
				throw new IOException();
			}

			if (b == null) {
				throw new NullPointerException("Input byte array is null");
			}

			if ((off < 0) || (len < 0) || (off + len > b.length)) {
				throw new IndexOutOfBoundsException("Offset: " + off + " Length: " + len + " Data length: " + b.length);
			}

			/*
			 * Return immediately if len==0
			 */
			if (len == 0) {
				return 0;
			}
			/*
			 * See how many bytes we should read
			 */
			int Minimum = len;

			if (threshold == 0) {
				/*
				 * If threshold is disabled, read should return as soon as data are available
				 * (up to the amount of available bytes in order to avoid blocking) Read may
				 * return earlier depending of the receive time out.
				 */
				int a = nativeavailable();
				if (a == 0)
					Minimum = 1;
				else
					Minimum = Math.min(Minimum, a);
			} else {
				/*
				 * Threshold is enabled. Read should return when 'threshold' bytes have been
				 * received (or when the receive timeout expired)
				 */
				Minimum = Math.min(Minimum, threshold);
			}
			if (monThreadisInterrupted == true) {
				log.debug("RXTXPort:SerialInputStream:read() Interrupted");
				return (0);
			}
			IOLockedMutex.readLock().lock();
			try {
				waitForTheNativeCodeSilly();
				result = readArray(b, off, Minimum);
				return (result);
			} finally {
				IOLockedMutex.readLock().unlock();
			}
		}

		/**
		 * @param b[]
		 * @param off
		 * @param len
		 * @param t[]
		 * @return int number of bytes read IOException
		 * 
		 *         We are trying to catch the terminator in the native code Right now it
		 *         is assumed that t[] is an array of 2 bytes.
		 * 
		 *         if the read encounters the two bytes, it will return and the array
		 *         will contain the terminator. Otherwise read behavior should be the
		 *         same as read( b[], off, len ). Timeouts have not been well tested.
		 */

		public synchronized int read(byte b[], int off, int len, byte t[]) throws IOException {
			log.trace("RXTXPort:SerialInputStream:read(" + b.length + " " + off + " " + len
					+ ") called" /* + new String(b) */ );
			int result;
			/*
			 * Some sanity checks
			 */
			if (fd == 0) {
				throw new IOException();
			}

			if (b == null) {
				throw new NullPointerException("Input byte array is null");
			}

			if ((off < 0) || (len < 0) || (off + len > b.length)) {
				throw new IndexOutOfBoundsException("Offset: " + off + " Length: " + len + " Data length: " + b.length);
			}

			/*
			 * Return immediately if len==0
			 */
			if (len == 0) {
				return 0;
			}
			/*
			 * See how many bytes we should read
			 */
			int Minimum = len;

			if (threshold == 0) {
				/*
				 * If threshold is disabled, read should return as soon as data are available
				 * (up to the amount of available bytes in order to avoid blocking) Read may
				 * return earlier depending of the receive time out.
				 */
				int a = nativeavailable();
				if (a == 0)
					Minimum = 1;
				else
					Minimum = Math.min(Minimum, a);
			} else {
				/*
				 * Threshold is enabled. Read should return when 'threshold' bytes have been
				 * received (or when the receive timeout expired)
				 */
				Minimum = Math.min(Minimum, threshold);
			}
			if (monThreadisInterrupted == true) {
				log.debug("RXTXPort:SerialInputStream:read() Interrupted");
				return (0);
			}
			IOLockedMutex.readLock().lock();
			try {
				waitForTheNativeCodeSilly();
				result = readTerminatedArray(b, off, Minimum, t);
				return (result);
			} finally {
				IOLockedMutex.readLock().unlock();
			}
		}
		/**
		 * @return int bytes available IOException
		 */
		public synchronized int available() throws IOException {
			if (monThreadisInterrupted == true) {
				return (0);
			}
			IOLockedMutex.readLock().lock();
			try {
				int r = nativeavailable();
				return r;
			} finally {
				IOLockedMutex.readLock().unlock();
			}
		}
	}

	/**
	 * A dummy method added so RXTX compiles on Kaffee
	 * 
	 * @deprecated deprecated but used in Kaffe
	 */
	public void setRcvFifoTrigger(int trigger) {
	};

	/*------------------------  END OF CommAPI -----------------------------*/

	private native static void nativeStaticSetSerialPortParams(String f, int b, int d, int s, int p)
			throws UnsupportedCommOperationException;
	private native static boolean nativeStaticSetDSR(String port, boolean flag)
			throws UnsupportedCommOperationException;
	private native static boolean nativeStaticSetDTR(String port, boolean flag)
			throws UnsupportedCommOperationException;
	private native static boolean nativeStaticSetRTS(String port, boolean flag)
			throws UnsupportedCommOperationException;

	private native static boolean nativeStaticIsDSR(String port) throws UnsupportedCommOperationException;
	private native static boolean nativeStaticIsDTR(String port) throws UnsupportedCommOperationException;
	private native static boolean nativeStaticIsRTS(String port) throws UnsupportedCommOperationException;
	private native static boolean nativeStaticIsCTS(String port) throws UnsupportedCommOperationException;
	private native static boolean nativeStaticIsCD(String port) throws UnsupportedCommOperationException;
	private native static boolean nativeStaticIsRI(String port) throws UnsupportedCommOperationException;

	private native static int nativeStaticGetBaudRate(String port) throws UnsupportedCommOperationException;
	private native static int nativeStaticGetDataBits(String port) throws UnsupportedCommOperationException;
	private native static int nativeStaticGetParity(String port) throws UnsupportedCommOperationException;
	private native static int nativeStaticGetStopBits(String port) throws UnsupportedCommOperationException;

	private native byte nativeGetParityErrorChar() throws UnsupportedCommOperationException;
	private native boolean nativeSetParityErrorChar(byte b) throws UnsupportedCommOperationException;
	private native byte nativeGetEndOfInputChar() throws UnsupportedCommOperationException;
	private native boolean nativeSetEndOfInputChar(byte b) throws UnsupportedCommOperationException;
	private native boolean nativeSetUartType(String type, boolean test) throws UnsupportedCommOperationException;
	native String nativeGetUartType() throws UnsupportedCommOperationException;
	private native boolean nativeSetBaudBase(int BaudBase) throws UnsupportedCommOperationException;
	private native int nativeGetBaudBase() throws UnsupportedCommOperationException;
	private native boolean nativeSetDivisor(int Divisor) throws UnsupportedCommOperationException;
	private native int nativeGetDivisor() throws UnsupportedCommOperationException;
	private native boolean nativeSetLowLatency() throws UnsupportedCommOperationException;
	private native boolean nativeGetLowLatency() throws UnsupportedCommOperationException;
	private native boolean nativeSetCallOutHangup(boolean NoHup) throws UnsupportedCommOperationException;
	private native boolean nativeGetCallOutHangup() throws UnsupportedCommOperationException;
	private native boolean nativeClearCommInput() throws UnsupportedCommOperationException;

	/**
	 * Extension to CommAPI This is an extension to CommAPI. It may not be supported
	 * on all operating systems.
	 *
	 * This is only accurate up to 38600 baud currently.
	 *
	 * @param port
	 *            the name of the port thats been preopened
	 * @return BaudRate on success UnsupportedCommOperationException; This will not
	 *         behave as expected with custom speeds
	 *
	 */
	public static int staticGetBaudRate(String port) throws UnsupportedCommOperationException {
		return (nativeStaticGetBaudRate(port));
	}
	/**
	 * Extension to CommAPI This is an extension to CommAPI. It may not be supported
	 * on all operating systems.
	 *
	 * @param port
	 *            the name of the port thats been preopened
	 * @return DataBits on success UnsupportedCommOperationException;
	 *
	 */
	public static int staticGetDataBits(String port) throws UnsupportedCommOperationException {
		return (nativeStaticGetDataBits(port));
	}

	/**
	 * Extension to CommAPI This is an extension to CommAPI. It may not be supported
	 * on all operating systems.
	 *
	 * @param port
	 *            the name of the port thats been preopened
	 * @return Parity on success UnsupportedCommOperationException;
	 *
	 */
	public static int staticGetParity(String port) throws UnsupportedCommOperationException {
		return (nativeStaticGetParity(port));
	}

	/**
	 * Extension to CommAPI This is an extension to CommAPI. It may not be supported
	 * on all operating systems.
	 *
	 * @param port
	 *            the name of the port thats been preopened
	 * @return StopBits on success UnsupportedCommOperationException;
	 *
	 */
	public static int staticGetStopBits(String port) throws UnsupportedCommOperationException {
		return (nativeStaticGetStopBits(port));
	}

	/**
	 * Extension to CommAPI This is an extension to CommAPI. It may not be supported
	 * on all operating systems.
	 *
	 * Set the SerialPort parameters 1.5 stop bits requires 5 databits
	 * 
	 * @param f
	 *            filename
	 * @param b
	 *            baudrate
	 * @param d
	 *            databits
	 * @param s
	 *            stopbits
	 * @param p
	 *            parity
	 *
	 *            UnsupportedCommOperationException
	 * @see gnu.io.UnsupportedCommOperationException
	 */

	public static void staticSetSerialPortParams(String f, int b, int d, int s, int p)
			throws UnsupportedCommOperationException {
		log.trace("RXTXPort:staticSetSerialPortParams( " + f + " " + b + " " + d + " " + s + " " + p);
		nativeStaticSetSerialPortParams(f, b, d, s, p);
	}

	/**
	 * Extension to CommAPI This is an extension to CommAPI. It may not be supported
	 * on all operating systems.
	 *
	 * Open the port and set DSR. remove lockfile and do not close This is so some
	 * software can appear to set the DSR before 'opening' the port a second time
	 * later on.
	 *
	 * @return true on success UnsupportedCommOperationException;
	 *
	 */

	public static boolean staticSetDSR(String port, boolean flag) throws UnsupportedCommOperationException {
		log.trace("RXTXPort:staticSetDSR( " + port + " " + flag);
		return (nativeStaticSetDSR(port, flag));
	}

	/**
	 * Extension to CommAPI This is an extension to CommAPI. It may not be supported
	 * on all operating systems.
	 *
	 * Open the port and set DTR. remove lockfile and do not close This is so some
	 * software can appear to set the DTR before 'opening' the port a second time
	 * later on.
	 *
	 * @return true on success UnsupportedCommOperationException;
	 *
	 */

	public static boolean staticSetDTR(String port, boolean flag) throws UnsupportedCommOperationException {
		log.trace("RXTXPort:staticSetDTR( " + port + " " + flag);
		return (nativeStaticSetDTR(port, flag));
	}

	/**
	 * Extension to CommAPI This is an extension to CommAPI. It may not be supported
	 * on all operating systems.
	 *
	 * Open the port and set RTS. remove lockfile and do not close This is so some
	 * software can appear to set the RTS before 'opening' the port a second time
	 * later on.
	 *
	 * @return none UnsupportedCommOperationException;
	 *
	 */

	public static boolean staticSetRTS(String port, boolean flag) throws UnsupportedCommOperationException {
		log.trace("RXTXPort:staticSetRTS( " + port + " " + flag);
		return (nativeStaticSetRTS(port, flag));
	}

	/**
	 * Extension to CommAPI This is an extension to CommAPI. It may not be supported
	 * on all operating systems.
	 *
	 * find the fd and return RTS without using a Java open() call
	 *
	 * @param port
	 * @return true if asserted UnsupportedCommOperationException;
	 *
	 */

	public static boolean staticIsRTS(String port) throws UnsupportedCommOperationException {
		log.trace("RXTXPort:staticIsRTS( " + port + " )");
		return (nativeStaticIsRTS(port));
	}
	/**
	 * Extension to CommAPI This is an extension to CommAPI. It may not be supported
	 * on all operating systems.
	 *
	 * find the fd and return CD without using a Java open() call
	 *
	 * @param port
	 * @return true if asserted UnsupportedCommOperationException;
	 *
	 */

	public static boolean staticIsCD(String port) throws UnsupportedCommOperationException {
		log.trace("RXTXPort:staticIsCD( " + port + " )");
		return (nativeStaticIsCD(port));
	}
	/**
	 * Extension to CommAPI This is an extension to CommAPI. It may not be supported
	 * on all operating systems.
	 *
	 * find the fd and return CTS without using a Java open() call
	 *
	 * @param port
	 * @return true if asserted UnsupportedCommOperationException;
	 *
	 */

	public static boolean staticIsCTS(String port) throws UnsupportedCommOperationException {
		log.trace("RXTXPort:staticIsCTS( " + port + " )");
		return (nativeStaticIsCTS(port));
	}
	/**
	 * Extension to CommAPI This is an extension to CommAPI. It may not be supported
	 * on all operating systems.
	 *
	 * find the fd and return DSR without using a Java open() call
	 *
	 * @param port
	 * @return true if asserted UnsupportedCommOperationException;
	 *
	 */

	public static boolean staticIsDSR(String port) throws UnsupportedCommOperationException {
		log.trace("RXTXPort:staticIsDSR( " + port + " )");
		return (nativeStaticIsDSR(port));
	}
	/**
	 * Extension to CommAPI This is an extension to CommAPI. It may not be supported
	 * on all operating systems.
	 *
	 * find the fd and return DTR without using a Java open() call
	 *
	 * @param port
	 * @return true if asserted UnsupportedCommOperationException;
	 *
	 */

	public static boolean staticIsDTR(String port) throws UnsupportedCommOperationException {
		log.trace("RXTXPort:staticIsDTR( " + port + " )");
		return (nativeStaticIsDTR(port));
	}
	/**
	 * Extension to CommAPI This is an extension to CommAPI. It may not be supported
	 * on all operating systems.
	 *
	 * find the fd and return RI without using a Java open() call
	 *
	 * @param port
	 * @return true if asserted UnsupportedCommOperationException;
	 *
	 */

	public static boolean staticIsRI(String port) throws UnsupportedCommOperationException {
		log.trace("RXTXPort:staticIsRI( " + port + " )");
		return (nativeStaticIsRI(port));
	}

	/**
	 * Extension to CommAPI This is an extension to CommAPI. It may not be supported
	 * on all operating systems.
	 * 
	 * @return int the Parity Error Character UnsupportedCommOperationException;
	 *
	 *         Anyone know how to do this in Unix?
	 */

	public byte getParityErrorChar() throws UnsupportedCommOperationException {
		byte ret;
		ret = nativeGetParityErrorChar();
		return (ret);
	}

	/**
	 * Extension to CommAPI This is an extension to CommAPI. It may not be supported
	 * on all operating systems.
	 * 
	 * @param b
	 *            Parity Error Character
	 * @return boolean true on success UnsupportedCommOperationException;
	 *
	 *         Anyone know how to do this in Unix?
	 */

	public boolean setParityErrorChar(byte b) throws UnsupportedCommOperationException {
		log.trace("setParityErrorChar(" + b + ")");
		return (nativeSetParityErrorChar(b));
	}

	/**
	 * Extension to CommAPI This is an extension to CommAPI. It may not be supported
	 * on all operating systems.
	 * 
	 * @return int the End of Input Character UnsupportedCommOperationException;
	 *
	 *         Anyone know how to do this in Unix?
	 */

	public byte getEndOfInputChar() throws UnsupportedCommOperationException {
		byte ret;
		ret = nativeGetEndOfInputChar();
		return (ret);
	}

	/**
	 * Extension to CommAPI This is an extension to CommAPI. It may not be supported
	 * on all operating systems.
	 * 
	 * @param b
	 *            End Of Input Character
	 * @return boolean true on success UnsupportedCommOperationException;
	 */

	public boolean setEndOfInputChar(byte b) throws UnsupportedCommOperationException {
		log.trace("setEndOfInputChar(" + b + ")");
		return (nativeSetEndOfInputChar(b));
	}

	/**
	 * Extension to CommAPI This is an extension to CommAPI. It may not be supported
	 * on all operating systems.
	 * 
	 * @param type
	 *            String representation of the UART type which mayb be "none",
	 *            "8250", "16450", "16550", "16550A", "16650", "16550V2" or "16750".
	 * @param test
	 *            boolean flag to determin if the UART should be tested.
	 * @return boolean true on success UnsupportedCommOperationException;
	 */
	public boolean setUARTType(String type, boolean test) throws UnsupportedCommOperationException {
		log.trace("RXTXPort:setUARTType()");
		return nativeSetUartType(type, test);
	}
	/**
	 * Extension to CommAPI This is an extension to CommAPI. It may not be supported
	 * on all operating systems.
	 * 
	 * @return type String representation of the UART type which mayb be "none",
	 *         "8250", "16450", "16550", "16550A", "16650", "16550V2" or "16750".
	 *         UnsupportedCommOperationException;
	 */
	public String getUARTType() throws UnsupportedCommOperationException {
		return nativeGetUartType();
	}

	/**
	 * Extension to CommAPI. Set Baud Base to 38600 on Linux and W32 before using.
	 * 
	 * @param BaudBase
	 *            The clock frequency divided by 16. Default BaudBase is 115200.
	 * @return true on success UnsupportedCommOperationException, IOException
	 */

	public boolean setBaudBase(int BaudBase) throws UnsupportedCommOperationException, IOException {
		log.trace("RXTXPort:setBaudBase()");
		return nativeSetBaudBase(BaudBase);
	}

	/**
	 * Extension to CommAPI
	 * 
	 * @return BaudBase UnsupportedCommOperationException, IOException
	 */

	public int getBaudBase() throws UnsupportedCommOperationException, IOException {
		return nativeGetBaudBase();
	}

	/**
	 * Extension to CommAPI. Set Baud Base to 38600 on Linux and W32 before using.
	 * 
	 * @param Divisor
	 *            UnsupportedCommOperationException, IOException
	 */

	public boolean setDivisor(int Divisor) throws UnsupportedCommOperationException, IOException {
		return nativeSetDivisor(Divisor);
	}

	/**
	 * Extension to CommAPI
	 * 
	 * @return Divisor; UnsupportedCommOperationException, IOException
	 */

	public int getDivisor() throws UnsupportedCommOperationException, IOException {
		return nativeGetDivisor();
	}

	/**
	 * Extension to CommAPI returns boolean true on success
	 * UnsupportedCommOperationException
	 */

	public boolean setLowLatency() throws UnsupportedCommOperationException {
		return nativeSetLowLatency();
	}

	/**
	 * Extension to CommAPI returns boolean true on success
	 * UnsupportedCommOperationException
	 */

	public boolean getLowLatency() throws UnsupportedCommOperationException {
		return nativeGetLowLatency();
	}

	/**
	 * Extension to CommAPI returns boolean true on success
	 * UnsupportedCommOperationException
	 */

	public boolean setCallOutHangup(boolean NoHup) throws UnsupportedCommOperationException {
		return nativeSetCallOutHangup(NoHup);
	}

	/**
	 * Extension to CommAPI returns boolean true on success
	 * UnsupportedCommOperationException
	 */

	public boolean getCallOutHangup() throws UnsupportedCommOperationException {
		return nativeGetCallOutHangup();
	}

	/**
	 * Extension to CommAPI returns boolean true on success
	 * UnsupportedCommOperationException
	 */

	public boolean clearCommInput() throws UnsupportedCommOperationException {
		return nativeClearCommInput();
	}

	public int enableRs485(boolean busEnableActiveLow, int delayBusEnableBeforeSendMs, int delayBusEnableAfterSendMs) {
		return controlRs485(fd, true, busEnableActiveLow, delayBusEnableBeforeSendMs, delayBusEnableAfterSendMs);
	}

	public int disableRs485() {
		return controlRs485(fd, false, false, 0, 0);
	}

	private native synchronized int controlRs485(int fd, boolean enable, boolean busEnableActiveLow,
			int delayBusEnableBeforeSendMs, int delayBusEnableAfterSendMs);

	/*------------------------  END OF CommAPI Extensions -----------------------*/
}
