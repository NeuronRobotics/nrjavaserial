all:
	echo "Please specify a system: windows wine linux osx"
	./gradlew build
windows:
	mingw32-make -C .\src\main\c windowsLocal
	./gradlew.bat build
wine:
	make -C src/main/c windows
	./gradlew build
linux:
	sudo apt-get install liblockfile-dev
	make -C src/main/c linux
	./gradlew build
linux32:
	sudo apt-get install  liblockfile-dev libc6-dev-i386
	make -C src/main/c linux32
	./gradlew build
linux64:
	sudo apt-get install liblockfile-dev
	make -C src/main/c linux64
	./gradlew build
freebsd:
	gmake -C src/main/c freebsd
	./gradlew build
freebsd32:
	gmake -C src/main/c freebsd32
	./gradlew build
freebsd64:
	gmake -C src/main/c freebsd64
	./gradlew build
arm:
	sudo apt-get install g++-arm-linux-gnueabihf g++-arm-linux-gnueabi g++-aarch64-linux-gnu liblockfile-dev
	make -C src/main/c arm
	./gradlew build
ppc:
	make -C src/main/c ppc
	./gradlew build
osx:
	make -C src/main/c osx
	./gradlew build

