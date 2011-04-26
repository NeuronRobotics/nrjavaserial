
all:
	echo "Please specify a system: windows wine linux osx"
	ant
windows:
	make -C .\src\main\c windowsLocal
	ant
wine:
	make -C src/main/c windows
	ant
linux32:
	make -C src/main/c linux32
	ant
linux64:
	make -C src/main/c linux64
	ant
armChumby:
	make -C src/main/c armChumby
	ant
armA8:
	make -C src/main/c armA8
	ant
armAndroid:
	make -C src/main/c	armAndroid
	ant
osx:
	make -C src/main/c osx
	ant