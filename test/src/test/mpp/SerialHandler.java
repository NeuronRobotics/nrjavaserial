package test.mpp;
// adapted from https://github.com/synogen/mpp/blob/master/src/main/java/org/mppsolartest/serial/SerialHandler.java

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialHandler {
	private InputStream input;
	private OutputStream output;
	private int errorCount = 0;

	public SerialHandler(InputStream input, OutputStream output) {
		this.input = input;
		this.output = output;
	}

	public synchronized String executeCommand(String command) throws IOException {
		var result = true;
		try {
			output.write(command.getBytes());
			var crc = CRCUtil.getCRCByte(command);
			// on /dev/hidraw writing one byte fails
			// write(8, "\r", 1)	   = -1 EINVAL (Invalid argument)
			byte[] hack = new byte[crc.length + 1];
			System.arraycopy(crc, 0, hack, 0, crc.length);
			hack[crc.length] = '\r';
			output.write(hack);
			/* This could be useful, when using MPP over LAN cable, RS232 or Bluetooth
			 * try {  // on hidraw devices ioctl(8, TCSBRK, 1) causes "Invalid argument in nativeDrain"
			 *	output.flush();
			 * } catch (IOException e) { }
			*/
			var timeout = System.currentTimeMillis() + 3000L;
			var sb = new StringBuilder();
			var linebreak = false;
			byte[] b = new byte[8];
			outerloop:
			while (System.currentTimeMillis() < timeout)
				if (input.read(b) > 0) {
					for (int by : b) {
						if (by == 13) {
							linebreak = true;
							break outerloop;
						}
						sb.append((char) by);
					}
				}

			if (!linebreak)
				result = false;
			var returnValue = sb.toString();
			return CRCUtil.checkCRC(returnValue) ? returnValue.substring(1, returnValue.length() - 2) : "";
		} catch (IOException e) {
			result = false;
			throw e;
		} finally {
			errorCount = result ? 0 : errorCount + 1;
			if (errorCount >= 12)
				System.err.println("[Serial] Communication failed " + Integer.toString(errorCount) + " times");
		}
	}

	public int errorCount() {
		return errorCount;
	}
}
