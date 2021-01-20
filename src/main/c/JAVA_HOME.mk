# “Undefined JAVA_HOME” is a strong contender for the most hated error message
# in all of software development, so we'll take a shot at figuring it out
# before leaving the user to fend for themselves.
#
# The detection here tries to be failure-tollerant, but Make is not a very good
# environment for either resilient string manipulation or reliable
# cross-platform path operations. If you're reading this because you think the
# path detection contained herein is causing trouble, the easiest way to avoid
# this file altogether is to define the JAVA_HOME environment variable.

ifndef JAVA_HOME

define MISSING_JAVA
The JAVA_HOME environment variable has not been defined, and you don't have the
`java` binary on your PATH, so I can't determine the location of your Java
installation automatically. Please set the JAVA_HOME environment variable to
the location of your preferred Java installation
endef

define EXPLICITLY_SET
If that's not correct, or to suppress this message, explicitly set the
JAVA_HOME environment variable to the location of your preferred Java
installation.
endef

define DARWIN_GUESSED_JAVA_HOME
The JAVA_HOME environment variable has not been defined;
based on the output of `/usr/libexec/java_home`, I'm going to use:

    $(JAVA_HOME)

$(EXPLICITLY_SET)
endef

define GUESSED_JAVA_HOME
The JAVA_HOME environment variable has not been defined.
Based on the location of the `java` binary, I've guessed it's:

    $(JAVA_HOME)

$(EXPLICITLY_SET)
endef

# Windows will set the “OS” environment variable by default.
ifeq ($(OS),)
OS := $(shell uname)
endif

# On macOS, we can just ask where Java is.
ifeq ($(OS),Darwin)
JAVA_HOME=$(shell /usr/libexec/java_home)
$(warning $(DARWIN_GUESSED_JAVA_HOME))

else

# First, find java(1). On POSIX systems, we can use which(1); on Windows, we
# need to use WHERE.
WHICH=which
ifeq ($(OS),Windows_NT)
# Just checking whether we're on Windows isn't good enough: if we're running in
# a Cygwin or MinGW environment (e.g., Git Bash) which requires Unix-like
# absolute paths (e.g., “/c/foo/bar”, not “C:\foo\bar”), then we still need to
# use which(1) – WHERE will give us paths we can't use. TERM seems like a
# pretty safe environment variable to check; it's a Unix-ism not embraced by
# cmd.exe or PowerShell, but it should be set by emulation environments.
ifndef TERM
# This needs to explicitly include the “.exe” extension to disambiguate between
# the classic “WHERE” executable and the “Where” PowerShell cmdlet.
WHICH=where.exe
endif
endif

JAVA_PATH := $(shell $(WHICH) java)
ifeq ($(JAVA_PATH),)
$(error $(MISSING_JAVA))
endif

# Make's builtin filename operations, such as $(realpath ...) and $(dir ...),
# operate on space-separated lists of filenames, and Java is often installed to
# a path containing spaces on Windows (e.g., C:\Program Files\AdoptOpenJDK\
# jdk-8.x.y.z-hotspot), so we need to munge any spaces in the path to something
# which won't appear in legal path names, do our manipulations, then swap the
# spaces back into place.
empty :=
space := $(empty) $(empty)
JAVA_PATH := $(subst $(space),?,$(JAVA_PATH))

# Resolve any symlinks (e.g., /usr/bin/java → /etc/alternatives/java →
# /usr/lib/jvm/java-11-openjdk-amd64/bin/java.
JAVA_REALPATH := $(realpath $(JAVA_PATH))
ifneq ($(JAVA_REALPATH),)
# Of course, if there _were_ any spaces in the path (which have now been
# replaced with an invalid character), realpath will fall over. Let's hope we
# never have to deal with a situation where the path contains symlinks _and_
# spaces.
JAVA_PATH := $(JAVA_REALPATH)
endif

# JAVA_PATH should be JAVA_HOME/bin/java or maybe JAVA_HOME/jre/bin/java. To
# get JAVA_HOME, we want to traverse two or three levels up from JAVA_PATH.
# However, we can't just call $(dir $(dir $(JAVA_PATH)), because unlike
# dirname(1), $(dir ...) returns the given path untouched if it is already a
# directory, instead of always returning the parent (i.e., “$(dir foo/bar/)”
# returns “foo/bar/”, not “foo/”). And $(abspath ...) breaks the leading “/c/”
# in POSIX emulation shells on Windows. We'll just resort to to string
# manipulation.
JAVA_HOME := $(subst /jre/bin/java,,$(JAVA_PATH))
JAVA_HOME := $(subst /bin/java,,$(JAVA_HOME))
JAVA_HOME := $(subst \bin\java.exe,,$(JAVA_HOME))
# Now fix any spaces in the path.
JAVA_HOME := "$(subst ?,$(space),$(JAVA_HOME))"
$(warning $(GUESSED_JAVA_HOME))

endif # ifeq ($(OS),Darwin)
endif # ifndef JAVA_HOME
