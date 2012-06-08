
OSXJAVAINCLUDE1 = -I/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Headers 
OSXJAVAINCLUDE2 = -I/Developer/SDKs/MacOSX10.6.sdk/System/Library/Frameworks/JavaVM.framework/Versions/A/Headers 
OSXJAVAINCLUDE4 = -I/Developer/SDKs/MacOSX10.6.sdk/usr/include 
OSXJAVAINCLUDE5 = -I./include/OSX/

OSXJAVAINCLUDE = $(OSXJAVAINCLUDE1) $(OSXJAVAINCLUDE2) $(OSXJAVAINCLUDE3) $(OSXJAVAINCLUDE4) $(OSXJAVAINCLUDE5)		
OSXINCLUDE=-I"./include" -I"./include/target"  $(OSXJAVAINCLUDE)
CCOSX=cc $(OSXINCLUDE)  -g -O2 -D_BSD_SOURCE -c
LINKOSX=cc  -dynamiclib -framework JavaVM -framework IOKit -framework CoreFoundation


LINOBJ=build/fixup.o build/fuserImp.o build/SerialImp.o
LININCLUDE=-I"./include" -I"./include/target" -I/usr/lib/jvm/java-6-openjdk-amd64/include/ -I/usr/lib/jvm/java-6-openjdk-armhf/include/ -I/usr/lib/jvm/java-6-openjdk-arm/include/ -I/usr/lib/jvm/java-6-openjdk-i386/include/

CCLIN32=gcc $(LININCLUDE) -O3 -Wall -c -fmessage-length=0 -fPIC -m32 -MMD
LINKLIN32=g++ -m32 -shared 


CCLIN64=gcc $(LININCLUDE) -O3 -Wall -c -fmessage-length=0 -fPIC -m64 -MMD
LINKLIN64=g++ -m64 -shared 

CCLINARM=arm-linux-gnueabi-gcc $(LININCLUDE) -O3 -Wall -c -fmessage-length=0 -fPIC -MMD -MMD -MP
LINKLINARM=arm-linux-gnueabi-g++ -shared

CCLINARM_HF=arm-linux-gnueabihf-gcc $(LININCLUDE) -O3 -Wall -c -fmessage-length=0 -fPIC -MMD -MMD -MP
LINKLINARM_HF=arm-linux-gnueabihf-g++ -shared

CCLINANDROID=/home/opt/android-ndk-r5b/toolchains/arm-linux-androideabi-4.4.3/prebuilt/linux-x86/arm-linux-androideabi/bin/gcc $(LININCLUDE) -O3 -Wall -c -fmessage-length=0 -fPIC -MMD -MMD -MP
LINKLINANDROID=/home/opt/android-ndk-r5b/toolchains/arm-linux-androideabi-4.4.3/prebuilt/linux-x86/arm-linux-androideabi/bin/g++ -shared

#Wine Windows
WINLINKOPT = -shared -Wl,--add-stdcall-alias -DBUILD_DLL
WINCCOPT = -O3 -Wall -DBUILD_DLL
JDKDIR = C:\Program Files\Java\jdk1.6.0_24\include

WININCLUDE32=-I".\include" -I".\include\target" -I".\include\windows" -I"C:\MinGW\include" -I"$(JDKDIR)" -I"$(JDKDIR)\win32"
CCWIN32 = wine "C:\MinGW\bin\gcc.exe" $(WININCLUDE32) -m32 $(WINCCOPT)
LINKWIN32 =wine "C:\MinGW\bin\gcc.exe" $(WINLINKOPT)

WININCLUDE64=-I".\include" -I".\include\target" -I".\include\windows" -I"C:\MinGW64\include" -I"$(JDKDIR)" -I"$(JDKDIR)\win32"
CCWIN64 = wine "C:\MinGW64\bin\gcc.exe" $(WININCLUDE64) -m64 $(WINCCOPT)
LINKWIN64 =wine "C:\MinGW64\bin\gcc.exe" $(WINLINKOPT)

#Native Windows

WININCLUDE32N=-I".\include" -I".\include\target" -I".\include\windows" -I"C:\MinGW32\include" -I"$(JDKDIR)" -I"$(JDKDIR)\win32"
CCWIN32N = "C:\MinGW32\bin\gcc.exe" $(WININCLUDE32N) -m32 $(WINCCOPT)
LINKWIN32N ="C:\MinGW32\bin\gcc.exe" $(WINLINKOPT)

