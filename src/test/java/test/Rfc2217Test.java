package test;

import gnu.io.NRSerialPort;

public class Rfc2217Test {

	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		NRSerialPort serial = new NRSerialPort("rfc2217://192.168.1.1:2001", 115200);
		serial.connect();
		Thread.sleep(2000);
		serial.disconnect();
	}

}
