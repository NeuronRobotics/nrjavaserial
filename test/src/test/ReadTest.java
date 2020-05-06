package test;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.TooManyListenersException;

import gnu.io.NRSerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.Zystem;
public class ReadTest {
	public static void main(String [] args) {

		String port = "";
		for(String s:NRSerialPort.getAvailableSerialPorts()){
			System.out.println("Availible port: "+s);
			port=s;
		}

		int baudRate = 115200;
		NRSerialPort serial = new NRSerialPort(port, baudRate);
		serial.connect();
		DataInputStream ins = new DataInputStream(serial.getInputStream());
		try {
			serial.addEventListener(ev->{
				if(ev.getEventType()==SerialPortEvent.DATA_AVAILABLE) {
					//while(ins.available()==0 && !Thread.interrupted());// wait for a byte
					try {
						while(ins.available()>0) {// read all bytes
							
								char b = (char) ins.read();
								//outs.write((byte)b);
								System.out.print(b);
							
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}if(ev.getEventType()==SerialPortEvent.HARDWARE_ERROR) {
					System.out.println("Hardware disconnected cleanly");
				}
			});
		} catch (TooManyListenersException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		
//		DataOutputStream outs = new DataOutputStream(serial.getOutputStream());
//		try{
//			//while(ins.available()==0 && !Thread.interrupted());// wait for a byte
//			while(!Thread.interrupted()) {// read all bytes
//				if(ins.available()>0) {
//					char b = (char) ins.read();
//					//outs.write((byte)b);
//					System.out.print(b);
//				}
//		    		Thread.sleep(5);
//			}
//		}catch(Exception ex){
//			ex.printStackTrace();
//		}
//		serial.disconnect();
	}
}
