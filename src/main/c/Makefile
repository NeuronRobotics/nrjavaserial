# If your system has the OS X 10.5 SDK installed, we want to build against it in
# order to generate native libraries compatible with 10.5 and up.
#
# See https://stackoverflow.com/questions/5378518/ for installation help.
OSXSDKPREFIX = /Developer/SDKs/MacOSX10.5.sdk

OSXJAVAINCLUDE1 = -I/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Headers 
OSXJAVAINCLUDE2 = -I$(OSXSDKPREFIX)/System/Library/Frameworks/JavaVM.framework/Versions/A/Headers 
OSXJAVAINCLUDE4 = -I$(OSXSDKPREFIX)/usr/include 
OSXJAVAINCLUDE5 = -I./include/OSX/

OSXJAVAINCLUDE = $(OSXJAVAINCLUDE1) $(OSXJAVAINCLUDE2) $(OSXJAVAINCLUDE3) $(OSXJAVAINCLUDE4) $(OSXJAVAINCLUDE5)		
OSXINCLUDE=-I"./include" -I"./include/target"  $(OSXJAVAINCLUDE)
OSXARCH = -arch i386 -arch x86_64
CCOSX=cc $(OSXINCLUDE) $(OSXARCH) -g -O2 -D_BSD_SOURCE -c
LINKOSX=cc $(OSXARCH) -dynamiclib -framework JavaVM -framework IOKit -framework CoreFoundation


LINOBJ=build/fixup.o build/fuserImp.o build/SerialImp.o
LININCLUDE=-I/usr/lib/jvm/java-7-openjdk-amd64/include/ -I/usr/lib/jvm/java-7-openjdk-i386/include/ -I"./include" -I"./include/target" -I/usr/lib/jvm/java-6-openjdk-amd64/include/ -I/usr/lib/jvm/java-6-openjdk-armhf/include/ -I/usr/lib/jvm/java-6-openjdk-arm/include/ -I/usr/lib/jvm/java-6-openjdk-i386/include/ -I/etc/alternatives/java_sdk/include -I/etc/alternatives/java_sdk/include/linux

CCLIN32=gcc $(LININCLUDE) -DLIBLOCKDEV -O3 -Wall -c -fmessage-length=0 -fPIC -m32 -MMD
LINKLIN32=g++ -m32 -shared


CCLIN64=gcc $(LININCLUDE) -DLIBLOCKDEV -O3 -Wall -c -fmessage-length=0 -fPIC -m64 -MMD
LINKLIN64=g++ -m64 -shared

CCLINARM=arm-linux-gnueabi-gcc $(LININCLUDE) -DLIBLOCKDEV -O3 -Wall -c -fmessage-length=0 -fPIC -MMD -MP
LINKLINARM=arm-linux-gnueabi-g++ -shared -fPIC -nostdlib -Lcross-compile-libs/ARM

CCLINARM_HF=arm-linux-gnueabihf-gcc $(LININCLUDE) -DLIBLOCKDEV -O3 -Wall -c -fmessage-length=0 -fPIC -MMD -MP
LINKLINARM_HF=arm-linux-gnueabihf-g++ -shared -fPIC -nostdlib -Lcross-compile-libs/ARM

CCLINANDROID=/home/opt/android-ndk-r5b/toolchains/arm-linux-androideabi-4.4.3/prebuilt/linux-x86/arm-linux-androideabi/bin/gcc $(LININCLUDE) -O3 -Wall -c -fmessage-length=0 -fPIC -MMD -MMD -MP
LINKLINANDROID=/home/opt/android-ndk-r5b/toolchains/arm-linux-androideabi-4.4.3/prebuilt/linux-x86/arm-linux-androideabi/bin/g++ -shared

#powerpc
CCLINPPC=/usr/local/powerpc-none-linux-gnuspe/bin/powerpc-none-linux-gnuspe-gcc $(LININCLUDE) -O3 -Wall -c -fmessage-length=0 -fPIC -MMD -MMD -MP
LINKLINPPC=/usr/local/powerpc-none-linux-gnuspe/bin/powerpc-none-linux-gnuspe-g++ -shared

