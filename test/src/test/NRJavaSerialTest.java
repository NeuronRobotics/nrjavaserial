package test;

import gnu.io.RXTXCommDriver;
import gnu.io.SerialManager;

public class NRJavaSerialTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			RXTXCommDriver.nativeGetVersion();
		}catch(Exception ex){
			ex.printStackTrace();
		}catch(Error er){
			er.printStackTrace();
		}
		System.exit(0);
	}

}
