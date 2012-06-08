
all:
	echo "Please specify a system: windows wine linux osx"
	ant
windows:
	make -C .\src\main\c windowsLocal
	ant
wine:
	make -C src/main/c windows
	ant
linux:
	make -C src/main/c linux
	ant
linux32:
	make -C src/main/c linux32
	ant
linux64:
	make -C src/main/c linux64
	ant
arm:
	sudo apt-get install g++-arm-linux-gnueabihf g++-arm-linux-gnueabi
	make -C src/main/c arm
	make -C src/main/c armHF
	ant
armAndroid:
	make -C src/main/c	armAndroid
	ant
osx:
	make -C src/main/c osx
	ant