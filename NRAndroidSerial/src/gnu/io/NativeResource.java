package gnu.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class NativeResource {
	public void load(String libraryName) {
		System.out.println("Loading the NRjavaSerial native lib...");
		if(System.getProperty(libraryName + ".userlib") != null) {
			try {
				if(System.getProperty(libraryName + ".userlib").equalsIgnoreCase("sys")) {
					System.loadLibrary(libraryName);
				} else {
					System.load(System.getProperty(libraryName + ".userlib"));
				}
				return;
			} catch (Exception e){
				throw new NativeResourceException("Unable to load native resource from given path.\n" + e.getLocalizedMessage());
			}
		}
		
		System.out.println("Attempting to load the native resource from Android..."+libraryName);
		System.loadLibrary(libraryName.substring(libraryName.indexOf("lib")+3));
		
	}

	private void loadLib(String name) {
		System.out.println("Loading NRjavaSerial from the jar...");
		try {
			InputStream resourceSource = locateResource(name);
			File resourceLocation = prepResourceLocation(name);
			copyResource(resourceSource, resourceLocation);
			loadResource(resourceLocation);
			
		} catch (IOException ex) {
			throw new NativeResourceException("Unable to load deployed native resource");
		}
	}
	
	private InputStream locateResource(String name) {
		name += OSUtil.getExtension();
		String file="";
		/*
		 * Unused on android
		if(OSUtil.isOSX()) {
			file="/native/osx/" + name;
		}else if(OSUtil.isWindows()) {
			if(OSUtil.is64Bit()){
				file="/native/windows/x86_64/" + name;
			}else {
				file="/native/windows/x86_32/" + name;
			}
		}else if(OSUtil.isLinux()) {
			if(OSUtil.isARM()) {
				if(OSUtil.isCortexA8())
					file = "/native/linux/ARM_A8/" + name;
				else
					file = "/native/linux/ARM/" + name;
			}else {
				if(OSUtil.is64Bit()) {
					file="/native/linux/x86_64/" + name;
				}else {
					file="/native/linux/x86_32/" + name;
				}
			}
		}
		*/
		file = "/native/android/libs/armeabi/" + name;
		System.out.println("Loading native file: "+file+" for os arch: "+OSUtil.getOsArch());
		InputStream s =getClass().getResourceAsStream(file);
		if(s==null)
			throw new NativeResourceException("Unable to load deployed native resource from Jar, resource not existant");
		return s;
	}
	
	private void loadResource(File resource) {
		System.load(resource.getAbsolutePath());
	}

	private void copyResource(InputStream io, File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		
		
		byte[] buf = new byte[256];
		int read = 0;
		while ((read = io.read(buf)) > 0) {
			fos.write(buf, 0, read);
		}
		fos.close();
		io.close();
	}

	private File prepResourceLocation(String fileName) {		
		String tmpDir = System.getProperty("java.io.tmpdir");
		if ((tmpDir == null) || (tmpDir.length() == 0)) {
			tmpDir = "tmp";
		}
		
		String displayName = new File(fileName).getName().split("\\.")[0];
		
		String user = System.getProperty("user.name");
		
		File fd = null;
		File dir = null;
		
		for(int i = 0; i < 10; i++) {
			dir = new File(tmpDir, displayName + "_" + user + "_" + (i));
			if (dir.exists()) {
				if (!dir.isDirectory()) {
					continue;
				}
				
				try {
					File[] files = dir.listFiles();
					for (int j = 0; j < files.length; j++) {
						if (!files[j].delete()) {
							continue;
						}
					}
				} catch (Throwable e) {
					
				}
			}
			
			if ((!dir.exists()) && (!dir.mkdirs())) {
				continue;
			}
			
			try {
				dir.deleteOnExit();
			} catch (Throwable e) {
				// Java 1.1 or J9
			}
			
			fd = new File(dir, fileName + OSUtil.getExtension());
			if ((fd.exists()) && (!fd.delete())) {
				continue;
			}
			
			try {
				if (!fd.createNewFile()) {
					continue;
				}
			} catch (IOException e) {
				continue;
			} catch (Throwable e) {
				// Java 1.1 or J9
			}
			
			break;
		}
		
		if(fd == null || !fd.canRead()) {
			throw new NativeResourceException("Unable to deploy native resource");
		}
		
		return fd;
	}
	
	private static class OSUtil {
		public static boolean is64Bit() {
			return getOsArch().startsWith("x86_64") || getOsArch().startsWith("amd64");
		}
		public static boolean isARM() {
			return getOsArch().startsWith("arm");
		}
		public static boolean isCortexA8(){
			if(isARM()){
				//TODO check for cortex a8 vs arm9 generic
				return true;
			}
			return false;
		}
		public static boolean isAndroid(){
			if(isARM()){
				//TODO check for cortex a8 vs arm9 generic
				return true;
			}
			return false;
		}
		public static boolean isWindows() {
			return getOsName().startsWith("Windows");
		}
		
		public static boolean isLinux() {
			return getOsName().startsWith("Linux");
		}
		
		public static boolean isOSX() {
			return getOsName().startsWith("Mac OS X");
		}
		
		public static String getExtension() {
			if(isWindows()) {
				return ".dll";
			}
			
			if(isLinux()) {
				return ".so";
			}
			
			if(isOSX()) {
				return ".jnilib";
			}
			
			return "";
		}
		
		public static String getOsName() {	
			return System.getProperty("os.name");
		}
		
		public static String getOsArch() {
			return System.getProperty("os.arch");
		}
		
		public static String getIdentifier() {
			return getOsName() + " : " + getOsArch();
		}
	}
}
