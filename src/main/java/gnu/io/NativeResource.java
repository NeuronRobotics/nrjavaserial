/*-------------------------------------------------------------------------
|   RXTX License v 2.1 - LGPL v 2.1 + Linking Over Controlled Interface.
|   RXTX is a native interface to serial ports in java.
|   Copyright 1997-2007 by Trent Jarvi tjarvi@qbang.org and others who
|   actually wrote it.  See individual source files for more information.
|
|   A copy of the LGPL v 2.1 may be found at
|   http://www.gnu.org/licenses/lgpl.txt on March 4th 2007.  A copy is
|   here for your convenience.
|
|   This library is free software; you can redistribute it and/or
|   modify it under the terms of the GNU Lesser General Public
|   License as published by the Free Software Foundation; either
|   version 2.1 of the License, or (at your option) any later version.
|
|   This library is distributed in the hope that it will be useful,
|   but WITHOUT ANY WARRANTY; without even the implied warranty of
|   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
|   Lesser General Public License for more details.
|
|   An executable that contains no derivative of any portion of RXTX, but
|   is designed to work with RXTX by being dynamically linked with it,
|   is considered a "work that uses the Library" subject to the terms and
|   conditions of the GNU Lesser General Public License.
|
|   The following has been added to the RXTX License to remove
|   any confusion about linking to RXTX.   We want to allow in part what
|   section 5, paragraph 2 of the LGPL does not permit in the special
|   case of linking over a controlled interface.  The intent is to add a
|   Java Specification Request or standards body defined interface in the 
|   future as another exception but one is not currently available.
|
|   http://www.fsf.org/licenses/gpl-faq.html#LinkingOverControlledInterface
|
|   As a special exception, the copyright holders of RXTX give you
|   permission to link RXTX with independent modules that communicate with
|   RXTX solely through the Sun Microsytems CommAPI interface version 2,
|   regardless of the license terms of these independent modules, and to copy
|   and distribute the resulting combined work under terms of your choice,
|   provided that every copy of the combined work is accompanied by a complete
|   copy of the source code of RXTX (the version of RXTX used to produce the
|   combined work), being distributed under the terms of the GNU Lesser General
|   Public License plus this exception.  An independent module is a
|   module which is not derived from or based on RXTX.
|
|   Note that people who make modified versions of RXTX are not obligated
|   to grant this special exception for their modified versions; it is
|   their choice whether to do so.  The GNU Lesser General Public License
|   gives permission to release a modified version without this exception; this
|   exception also makes it possible to release a modified version which
|   carries forward this exception.
|
|   You should have received a copy of the GNU Lesser General Public
|   License along with this library; if not, write to the Free
|   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
|   All trademarks belong to their respective owners.
--------------------------------------------------------------------------*/
package gnu.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class NativeResource {

	private boolean loaded = false;
	public synchronized void load(String libraryName) throws NativeResourceException {
		if(loaded) {
			return;
		}
		loaded = true;
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

	private void inJarLoad(String name)throws UnsatisfiedLinkError, NativeResourceException{
		//start by assuming the library can be loaded from the jar
		InputStream resourceSource = locateResource(name);
		File resourceLocation = prepResourceLocation(name);
		try {
			copyResource(resourceSource, resourceLocation);
		} catch (IOException e) {
			throw new UnsatisfiedLinkError();
		}
		loadResource(resourceLocation);
		testNativeCode();
	}

	private static final String[] ARM32_LIBS = {"libNRJavaSerialv8_HF","libNRJavaSerialv8","libNRJavaSerialv7_HF","libNRJavaSerialv7","libNRJavaSerialv6_HF","libNRJavaSerialv6","libNRJavaSerialv5"};
	private static final String[] ARM64_LIBS = {"libNRJavaSerialv8"};

	private void loadLib(String name) throws NativeResourceException {
		try {
			if(OSUtil.isARM()) {
				//System.err.println("Attempting arm variants");
				for(String libName : OSUtil.is64Bit() ? ARM64_LIBS : ARM32_LIBS) {
					try {
						inJarLoad(libName);
						//System.err.println("Arm lib success! "+libName);
						return;
					}catch(UnsatisfiedLinkError e) {
						//System.err.println("Is not "+libName);
					}
				}
			}else {
				inJarLoad(name);
			}
			return;
		} catch (UnsatisfiedLinkError ex) {
			if(OSUtil.isOSX() || OSUtil.isLinux()){
				try{
					inJarLoad("libNRJavaSerial_legacy");
					//System.err.println("Normal lib failed, using legacy..OK!");
					return;
				}catch(UnsatisfiedLinkError er){
					ex.printStackTrace();
				}
			}else{
				ex.printStackTrace();
			}
			try{
				//check to see if the library is available in standard locations
				String libName = name.substring(name.indexOf("lib")+3);
				System.loadLibrary(libName);
				testNativeCode();
				return;
			}catch(UnsatisfiedLinkError e){
				try{
					name = "rxtxSerial";
					//last ditch effort to load
					System.loadLibrary(name);
					testNativeCode();
					return;
				}catch(UnsatisfiedLinkError err){
					//System.err.println("Failed to load all possible JNI local and from: \n"+System.getProperty("java.library.path"));
					ex.printStackTrace();
					throw new NativeResourceException("Unable to load deployed native resource");
				}
			}

		}
	}

	private void testNativeCode()throws UnsatisfiedLinkError {
		CommPortIdentifier.getPortIdentifiers();
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
				if(OSUtil.is64Bit()) {
					file="/native/linux/ARM_64/" + name;
				}else {
					file="/native/linux/ARM_32/" + name;
				}
			}else if(OSUtil.isPPC()) {
				file = "/native/linux/PPC/" + name;
			}else {
				if(OSUtil.is64Bit()) {
					file="/native/linux/x86_64/" + name;
				}else {
					file="/native/linux/x86_32/" + name;
				}
			}
		}else if(OSUtil.isFreeBSD()) {
			if(OSUtil.is64Bit()) {
				file="/native/freebsd/x86_64/" + name;
			}else {
				file="/native/freebsd/x86_32/" + name;
			}
		}else{
			//System.err.println("Can't load native file: "+name+" for os arch: "+OSUtil.getOsArch());
			return null;
		}
		//System.out.println("Loading "+file);
		return getClass().getResourceAsStream(file);
	}

	private void loadResource(File resource) {
		if(!resource.canRead()) {
			throw new RuntimeException("Cant open JNI file: "+resource.getAbsolutePath());
		}
		//System.out.println("Loading: "+resource.getAbsolutePath());
		try {
			System.load(resource.getAbsolutePath());
		} catch(UnsatisfiedLinkError e){
			System.out.println(e.getMessage());
			throw e;
		}
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

	private File prepResourceLocation(String fileName) throws NativeResourceException {
		String tmpDir = System.getProperty("java.io.tmpdir");
		//String tmpDir = "M:\\";
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
		//System.out.println("Local file: "+fd.getAbsolutePath());
		return fd;
	}

	private static class OSUtil {
		public static boolean is64Bit() {
			////System.out.println("Arch: "+getOsArch());
			return getOsArch().startsWith("x86_64") || getOsArch().startsWith("amd64")  || getOsArch().startsWith("aarch64");
		}
		public static boolean isARM() {
			return getOsArch().startsWith("arm") || getOsArch().startsWith("aarch");
		}
		public static boolean isPPC() {
			return getOsArch().toLowerCase().contains("ppc");
		}
		public static boolean isWindows() {
			////System.out.println("OS name: "+getOsName());
			return getOsName().toLowerCase().startsWith("windows") ||getOsName().toLowerCase().startsWith("microsoft") || getOsName().toLowerCase().startsWith("ms");
		}

		public static boolean isLinux() {
			return getOsName().toLowerCase().startsWith("linux");
		}

		public static boolean isFreeBSD() {
			return getOsName().toLowerCase().startsWith("freebsd");
		}

		public static boolean isOSX() {
			return getOsName().toLowerCase().startsWith("mac");
		}

		public static String getExtension() {
			if(isWindows()) {
				return ".dll";
			}

			if(isLinux() || isFreeBSD()) {
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

		@SuppressWarnings("unused")
		public static String getIdentifier() {
			return getOsName() + " : " + getOsArch();
		}
	}
}
