
LINOBJ=build/fixup.o build/fuserImp.o build/SerialImp.o
LININCLUDE=-I"./include" -I"./include/target" -I/usr/src/linux-headers-2.6.32-26-generic/include/ -I/usr/lib/jvm/java-6-openjdk/include/

CCLIN32=gcc $(LININCLUDE) -O3 -Wall -c -fmessage-length=0 -fPIC -m32 -MMD
LINKLIN32=g++ -m32 -shared 


CCLIN64=gcc $(LININCLUDE) -O3 -Wall -c -fmessage-length=0 -fPIC -m64 -MMD
LINKLIN64=g++ -m64 -shared 

CCLINARM=/usr/arm-linux/bin/gcc $(LININCLUDE) -O3 -Wall -c -fmessage-length=0 -fPIC -MMD -MMD -MP
LINKLINARM=/usr/arm-linux/bin/g++ -shared

CCLINA8=~/CodeSourcery/Sourcery_G++_Lite/bin/arm-none-linux-gnueabi-gcc $(LININCLUDE) -O3 -Wall -c -fmessage-length=0 -fPIC -MMD -MMD -MP
LINKLINA8=~/CodeSourcery/Sourcery_G++_Lite/bin/arm-none-linux-gnueabi-g++ -shared

CCLINANDROID=~/Desktop/android-ndk-r5b/toolchains/arm-linux-androideabi-4.4.3/prebuilt/linux-x86/arm-linux-androideabi/bin/gcc $(LININCLUDE) -O3 -Wall -c -fmessage-length=0 -fPIC -MMD -MMD -MP
LINKLINANDROID=~/Desktop/android-ndk-r5b/toolchains/arm-linux-androideabi-4.4.3/prebuilt/linux-x86/arm-linux-androideabi/bin/g++ -shared

#Wine Windows
WININCLUDE32=-I".\include" -I".\include\target" -I".\include\windows" -I"C:\MinGW\include" -I"C:\Program Files\Java\jdk1.6.0_22\include\win32" -I"C:\Program Files\Java\jdk1.6.0_22\include"
CCWIN32 = wine "C:\MinGW\bin\gcc.exe" $(WININCLUDE32) -O3 -Wall -c -DBUILD_DLL
LINKWIN32 =wine "C:\MinGW\bin\gcc.exe" -shared -Wl,--out-implib,librxtxSerial.a

CCWIN64 = wine "C:\MinGW\bin\gcc.exe" $(WININCLUDE32)  -m64 -O3 -Wall -c -DBUILD_DLL
LINKWIN64 =wine "C:\MinGW\bin\gcc.exe" -shared -m64 -Wl,--out-implib,librxtxSerial.a

#Native Windows
WININCLUDE32N=-I".\include" -I".\include\target" -I".\include\windows" -I"C:\MinGW\include" -I"C:\Program Files\Java\jdk1.6.0_22\include\win32" -I"C:\Program Files\Java\jdk1.6.0_22\include"
CCWIN32N = "C:\MinGW\bin\gcc.exe" $(WININCLUDE32N) -O3 -Wall -c -DBUILD_DLL
LINKWIN32N ="C:\MinGW\bin\gcc.exe" -shared -Wl,--out-implib,librxtxSerial.a

WININCLUDE64N=-I".\include" -I".\include\target" -I".\include\windows" -I"C:\MinGW64\include" -I"C:\Program Files\Java\jdk1.6.0_22\include\win32" -I"C:\Program Files\Java\jdk1.6.0_22\include"
CCWIN64N = "C:\MinGW64\bin\gcc.exe" $(WININCLUDE64N) -m64 -O3 -Wall -c -DBUILD_DLL
LINKWIN64N ="C:\MinGW64\bin\gcc.exe" -shared -m64 -Wl,--out-implib,librxtxSerial.a

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
	$(CCWIN32N) src/windows/init.c -o build/init.o
	$(CCWIN32N) src/windows/termios.c -o build/termios.o
	$(CCWIN32N) src/fixup.c -o build/fixup.o
	$(CCWIN32N) src/fuserImp.c -o build/fuserImp.o
	$(CCWIN32N) src/SerialImp.c -o build/SerialImp.o
	$(LINKWIN32N) -o"resources/native/windows/x86_32/libNRJavaSerial.dll" build/SerialImp.o build/fuserImp.o build/fixup.o build/termios.o build/init.o
		
	$(CCWIN64N) src/windows/init.c -o build/init.o
	$(CCWIN64N) src/windows/termios.c -o build/termios.o
	$(CCWIN64N) src/fixup.c -o build/fixup.o
	$(CCWIN64N) src/fuserImp.c -o build/fuserImp.o
	$(CCWIN64N) src/SerialImp.c -o build/SerialImp.o
	$(LINKWIN64N) -o"resources/native/windows/x86_64/libNRJavaSerial.dll" build/SerialImp.o build/fuserImp.o build/fixup.o build/termios.o build/init.o
	
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
linux:dirs

	$(CCLIN32) src/fixup.c -o build/fixup.o
	$(CCLIN32) src/fuserImp.c -o build/fuserImp.o
	$(CCLIN32) src/SerialImp.c -o build/SerialImp.o
	$(LINKLIN32) -o"resources/native/linux/x86_32/libNRJavaSerial.so" $(LINOBJ)
	rm build/*
	
	$(CCLIN64) src/fixup.c -o build/fixup.o
	$(CCLIN64) src/fuserImp.c -o build/fuserImp.o
	$(CCLIN64) src/SerialImp.c -o build/SerialImp.o
	$(LINKLIN64) -o"resources/native/linux/x86_64/libNRJavaSerial.so" $(LINOBJ)
	rm -rf build
armChumby:	dirs
	$(CCLINARM) src/fixup.c -o build/fixup.o
	$(CCLINARM) src/fuserImp.c -o build/fuserImp.o
	$(CCLINARM) src/SerialImp.c -o build/SerialImp.o
	$(LINKLINARM) -o"resources/native/linux/ARM/libNRJavaSerial.so" $(LINOBJ)
	rm -rf build
armA8:dirs
	$(CCLINA8) src/fixup.c -o build/fixup.o
	$(CCLINA8) src/fuserImp.c -o build/fuserImp.o
	$(CCLINA8) src/SerialImp.c -o build/SerialImp.o
	$(LINKLINA8) -o"resources/native/linux/ARM_A8/libNRJavaSerial.so" $(LINOBJ)
	rm -rf build
	
armAndroid:dirs
	$(CCLINANDROID) src/fixup.c -o build/fixup.o
	$(CCLINANDROID) src/fuserImp.c -o build/fuserImp.o
	$(CCLINANDROID) src/SerialImp.c -o build/SerialImp.o
	$(LINKLINANDROID) -o"resources/native/linux/ARM_ANDROID/libNRJavaSerial.so" $(LINOBJ)
	rm -rf build	
	