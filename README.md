# About

[![Financial Contributors on Open Collective](https://opencollective.com/nrjavaserial/all/badge.svg?label=financial+contributors)](https://opencollective.com/nrjavaserial) [![Join the chat at https://gitter.im/NeuronRobotics/nrjavaserial](https://badges.gitter.im/NeuronRobotics/nrjavaserial.svg)](https://gitter.im/NeuronRobotics/nrjavaserial?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.neuronrobotics/nrjavaserial/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.neuronrobotics/nrjavaserial)
[![Build](https://github.com/NeuronRobotics/nrjavaserial/workflows/Build/badge.svg)](https://github.com/NeuronRobotics/nrjavaserial/actions/workflows/build.yml?query=branch%3Amaster)
[![License](https://img.shields.io/badge/License-LGPL%20v2.1-brightgreen.svg)](http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt)

This is a fork of the [RXTX library](http://rxtx.qbang.org/) with a focus on
ease of use and embeddability in other libraries.

## Some of the features we have added

* A simplified serial port object called `NRSerialPort`. See below for an
  example.

* Self-deployment of native libraries (all native code is stored inside the JAR
  and deployed at runtime). No more manual installation of native code.

* Arm Cortex support (Gumstix).

* Android (3.x or lower, requires a rooted phone to access the serial hardware).

    This feature is depricated by changes in Android permissions moving forward with 4.x

* Single Makefile compile which simplifies the compilation of project binaries.

* Gradle support for JAR creation.

* Removal of partially-implemented RXTX code to streamline the library for just
  serial port access.

* Full Eclipse integration for testing application code against sources.

* [RFC 2217](http://tools.ietf.org/html/rfc2217) support provided by
  incorporating the [jvser library](http://github.com/archiecobbs/jvser).

* RS485 support for Linux

## And a bunch of bug fixes

* Fixed the memory access error that causes OS X to crash the JVM when
  `serial.close()` is called.

* Fixed the Windows serial port zombie bind that prevents re-accessing serial
  ports when exiting on an exception.

* Fixed erroneous printouts of native library mis-match.

# Dependency Management

## Maven Java 8 and Java 11+
```
	<dependency>
	  <groupId>com.neuronrobotics</groupId>
	  <artifactId>nrjavaserial</artifactId>
	  <version>5.1.1</version>
	</dependency>
```

# Building the JAR

1. Checkout the repository.

        $ git clone https://github.com/NeuronRobotics/nrjavaserial.git

2. Build with Gradle.

        $ cd nrjavaserial
        $ ./gradlew build

The resulting JAR will be found in the `build/libs/` directory.

# Building the native libraries

The native libraries are written in C,
and are built with Make.
The source,
including the master makefile,
lives in `src/main/c`.
The makefile in the project root
will delegate to this makefile,
and rebuild the JAR afterwards.

You'll also need a copy of the JNI development headers
for your system.
The easiest way to get these is to install a copy of the JDK.
The headers are located in `JAVA_HOME/include`.
The build process will attempt to automatically determine
the location of your Java installation;
however, to ensure a predictable build process,
you should set the `JAVA_HOME` environment variable
to the path of your Java installation directory.

    # Install cross-compilation toolchains for all of the supported Linux
    # architectures and Windows onto an amd64 Linux build host running
    # Debian 10 or a modern derivative.
    $ sudo make -C src/main/c crosstools

    # Build both the 32- and 64-bit Windows binaries.
    $ make windows

    # Build Linux binaries for 32- and 64-bit x86, 32-bit ARMv5/v6/v7/v8,
    # 64-bit ARMv8, and 32-bit PPC.
    $ make linux

    # Build only 32- or 64-bit x86 Linux binaries, respectively.
    $ make linux32
    $ make linux64

    # Build binaries for all the supported ARM flavors.
    $ make arm

    # Build the PPC binaries.
    $ make ppc

    # Build 64-bit x86 macOS binaries. This requires a macOS build host.
    $ make osx

    # Build 32- and 64-bit x86 FreeBSD binaries, or just for 32/64-bit,
    # respectively. This requires a FreeBSD build host.
    $ make freebsd
    $ make freebsd32
    $ make freebsd64

## Building on Windows

You'll need some installation of GCC.
We recommend the [TDM-GCC] distribution of Mingw64-w64.
Following its default installation process
should result in its `bin/` directory being added to your path;
if you can pop open a command prompt and run `x86_64-w64-mingw32-gcc`,
you're good to go.

[TDM-GCC]: https://jmeubank.github.io/tdm-gcc/

## Building on OS X

We're pretty big on maintaining backwards compatibility as far as reasonable.
Our OS X natives target OS X 10.5, so to build them, you'll need an appropriate
SDK installed. [This StackOverflow answer](http://stackoverflow.com/a/6293605)
provides pointers for getting the appropriate SDK installed.

# How to use NRSerialPort objects
```
import gnu.io.NRSerialPort;
String port = "";
for(String s:NRSerialPort.getAvailableSerialPorts()){
	System.out.println("Availible port: "+s);
	port=s;
}

int baudRate = 115200;
NRSerialPort serial = new NRSerialPort(port, baudRate);
serial.connect();

DataInputStream ins = new DataInputStream(serial.getInputStream());
DataOutputStream outs = new DataOutputStream(serial.getOutputStream());
try{
	//while(ins.available()==0 && !Thread.interrupted());// wait for a byte
	while(!Thread.interrupted()) {// read all bytes
		if(ins.available()>0) {
			char b = ins.read();
			//outs.write((byte)b);
			System.out.print(b);
		}
    		Thread.sleep(5);
	}
}catch(Exception ex){
	ex.printStackTrace();
}
serial.disconnect();
```

## Contributors

### Code Contributors

This project exists thanks to all the people who contribute. [[Contribute](CONTRIBUTING.md)].
<a href="https://github.com/NeuronRobotics/nrjavaserial/graphs/contributors"><img src="https://opencollective.com/nrjavaserial/contributors.svg?width=890&button=false" /></a>

### Financial Contributors

Become a financial contributor and help us sustain our community. [[Contribute](https://opencollective.com/nrjavaserial/contribute)]

#### Individuals

<a href="https://opencollective.com/nrjavaserial"><img src="https://opencollective.com/nrjavaserial/individuals.svg?width=890"></a>

#### Organizations

Support this project with your organization. Your logo will show up here with a link to your website. [[Contribute](https://opencollective.com/nrjavaserial/contribute)]

<a href="https://opencollective.com/nrjavaserial/organization/0/website"><img src="https://opencollective.com/nrjavaserial/organization/0/avatar.svg"></a>
<a href="https://opencollective.com/nrjavaserial/organization/1/website"><img src="https://opencollective.com/nrjavaserial/organization/1/avatar.svg"></a>
<a href="https://opencollective.com/nrjavaserial/organization/2/website"><img src="https://opencollective.com/nrjavaserial/organization/2/avatar.svg"></a>
<a href="https://opencollective.com/nrjavaserial/organization/3/website"><img src="https://opencollective.com/nrjavaserial/organization/3/avatar.svg"></a>
<a href="https://opencollective.com/nrjavaserial/organization/4/website"><img src="https://opencollective.com/nrjavaserial/organization/4/avatar.svg"></a>
<a href="https://opencollective.com/nrjavaserial/organization/5/website"><img src="https://opencollective.com/nrjavaserial/organization/5/avatar.svg"></a>
<a href="https://opencollective.com/nrjavaserial/organization/6/website"><img src="https://opencollective.com/nrjavaserial/organization/6/avatar.svg"></a>
<a href="https://opencollective.com/nrjavaserial/organization/7/website"><img src="https://opencollective.com/nrjavaserial/organization/7/avatar.svg"></a>
<a href="https://opencollective.com/nrjavaserial/organization/8/website"><img src="https://opencollective.com/nrjavaserial/organization/8/avatar.svg"></a>
<a href="https://opencollective.com/nrjavaserial/organization/9/website"><img src="https://opencollective.com/nrjavaserial/organization/9/avatar.svg"></a>

