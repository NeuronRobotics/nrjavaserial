This directory contains shared objects (used as stubs) 
that cannot (easily) be provided by packages on the 
system that is used for cross compiling.

The shared object "stubs" are used when a new shared object
(our serial native library) is created during cross 
compilation. They "inform" the linker about the entry 
points available on the target system. Therefore they 
only have to be updated when the API version changes. 