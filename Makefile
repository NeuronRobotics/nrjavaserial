all:
	echo "Please specify a system: windows wine linux osx"
	gradle build
windows:
	make -C .\src\main\c windowsLocal
	gradle build
wine:
	make -C src/main/c windows
	gradle build
linux:
	make -C src/main/c linux
	gradle build
linux32:
	make -C src/main/c linux32
	gradle build
linux64:
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
	sudo apt-get install g++-arm-linux-gnueabihf g++-arm-linux-gnueabi
	make -C src/main/c arm7
	make -C src/main/c arm7HF
	make -C src/main/c arm8
	make -C src/main/c arm8HF
	gradle build
ppc:
	make -C src/main/c ppc
	gradle build
osx:
	make -C src/main/c osx
	gradle build

