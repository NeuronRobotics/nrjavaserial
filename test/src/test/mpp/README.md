# Introduction

This shows how to use a /dev/hidraw device in the example of idVendor=0665 (Cypress Semiconductor), idProduct=5161 (USB to Serial), bcdDevice= 0.02, USB device string Mfr=3, Product=1, SerialNumber=0.

The device is embedded in inverters known as Axpert, Effekta, MPP, Voltronic, or even having no name.

Communication with it can be done using a USB Type B cable, demonstrated herein.  It supports also communication over LAN cable, bluetooth, RS232.

# Caveats imposed by the Linux Kernel

* reading one byte or writing one byte does not work.
* tcgetattr() and tcsetattr() also do not work.  tcgetattr() sets errno to EINVAL, but should set it to ENOTTY, cf. https://lore.kernel.org/linux-input/24eaed9105633d03eded13e11c5a994bd93a81aa.camel@aegee.org/.
* ioctl(,FIONREAD,) also does not work.
* reading data arrives in batches of 8 bytes.

# Implications for the implementation

* Do not fetch one byte with RXTXPort.getInputStream().read(), do not use RXTXPort.getOutputStream.write(byte).
* Avoid RXTXPort.nativeavailable() by utilizing enableReceiveThreshold(8).  It calls tcgetattr(), which throws an exception, but nevertheless sets the threshold.
* To make progress, when the cable is unplugged, set enableReceiveTimeout(1000). It again throws an exception when invoking tcgetattr(), but nevertheless sets the timeout for select().

# Building

cd nrjavaserial/test/src
javac test/mpp/CRCUtil.java test/mpp/SerialHandler.java
java -classpath ../../build/libs/nrjavaserial-5.2.1.jar:. test/mpp/MPP.java

To utilizie /dev/hidraw1, instead of the default /dev/hidraw0, use

java -classpath ../../build/libs/nrjavaserial-5.2.1.jar:. test/mpp/MPP.java /dev/hidraw1