WININCLUDE64N=-I".\include" -I".\include\target" -I".\include\windows" -I"C:\MinGW64\include" -I"$(JDKDIR)" -I"$(JDKDIR)\win32"
CCWIN64N = "C:\MinGW64\bin\gcc.exe" $(WININCLUDE64N) -m64 $(WINCCOPT)
LINKWIN64N ="C:\MinGW64\bin\gcc.exe" $(WINLINKOPT)

all:
	echo "Specify a system: make linux, make windows, make osx"
dirs:
	if (test -d build); then rm -rf build;mkdir build;else mkdir build; fi
	if (test -d resources/native);\
	 then echo dirs ok;else\
	 mkdir -p resources/native/linux/x86_32/;\
	 mkdir -p resources/native/linux/x86_64/;\
	 mkdir -p resources/native/linux/ARM/; \
	 mkdir -p resources/native/linux/ARM_A8/; \
	 mkdir -p resources/native/linux/ARM_ANDROID/; \
	 mkdir -p resources/native/windows/x86_32/; \
	 mkdir -p resources/native/windows/x86_64/; \
	 mkdir -p resources/native/osx/; \
	 fi
	 
windowsLocal:
	$(CCWIN64N) -c src/windows/init.c -o build/init.o
	$(CCWIN64N) -c src/windows/termios.c -o build/termios.o
	$(CCWIN64N) -c src/fixup.c -o build/fixup.o
	$(CCWIN64N) -c src/fuserImp.c -o build/fuserImp.o
	$(CCWIN64N) -c src/SerialImp.c -o build/SerialImp.o
	$(LINKWIN64N) -o"resources/native/windows/x86_64/libNRJavaSerial.dll" build/SerialImp.o build/fuserImp.o build/fixup.o build/termios.o build/init.o
	
	$(CCWIN32N) -c src/windows/init.c -o build/init.o
	$(CCWIN32N) -c src/windows/termios.c -o build/termios.o
	$(CCWIN32N) -c src/fixup.c -o build/fixup.o
	$(CCWIN32N) -c src/fuserImp.c -o build/fuserImp.o
	$(CCWIN32N) -c src/SerialImp.c -o build/SerialImp.o
	$(LINKWIN32N) -o"resources/native/windows/x86_32/libNRJavaSerial.dll" build/SerialImp.o build/fuserImp.o build/fixup.o build/termios.o build/init.o
		

	