BSDOBJ=build/fixup.o build/fuserImp.o build/SerialImp.o
BSDINCLUDE=-I"./include" -I"./include/target" -I/usr/local/openjdk8/include/ -I/usr/local/openjdk8/include/freebsd/

CCBSD32=cc $(BSDINCLUDE) -O3 -Wall -c -fmessage-length=0 -fPIC -m32 -MMD
LINKBSD32=c++ -m32 -shared 


CCBSD64=cc $(BSDINCLUDE) -O3 -Wall -c -fmessage-length=0 -fPIC -m64 -MMD
LINKBSD64=c++ -m64 -shared 


#Wine Windows
WINLINKOPT = -shared -Wl,--add-stdcall-alias -DBUILD_DLL
WINCCOPT = -O3 -Wall -DBUILD_DLL
#JDKDIR = C:\Program Files\Java\jdk1.6.0_24\include
JDKDIR = D:/java-32/jdk1.7.0_25/include

WININCLUDE32=-I".\include" -I".\include\target" -I".\include\windows" -I"C:\MinGW\include" -I"$(JDKDIR)" -I"$(JDKDIR)\win32"
CCWIN32 = wine "C:\MinGW\bin\gcc.exe" $(WININCLUDE32) -m32 $(WINCCOPT)
LINKWIN32 =wine "C:\MinGW\bin\gcc.exe" $(WINLINKOPT)

WININCLUDE64=-I".\include" -I".\include\target" -I".\include\windows" -I"C:\MinGW64\include" -I"$(JDKDIR)" -I"$(JDKDIR)\win32"
CCWIN64 = wine "C:\MinGW64\bin\gcc.exe" $(WININCLUDE64) -m64 $(WINCCOPT)
LINKWIN64 =wine "C:\MinGW64\bin\gcc.exe" $(WINLINKOPT)



#Native Windows
# MINGW32 = C:\MinGW32
# MINGW64 = C:\MinGW64
MINGW32 = C:\TDM-GCC-32
MINGW64 = C:\TDM-GCC-64

WININCLUDE32N=-I".\include" -I".\include\target" -I".\include\windows" -I"$(MINGW32)\include" -I"$(JDKDIR)" -I"$(JDKDIR)/win32"
CCWIN32N = "$(MINGW32)\bin\gcc.exe" $(WININCLUDE32N) -m32 $(WINCCOPT)
LINKWIN32N ="$(MINGW32)\bin\gcc.exe" $(WINLINKOPT)

WININCLUDE64N=-I".\include" -I".\include\target" -I".\include\windows" -I"$(MINGW64)\include" -I"$(JDKDIR)" -I"$(JDKDIR)/win32"
CCWIN64N = "$(MINGW64)\bin\gcc.exe" $(WININCLUDE64N) -m64 $(WINCCOPT)
LINKWIN64N ="$(MINGW64)\bin\gcc.exe" $(WINLINKOPT)

all:
	echo "Specify a system: make linux, make windows, make osx"
dirs:
	if (test -d build); then rm -rf build;mkdir build;else mkdir build; fi
	if (test -d resources/native);\
	 then echo Native output directories seem to exist.;else\
	 mkdir -p resources/native/linux/x86_32/;\
	 mkdir -p resources/native/linux/x86_64/;\
	 mkdir -p resources/native/linux/ARM/; \
	 mkdir -p resources/native/linux/ARM_A8/; \
	 mkdir -p resources/native/linux/ARM_ANDROID/; \
	 mkdir -p resources/native/linux/PPC/; \
	 mkdir -p resources/native/windows/x86_32/; \
	 mkdir -p resources/native/windows/x86_64/; \
	 mkdir -p resources/native/osx/; \
	 fi
	if (test -d resources/native/freebsd);\
	 then echo "FreeBSD native dir exists"; else\
	 mkdir -p resources/native/freebsd/x86_64/; \
	 mkdir -p resources/native/freebsd/x86_32/; \
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
linux:linux32 linux64 arm armHF ppc
	echo all Linux ok!
