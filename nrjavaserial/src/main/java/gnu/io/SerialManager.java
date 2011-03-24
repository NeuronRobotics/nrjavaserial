package gnu.io;

public class SerialManager {
	
	private static SerialManager instance;
	private static boolean loaded = false;
	private SerialManager() {
		if(!loaded) {
			loaded = true;
			
			try {
				NativeResource nr = new NativeResource();
				nr.load("libNRJavaSerial");
			} catch(Exception e) {
				 System.err.println("Exception caught while trying to load NativeResource: " + e);
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