windows:dirs
	export WINEPREFIX=/home/hephaestus/.wine/
	$(CCWIN32) src/windows/init.c -o build/init.o
	$(CCWIN32) src/windows/termios.c -o build/termios.o
	$(CCWIN32) src/fixup.c -o build/fixup.o
	$(CCWIN32) src/fuserImp.c -o build/fuserImp.o
	$(CCWIN32) src/SerialImp.c -o build/SerialImp.o
	$(LINKWIN32) -o"resources/native/windows/x86_32/libNRJavaSerial.dll" build/SerialImp.o build/fuserImp.o build/fixup.o build/termios.o build/init.o
	rm build/*
	
	#$(CCWIN64) src/windows/init.c -o build/init.o
	#$(CCWIN64) src/windows/termios.c -o build/termios.o
	#$(CCWIN64) src/fixup.c -o build/fixup.o
	#$(CCWIN64) src/fuserImp.c -o build/fuserImp.o
	#$(CCWIN64) src/SerialImp.c -o build/SerialImp.o
	#$(LINKWIN64) -o"resources/native/windows/x86_64/libNRJavaSerial.dll" build/SerialImp.o build/fuserImp.o build/fixup.o build/termios.o build/init.o
	#rm build/*
	rm -rf build
linux:linux32 linux64 arm armHF
	echo all Linux ok!
linux32:dirs
	$(CCLIN32) src/fixup.c -o build/fixup.o
	$(CCLIN32) src/fuserImp.c -o build/fuserImp.o
	$(CCLIN32) src/SerialImp.c -o build/SerialImp.o
	#rm "resources/native/linux/x86_32/libNRJavaSerial.so"
	$(LINKLIN32) -o"resources/native/linux/x86_32/libNRJavaSerial.so" $(LINOBJ)
	rm build/*
linux64:dirs	
	$(CCLIN64) src/fixup.c -o build/fixup.o
	$(CCLIN64) src/fuserImp.c -o build/fuserImp.o
	$(CCLIN64) src/SerialImp.c -o build/SerialImp.o
	#rm "resources/native/linux/x86_64/libNRJavaSerial.so"
	$(LINKLIN64) -o"resources/native/linux/x86_64/libNRJavaSerial.so" $(LINOBJ)
	rm -rf build
arm7:	dirs
	$(CCLINARM) --with-arch=armv7  src/fixup.c -o build/fixup.o
	$(CCLINARM) --with-arch=armv7  src/fuserImp.c -o build/fuserImp.o
	$(CCLINARM) --with-arch=armv7  src/SerialImp.c -o build/SerialImp.o
	$(LINKLINARM) --with-arch=armv7  -o"resources/native/linux/ARM/libNRJavaSerial.so" $(LINOBJ)
	rm -rf build
arm7HF:dirs
	$(CCLINARM_HF) --with-arch=armv7  src/fixup.c -o build/fixup.o
	$(CCLINARM_HF) --with-arch=armv7  src/fuserImp.c -o build/fuserImp.o
	$(CCLINARM_HF) --with-arch=armv7  src/SerialImp.c -o build/SerialImp.o
	$(LINKLINARM_HF) --with-arch=armv7 -o"resources/native/linux/ARM/libNRJavaSerial_HF.so" $(LINOBJ)
	rm  build/*	
	
arm6:	dirs
	$(CCLINARM) --with-arch=armv6  src/fixup.c -o build/fixup.o
	$(CCLINARM) --with-arch=armv6  src/fuserImp.c -o build/fuserImp.o
	$(CCLINARM) --with-arch=armv6  src/SerialImp.c -o build/SerialImp.o
	$(LINKLINARM) --with-arch=armv6  -o"resources/native/linux/ARM/libNRJavaSerialv6.so" $(LINOBJ)
	rm -rf build
arm6HF:dirs
	$(CCLINARM_HF)  src/fixup.c -o build/fixup.o
	$(CCLINARM_HF)  src/fuserImp.c -o build/fuserImp.o
	$(CCLINARM_HF)  src/SerialImp.c -o build/SerialImp.o
	$(LINKLINARM_HF)  -o"resources/native/linux/ARM/libNRJavaSerialv6_HF.so" $(LINOBJ)
	rm  build/*	

	
osx:dirs
	if (test -d include/OSX/CoreFoundation/);\
	then\
		echo Dirs OK!;\
	else\
		rm include/OSX/CoreFoundation;\
		sudo ln -s /Developer/SDKs/MacOSX10.6.sdk/System/Library/Frameworks/CoreFoundation.framework/Headers/ include/OSX/CoreFoundation; \
	fi
	
	if (test -d include/OSX/CoreServices/);\
	then\
		echo Dirs OK!;\
	else\
		rm include/OSX/CoreServices;\
		sudo ln -s /Developer/SDKs/MacOSX10.6.sdk/System/Library/Frameworks/CoreServices.framework/Headers/ include/OSX/CoreServices; \
	fi
	
	if (test -d include/OSX/CarbonCore/);\
	then\
		echo Dirs OK!;\
	else\
		rm include/OSX/CarbonCore;\
		sudo ln -s /Developer/SDKs/MacOSX10.6.sdk/System/Library/Frameworks/CoreServices.framework/Versions/A/Frameworks/CarbonCore.framework/Versions/A/Headers include/OSX/CarbonCore; \
	fi
	
	if (test -d include/OSX/IOKit/);\
	then\
		echo Dirs OK!;\
	else\
		rm include/OSX/CarbonCore;\
		sudo ln -s /Developer/SDKs/MacOSX10.6.sdk/System/Library/Frameworks/IOKit.framework/Headers include/OSX/IOKit; \
	fi
	
	$(CCOSX) src/fuserImp.c -o build/fuserImp.o
	$(CCOSX) src/SerialImp.c -o build/SerialImp.o
	$(LINKOSX) -o "./resources/native/osx/libNRJavaSerial.jnilib" build/SerialImp.o build/fuserImp.o
	rm -rf build	
	
