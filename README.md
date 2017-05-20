# About

[![Join the chat at https://gitter.im/NeuronRobotics/nrjavaserial](https://badges.gitter.im/NeuronRobotics/nrjavaserial.svg)](https://gitter.im/NeuronRobotics/nrjavaserial?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.neuronrobotics/nrjavaserial/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.neuronrobotics/nrjavaserial)

This is a fork of the [RXTX library](http://rxtx.qbang.org/) with a focus on
ease of use and embeddability in other libraries.

## Some of the features we have added

* A simplified serial port object called `NRSerialPort`. See below for an
  example.

* Self-deployment of native libraries (all native code is stored inside the JAR
  and deployed at runtime). No more manual installation of native code.

* Arm Cortex support (Gumstix).

* Android Support (requires a rooted phone to access the serial hardware).

* Single Makefile compile which simplifies the compilation of project binaries.

* Gradle support for JAR creation.

* Removal of partially-implemented RXTX code to streamline the library for just
  serial port access.

* Full Eclipse integration for testing application code against sources.

* [RFC 2217](http://tools.ietf.org/html/rfc2217) support provided by
  incorporating the [jvser library](http://github.com/archiecobbs/jvser).

## And a bunch of bug fixes

* Fixed the memory access error that causes OS X to crash the JVM when
  `serial.close()` is called.

* Fixed the Windows serial port zombie bind that prevents re-accessing serial
  ports when exiting on an exception.

* Fixed erroneous printouts of native library mis-match.

# Dependency Management

## Maven

	<dependency>
	  <groupId>com.neuronrobotics</groupId>
	  <artifactId>nrjavaserial</artifactId>
	  <version>3.12.1</version>
	</dependency>

# Building the JAR

1. Checkout the repository.

        $ git clone https://github.com/NeuronRobotics/nrjavaserial.git

2. Build with Gradle.

        $ cd nrjavaserial
        $ gradle build

The resulting JAR will be found in the `build/libs/` directory.

# Building Native Code

Native code is built using the Makefile found in the root of the repository.
After the native code is built, the JAR is rebuilt.

    # Build both the 32- and 64-bit Windows binaries.
    $ make windows

    # Build the windows binaries on Linux via Wine.
    $ make wine

    # Build both the 32- and 64-bit Linux x86 binaries.
    $make linux

    # Build 32- or 64-bit Linux binaries, respectively.
    $ make linux32
    $ make linux64

    # Build the binaries for all the supported ARM flavors (requires arm-linux-geabi-* packages)
    $ make arm

    # Build the OSX binaries.
    $ make osx

    # Build the PPC binaries.
    $ make ppc


## Building on Windows

You'll need some installation of GCC. We recommend the
[TDM-GCC](http://tdm-gcc.tdragon.net/) distribution of mingw64-w64.

## Building on OS X

We're pretty big on maintaining backwards compatibility as far as reasonable.
Our OS X natives target OS X 10.5, so to build them, you'll need an appropriate
SDK installed. [This StackOverflow answer](http://stackoverflow.com/a/6293605)
provides pointers for getting the appropriate SDK installed.

# How to use NRSerialPort objects
    for(String s:NRSerialPort.getAvailableSerialPorts()){
			System.out.println("Availible port: "+s);
		}
    String port = "COM3";
    int baudRate = 115200;
    NRSerialPort serial = new NRSerialPort(port, baudRate);
    serial.connect();

    DataInputStream ins = new DataInputStream(serial.getInputStream());
    DataOutputStream outs = new DataOutputStream(serial.getOutputStream());

    byte b = ins.read();
    outs.write(b);

    serial.disconnect();