linux32:dirs
	$(CCLIN32) src/fixup.c -o build/fixup.o
	$(CCLIN32) src/fuserImp.c -o build/fuserImp.o
	$(CCLIN32) src/SerialImp.c -o build/SerialImp.o
	#rm "resources/native/linux/x86_32/libNRJavaSerial.so"
	$(LINKLIN32) -o"resources/native/linux/x86_32/libNRJavaSerial.so" $(LINOBJ) -llockdev
	rm build/*
linux64:dirs	
	$(CCLIN64) src/fixup.c -o build/fixup.o
	$(CCLIN64) src/fuserImp.c -o build/fuserImp.o
	$(CCLIN64) src/SerialImp.c -o build/SerialImp.o
	#rm "resources/native/linux/x86_64/libNRJavaSerial.so"
	$(LINKLIN64) -o"resources/native/linux/x86_64/libNRJavaSerial.so" $(LINOBJ) -llockdev
	rm -rf build
freebsd:freebsd32 freebsd64
	echo all FreeBSD ok!
freebsd32:dirs
	$(CCBSD32) src/fixup.c -o build/fixup.o
	$(CCBSD32) src/fuserImp.c -o build/fuserImp.o
	$(CCBSD32) src/SerialImp.c -o build/SerialImp.o
	#rm "resources/native/freebsd/x86_32/libNRJavaSerial.so"
	$(LINKBSD32) -o"resources/native/freebsd/x86_32/libNRJavaSerial.so" $(LINOBJ)
	rm build/*
freebsd64:dirs	
	$(CCBSD64) src/fixup.c -o build/fixup.o
	$(CCBSD64) src/fuserImp.c -o build/fuserImp.o
	$(CCBSD64) src/SerialImp.c -o build/SerialImp.o
	#rm "resources/native/freebsd/x86_64/libNRJavaSerial.so"
	$(LINKBSD64) -o"resources/native/freebsd/x86_64/libNRJavaSerial.so" $(LINOBJ)
	rm -rf build	
	
.PHONY: arm
arm:
	$(MAKE) arm8
	$(MAKE) arm8HF
	$(MAKE) arm7
	$(MAKE) arm7HF
	$(MAKE) arm6
	$(MAKE) arm6HF
	$(MAKE) arm5
	@echo all ARM ok!

arm8:	dirs
	$(CCLINARM) -march=armv8-a src/fixup.c -o build/fixup.o
	$(CCLINARM) -march=armv8-a src/fuserImp.c -o build/fuserImp.o
	$(CCLINARM) -march=armv8-a src/SerialImp.c -o build/SerialImp.o
	$(LINKLINARM) -march=armv8-a -o"resources/native/linux/ARM/libNRJavaSerialv8.so" $(LINOBJ) -llockdev
	rm -rf build
	
arm8HF:dirs
	$(CCLINARM_HF) -march=armv8-a src/fixup.c -o build/fixup.o
	$(CCLINARM_HF) -march=armv8-a src/fuserImp.c -o build/fuserImp.o
	$(CCLINARM_HF) -march=armv8-a src/SerialImp.c -o build/SerialImp.o
	$(LINKLINARM_HF) -march=armv8-a -o"resources/native/linux/ARM/libNRJavaSerialv8_HF.so" $(LINOBJ) -llockdev
	rm  build/*	

arm7:	dirs
	$(CCLINARM) -march=armv7-a src/fixup.c -o build/fixup.o
	$(CCLINARM) -march=armv7-a src/fuserImp.c -o build/fuserImp.o
	$(CCLINARM) -march=armv7-a src/SerialImp.c -o build/SerialImp.o
	$(LINKLINARM) -march=armv7-a -o"resources/native/linux/ARM/libNRJavaSerialv7.so" $(LINOBJ) -llockdev
	rm -rf build
	
arm7HF:dirs
	$(CCLINARM_HF) -march=armv7-a src/fixup.c -o build/fixup.o
	$(CCLINARM_HF) -march=armv7-a src/fuserImp.c -o build/fuserImp.o
	$(CCLINARM_HF) -march=armv7-a src/SerialImp.c -o build/SerialImp.o
	$(LINKLINARM_HF) -march=armv7-a -o"resources/native/linux/ARM/libNRJavaSerialv7_HF.so" $(LINOBJ) -llockdev
	rm  build/*	
	
arm6:	dirs
	$(CCLINARM) -march=armv6 src/fixup.c -o build/fixup.o
	$(CCLINARM) -march=armv6 src/fuserImp.c -o build/fuserImp.o
	$(CCLINARM) -march=armv6 src/SerialImp.c -o build/SerialImp.o
	$(LINKLINARM) -march=armv6 -o"resources/native/linux/ARM/libNRJavaSerialv6.so" $(LINOBJ) -llockdev
	rm -rf build
arm6HF:dirs
	$(CCLINARM_HF) -march=armv6 -mfpu=vfp -marm src/fixup.c -o build/fixup.o
	$(CCLINARM_HF) -march=armv6 -mfpu=vfp -marm src/fuserImp.c -o build/fuserImp.o
	$(CCLINARM_HF) -march=armv6 -mfpu=vfp -marm src/SerialImp.c -o build/SerialImp.o
	$(LINKLINARM_HF) -march=armv6 -mfpu=vfp -marm -o"resources/native/linux/ARM/libNRJavaSerialv6_HF.so" $(LINOBJ) -llockdev
	rm  build/*	

arm5:	dirs
	$(CCLINARM) -march=armv5t src/fixup.c -o build/fixup.o
	$(CCLINARM) -march=armv5t src/fuserImp.c -o build/fuserImp.o
	$(CCLINARM) -march=armv5t src/SerialImp.c -o build/SerialImp.o
	$(LINKLINARM) -march=armv5t -o"resources/native/linux/ARM/libNRJavaSerialv5.so" $(LINOBJ) -llockdev
	rm -rf build

ppc:   dirs
	$(CCLINPPC) src/fixup.c -o build/fixup.o
	$(CCLINPPC) src/fuserImp.c -o build/fuserImp.o
	$(CCLINPPC) src/SerialImp.c -o build/SerialImp.o
	$(LINKLINPPC) -o"resources/native/linux/PPC/libNRJavaSerial.so" $(LINOBJ)
	rm -rf build
	
osx:dirs
	if (test -d include/OSX/CoreFoundation/);\
	then\
		echo "CoreFoundation framework symlink exists.";\
	else\
		rm include/OSX/CoreFoundation;\
		ln -s $(OSXSDKPREFIX)/System/Library/Frameworks/CoreFoundation.framework/Headers/ include/OSX/CoreFoundation; \
	fi
	
	if (test -d include/OSX/CoreServices/);\
	then\
		echo "CoreServices framework symlink exists.";\
	else\
		rm include/OSX/CoreServices;\
		ln -s $(OSXSDKPREFIX)/System/Library/Frameworks/CoreServices.framework/Headers/ include/OSX/CoreServices; \
	fi
	
	if (test -d include/OSX/CarbonCore/);\
	then\
		echo "CarbonCore framework symlink exists.";\
	else\
		rm include/OSX/CarbonCore;\
		ln -s $(OSXSDKPREFIX)/System/Library/Frameworks/CoreServices.framework/Versions/A/Frameworks/CarbonCore.framework/Versions/A/Headers include/OSX/CarbonCore; \
	fi
	
	if (test -d include/OSX/IOKit/);\
	then\
		echo "IOKit framework symlink exists";\
	else\
		rm include/OSX/IOKit;\
		ln -s $(OSXSDKPREFIX)/System/Library/Frameworks/IOKit.framework/Headers include/OSX/IOKit; \
	fi
	
	$(CCOSX) src/fuserImp.c -o build/fuserImp.o
	$(CCOSX) src/SerialImp.c -o build/SerialImp.o
	$(LINKOSX) -o "./resources/native/osx/libNRJavaSerial.jnilib" build/SerialImp.o build/fuserImp.o
	rm -rf build	
	
