include src/main/c/platform.mk

define ONLY_GRADLE
You haven't specified a platform to build the native library, so I'm only going
to build the Java portion of the project. To build natives, specify a platform:

    make linux|osx|freebsd|windows

endef

only-gradle:
	$(info $(ONLY_GRADLE))
	$(call gradlew-build)
%:
	$(MAKE) -C src/main/c $@
	$(call gradlew-build)
