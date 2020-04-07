all:
	echo "Please specify a system: windows wine linux osx"
	gradle build
windows:
	mingw32-make -C .\src\main\c windowsLocal
	gradle build
wine:
	make -C src/main/c windows
	gradle build
linux:
	sudo apt-get install liblockdev1-dev
	make -C src/main/c linux
	gradle build
linux32:
	sudo apt-get install  liblockdev1-dev
	make -C src/main/c linux32
	gradle build
linux64:
	sudo apt-get install liblockdev1-dev
	make -C src/main/c linux64
	gradle build
freebsd:
	gmake -C src/main/c freebsd
	gradle build
freebsd32:
	gmake -C src/main/c freebsd32
	gradle build
freebsd64:
	gmake -C src/main/c freebsd64
	gradle build
arm:
	sudo apt-get install g++-arm-linux-gnueabihf g++-arm-linux-gnueabi g++-aarch64-linux-gnu liblockdev1-dev
	make -C src/main/c arm
	gradle build
ppc:
	make -C src/main/c ppc
	gradle build
osx:
	make -C src/main/c osx
	gradle build

