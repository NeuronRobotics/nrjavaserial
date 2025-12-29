# Native library compilation.
#
# The suggested package names assume you're running Debian 10 (buster) or a
# close relative (e.g., Ubuntu) on an amd64 build host.
#
# For each supported platform, this file defines which objects need to be
# built, how the resulting library binary should be organized in the source
# tree, and compiler and linker executables and flags. Due to the mind-boggling
# complexity of writing interesting dynamic Make rules, the actual build is
# delegated to a sub-Make task.
#
# To define compilation for a new platform, the following target-specific
# variables must be exported:
#
# * platform: The subdirectory within `resources/native/` which will hold the
#             library binary.
# * variant: A suffix inserted into the filename of the library between its
#            regular name and its extension. E.g., if the usual name of the
#            library is “libNRJavaSerial.so”, for variant “foo”, the filename
#            produced will be “libNRJavaSerialfoo.so”. This is useful for ARM
#            platforms, where there are multiple variants on the same core
#            32-bit architecture.
# * lib_type: The type of library (so, dylib, dll). Used to construct the
#             library filename.
#
# Additionally, the following variables will already have sensible definitions
# for compilation targetting the build host, but some of which will probably
# require overriding for cross-compilation:
#
# * CC and LD: Command names for the compiler and linker, respectively.
# * CFLAGS and LDFLAGS: Flags for the compiler and linker, respectively.
# * objects: The names of the object files to be compiled and linked together
#            to form the library. A 1-to-1 relationship between object names
#            (`foo.o`) and source file names (`foo.c`) is assumed. Most
#            platforms build from the same source set, but sometimes additional
#            code is necessary (e.g., on Windows).
#
# Your target must also invoke the `natives.mk` sub-makefile directly.
# Unfortunately, this must be done per-target: it cannot be done as a phony
# rule, because Make will invoke each target at most once per run. If you split
# the invocation into such a rule:
#
# .PHONY: natives
# natives:
# 	$(MAKE) -f natives.mk
#
# ...it will only be invoked for the first target which depends on it.
#
# See the existing targets for examples of all of this. The Windows and macOS
# targets are good examples, because they require overrides for just about
# everything.

# We need to know the location of a Java installation to include the JNI
# headers. We'd rather pull from the JAVA_HOME environment variable, but it's
# very often not set. In cases where it's absent, this included Makefile will
# attempt to discover the Java home path.
include JAVA_HOME.mk

include platform.mk

# In all cases, we want to include the system JNI headers, our own headers,
# crank the optimization level, and compile position-independent code so that
# it will work as a library (dlopen(3) et al. can load it into any memory).
export CFLAGS ?= -I"$(JAVA_HOME)/include" -I./include -I./include/target \
                   -O3 -fPIC -c \
                   -Wall
# We want to use the compiler frontend to invoke the linker, too.
export LD = $(CC)
# We want to link objects into a shared library.
export LDFLAGS ?= -fPIC -shared

export objects := fixup.o fuserImp.o SerialImp.o
export lib_type := so

define NO_TARGET_PLATFORM_SPECIFIED
You must specify a target platform to build its natives:

    make linux|osx|freebsd|windows

The “linux” target encompasses both x86 and ARM platforms. To install
cross-compilation toolchains to target these platforms and Windows from an
amd64 build host running Debian or a derivative, build the “crosstools” target
as root
endef

all:
	$(error $(NO_TARGET_PLATFORM_SPECIFIED))

crosstools:
	apt install --install-recommends \
	            gcc-i686-linux-gnu \
	            gcc-arm-linux-gnueabi \
	            gcc-arm-linux-gnueabihf \
	            gcc-aarch64-linux-gnu \
	            gcc-mingw-w64 \
	            gcc-powerpc-linux-gnu

# Does not include Android, because Android toolchains are large, not commonly
# available, and the resulting library is not typically desired.
linux: linux32 linux64 arm ppc
LINUX_CFLAGS := -I$(JAVA_HOME)/include/linux
# Source fortification (-D_FORTIFY_SOURCE) are great, but some of the runtime
# checks are only available in newer versions of glibc. Disabling this for
# deployment versions lets us maintain compatibility with much older systems
# (from glibc 2.15 down to glibc 2.7).
ifndef DEBUG
LINUX_CFLAGS += -U_FORTIFY_SOURCE
endif

