package gnu.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class NativeResource {
	public void load(String libraryName) {		
		if(System.getProperty(libraryName + ".userlib") != null) {
			try {
				if(System.getProperty(libraryName + ".userlib").equalsIgnoreCase("sys")) {
					System.loadLibrary(libraryName);
				} else {
					System.load(System.getProperty(libraryName + ".userlib"));
				}
				return;
			} catch (Exception e){
				e.printStackTrace();
				throw new NativeResourceException("Unable to load native resource from given path.\n" + e.getLocalizedMessage());
			}
		}
		loadLib(libraryName);	
	}

	private void loadLib(String name) {
		try {
			//System.out.println("Locating resource");
			InputStream resourceSource = locateResource(name);
			//System.out.println("Preping resource location");
			File resourceLocation = prepResourceLocation(name);
			//System.out.println("Copying resource");
			copyResource(resourceSource, resourceLocation);
			//System.out.println("Loading resource");
			loadResource(resourceLocation);
			System.out.println("Native Library Loaded");
			RXTXCommDriver.nativeGetVersion();
			System.out.println("JNI test ok");
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new NativeResourceException("Unable to load deployed native resource");
		}
	}
	
	private InputStream locateResource(String name) {
		name += OSUtil.getExtension();
		String file="";
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
		}else{
			System.err.println("Can't load native file: "+name+" for os arch: "+OSUtil.getOsArch());
			return null;
		}
		System.out.println("Loading "+file);
		return getClass().getResourceAsStream(file);
	}
	
	private void loadResource(File resource) {
		if(!resource.canRead())
			throw new RuntimeException("Cant open JNI file: "+resource.getAbsolutePath());
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
		System.out.println("Local file: "+fd.getAbsolutePath());
		return fd;
	}
	
	private static class OSUtil {
		public static boolean is64Bit() {
			//System.out.println("Arch: "+getOsArch());
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
		public static boolean isWindows() {
			//System.out.println("OS name: "+getOsName());
			return getOsName().toLowerCase().startsWith("windows") ||getOsName().toLowerCase().startsWith("microsoft") || getOsName().toLowerCase().startsWith("ms");
		}
		
		public static boolean isLinux() {
			return getOsName().toLowerCase().startsWith("linux");
		}
		
		public static boolean isOSX() {
			return getOsName().toLowerCase().startsWith("mac");
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
