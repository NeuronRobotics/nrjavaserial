package test.mpp;

import java.io.IOException;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.RXTXPort;

public class MPP implements AutoCloseable {
	private RXTXPort r;
	private SerialHandler serialHandler;

	public MPP(final String port) throws NoSuchPortException, PortInUseException {
		r = CommPortIdentifier.getPortIdentifier(port).open(MPP.class.getSimpleName(), 1000); // calls nativeavailable
		try {
			/*
			 * read(1) on hidraw devices does not work, so read(byte[]) must be done.  But
			 * ioctl(…FIORDCHK, 0) also does not work, so to avoid calling RXTXPort.nativeavailable
			 * threshold must be set.  It turns out that the received data arrives in batches of 8.
			 */
			r.enableReceiveThreshold(8);
		} catch (Exception e) {
			/*
			 * IOException: Invalid argument in TimeoutThreshold, likewise for below.  The reason
			 * is that RXTXPort.NativeEnableReceiveTimeoutThreshold aborts when tcgetattr() fails.
			 * Why Excepiton and not IOException?  Because the declaration in RXTXPort.java for
			 * native void NativeEnableReceiveTimeoutThreshold does not say it can throw an
			 * exception, so it throws only RuntimeExceptions in theory and IOException in practice.
			 */
		}
		try { // useful when the cable is unplugged or device is off
			r.enableReceiveTimeout(1000);
		} catch (Exception e) {} // as above
		serialHandler = new SerialHandler(r.getInputStream(), r.getOutputStream());
	}

	public static void main(String[] args) {
		final String port = args.length == 1 ? args[0] : "/dev/hidraw0";

		try (MPP d = new MPP(port)) {
			System.out.println("Main CPU Firmware:    " + d.command("QVFW"));
			System.out.println("Another CPU Firmware: " + d.command("QVFW2"));
			System.out.println("Device Protocol ID:   " + d.command("QPI"));
			System.out.println("Device Serial Number: " + d.command("QID"));
		} catch (NoSuchPortException e) {
			System.err.println("No such port " + port + " " + e);
		} catch (PortInUseException e) {
			System.err.println("Port in use " + e);
		}
	}

	public String command(String command) {
		try {
			return serialHandler.executeCommand(command);
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	@Override
	public void close() {
		final RXTXPort localR = r;
		if (localR != null) {
			localR.close();
			r = null;
		}
		serialHandler = null;
	}
}
