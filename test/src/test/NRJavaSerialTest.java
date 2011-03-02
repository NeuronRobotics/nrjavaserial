package test;

import java.util.Enumeration;

import gnu.io.CommPortIdentifier;
import gnu.io.RXTXCommDriver;

public class NRJavaSerialTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Starting Test..");
		try{
			Enumeration<CommPortIdentifier> ports = CommPortIdentifier.getPortIdentifiers();
			while(ports.hasMoreElements())
				System.out.println(ports.nextElement().getName());
		}catch(Exception ex){
			ex.printStackTrace();
		}catch(Error er){
			er.printStackTrace();
		}
		System.exit(0);
	}

}
