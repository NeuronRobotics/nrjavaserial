package gnu.io;

public class SerialManager {
	
	private static SerialManager instance;
	private static boolean loaded = false;
	private SerialManager() throws NativeResourceException {
		if(!loaded) {
			loaded = true;
			new NativeResource().load("libNRJavaSerial");	
		}
	}
	
	public static SerialManager getInstance() throws NativeResourceException {
		if(instance == null) {
			instance = new SerialManager();
		}		
		return instance;
	}
}