# Requires gcc-i686-linux-gnu.
linux32: export CC := i686-linux-gnu-gcc
linux32: export CFLAGS += $(LINUX_CFLAGS) -m32
linux32: export LDFLAGS += -m32
linux32: export platform := linux/x86_32
linux32:
	$(MAKE) -f natives.mk

# Requires gcc.
linux64: export CFLAGS += $(LINUX_CFLAGS) -m64
linux64: export platform := linux/x86_64
linux64:
	$(MAKE) -f natives.mk

arm: arm32v5 \
     arm32v6 arm32v6HF \
     arm32v7 arm32v7HF \
     arm32v8 arm32v8HF arm64v8

# Requires gcc-arm-linux-gnueabi.
arm32v5: export CC := arm-linux-gnueabi-gcc
arm32v5: export CFLAGS += $(LINUX_CFLAGS) -march=armv5t
arm32v5: export LDFLAGS += -march=armv5t
arm32v5: export platform := linux/ARM_32
arm32v5: export variant := v5
arm32v5:
	$(MAKE) -f natives.mk

# Requires gcc-arm-linux-gnueabi.
arm32v6: export CC := arm-linux-gnueabi-gcc
arm32v6: export CFLAGS += $(LINUX_CFLAGS) -march=armv6
arm32v6: export LDFLAGS += -march=armv6
arm32v6: export platform := linux/ARM_32
arm32v6: export variant := v6
arm32v6:
	$(MAKE) -f natives.mk

# Requires gcc-arm-linux-gnueabihf.
arm32v6HF: export CC := arm-linux-gnueabihf-gcc
arm32v6HF: export CFLAGS += $(LINUX_CFLAGS) -march=armv6 -mfpu=vfp -marm
arm32v6HF: export LDFLAGS += -march=armv6 -mfpu=vfp -marm
arm32v6HF: export platform := linux/ARM_32
arm32v6HF: export variant := v6_HF
arm32v6HF:
	$(MAKE) -f natives.mk

# Requires gcc-arm-linux-gnueabi.
arm32v7: export CC := arm-linux-gnueabi-gcc
arm32v7: export CFLAGS += $(LINUX_CFLAGS) -march=armv7-a
arm32v7: export LDFLAGS += -march=armv7-a
arm32v7: export platform := linux/ARM_32
arm32v7: export variant := v7
arm32v7:
	$(MAKE) -f natives.mk

# Requires gcc-arm-linux-gnueabihf.
arm32v7HF: export CC := arm-linux-gnueabihf-gcc
arm32v7HF: export CFLAGS += $(LINUX_CFLAGS) -march=armv7-a -mfpu=vfpv3-d16 -marm
arm32v7HF: export LDFLAGS += -march=armv7-a -mfpu=vfpv3-d16 -marm
arm32v7HF: export platform := linux/ARM_32
arm32v7HF: export variant := v7_HF
arm32v7HF:
	$(MAKE) -f natives.mk

# Requires gcc-arm-linux-gnueabi.
arm32v8: export CC := arm-linux-gnueabi-gcc
arm32v8: export CFLAGS += $(LINUX_CFLAGS) -march=armv8-a
arm32v8: export LDFLAGS += -march=armv8-a
arm32v8: export platform := linux/ARM_32
arm32v8: export variant := v8
arm32v8:
	$(MAKE) -f natives.mk

# Requires gcc-arm-linux-gnueabihf.
arm32v8HF: export CC := arm-linux-gnueabihf-gcc
arm32v8HF: export CFLAGS += $(LINUX_CFLAGS) -march=armv8-a -mfpu=neon-vfpv4 -marm
arm32v8HF: export LDFLAGS += -march=armv8-a -mfpu=neon-vfpv4 -marm
arm32v8HF: export platform := linux/ARM_32
arm32v8HF: export variant := v8_HF
arm32v8HF:
	$(MAKE) -f natives.mk

# Requires gcc-aarch64-linux-gnu.
arm64v8: export CC := aarch64-linux-gnu-gcc
arm64v8: export CFLAGS += $(LINUX_CFLAGS) -march=armv8-a
arm64v8: export LDFLAGS += -march=armv8-a
arm64v8: export platform := linux/ARM_64
arm64v8: export variant := v8
arm64v8:
	$(MAKE) -f natives.mk

# Requires an Android NDK be installed, and its tools be on your PATH.
#
# This rule has not been tested.
android: export CC := arm-linux-androideabi-gcc
android: export CFLAGS += $(LINUX_CFLAGS)
android: export platform := linux/ARM_ANDROID
android:
	$(MAKE) -f natives.mk

