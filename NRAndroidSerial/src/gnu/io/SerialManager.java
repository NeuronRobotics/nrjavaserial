package gnu.io;

public class SerialManager {
	
	private static SerialManager instance;
	private static boolean loaded = false;
	private SerialManager() {
		System.out.println("Starting the NRjavaSerial Serial Manager...");
		if(!loaded) {
			loaded = true;
			
			try {
				NativeResource nr = new NativeResource();
				nr.load("libNRJavaSerial");
			} catch(Exception e) {
				
			}
		}
	}
	
	public static SerialManager getInstance() {
		if(instance == null) {
			instance = new SerialManager();
		}		
		return instance;
	}
}