# Requires gcc-powerpc-linux-gnu.
ppc: export CC := powerpc-linux-gnu-gcc
ppc: export CFLAGS += $(LINUX_CFLAGS)
ppc: export platform := linux/PPC
ppc:
	$(MAKE) -f natives.mk

# Requires a macOS host.
osx: export CFLAGS += -I$(JAVA_HOME)/include/darwin -arch x86_64
osx: export LDFLAGS := -arch x86_64 -dynamiclib -framework JavaVM -framework IOKit -framework CoreFoundation
osx: export objects := fuserImp.o SerialImp.o
osx: export platform := osx
osx: export lib_type := jnilib
osx:
	$(MAKE) -f natives.mk

freebsd: freebsd32 freebsd64

# Requires a FreeBSD host.
freebsd32: export CFLAGS += -I$(JAVA_HOME)/include/freebsd -m32
freebsd32: export LDFLAGS += -m32
freebsd32: export platform := freebsd/x86_32
freebsd32:
	$(MAKE) -f natives.mk

# Requires a FreeBSD host.
freebsd64: export CFLAGS += -I$(JAVA_HOME)/include/freebsd -m64
freebsd64: export LDFLAGS += -m64
freebsd64: export platform := freebsd/x86_64
freebsd64:
	$(MAKE) -f natives.mk

# Requires a FreeBSD host running on aarch64 (arm64) architecture.
freebsdarm64v8: export CFLAGS += -I$(JAVA_HOME)/include/freebsd
freebsdarm64v8: export platform := freebsd/ARM_64
freebsdarm64v8: export variant := v8
freebsdarm64v8:
	$(MAKE) -f natives.mk

windows: windows32 windows64
# Don't embed the build timestamp into DLLs so the output is the same from one
# compile to the next for the same input source.
# https://blog.beuc.net/posts/Practical_basics_of_reproducible_builds_2/
WINDOWS_LDFLAGS := -static-libgcc \
                   -Wl,--add-stdcall-alias \
                   -Wl,--no-insert-timestamp

# Requires gcc-mingw-w64-i686 (included in gcc-mingw-w64). On Windows, this,
# along with mingw32-make, is most easily acquired in the form of TDM-GCC:
# https://jmeubank.github.io/tdm-gcc/.
ifeq ($(OS),Windows_NT)
# On Windows, TDM-GCC includes one GCC binary which can produce both 32- and
# 64-bit binaries.
windows32: export CC := x86_64-w64-mingw32-gcc
else
# Elsewhere, Mingw-w64 ships a separate GCC frontend for each target
# architecture.
windows32: export CC := i686-w64-mingw32-gcc
endif
# We include the only relevant Windows-specific Java header in the repository
# and compile against that path, instead of $(JAVA_HOME)/include/win32, because
# we can cross-compile the Windows natives from other platforms via Mingw-w64,
# and other platform JDK installations will only have their own headers.
windows32: export CFLAGS += -I./include/windows -I./include/windows/win32 -m32
windows32: export LDFLAGS += $(WINDOWS_LDFLAGS) -m32
windows32: export objects += init.o termios.o
windows32: export platform := windows/x86_32
windows32: export lib_type := dll
windows32:
	$(MAKE) -f natives.mk

# Requires gcc-mingw-w64-x86-64 (included in gcc-mingw-w64).
windows64: export CC := x86_64-w64-mingw32-gcc
windows64: export CFLAGS += -I./include/windows -I./include/windows/win32 -m64
windows64: export LDFLAGS += $(WINDOWS_LDFLAGS) -m64
windows64: export objects += init.o termios.o
windows64: export platform := windows/x86_64
windows64: export lib_type := dll
windows64:
	$(MAKE) -f natives.mk

.PHONY: clean
clean: | clean-intermediaries
	$(warning The “clean” target only removes intermediary build objects. \
	          Because not all of the native libraries can be cross-compiled, \
	          you probably only want to delete the ones for your current \
	          platform. To remove those, along with the intermediary build \
	          objects, invoke the appropriate “clean-platform” target: make \
	          clean-linux|clean-osx|clean-freebsd|clean-windows.)

.PHONY: clean-%
clean-%: | clean-intermediaries
	$(call rmdir_r,resources/native/$*)

.PHONY: clean-intermediaries
clean-intermediaries:
	$(call rmdir_r,build)
