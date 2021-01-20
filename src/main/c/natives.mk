# Make is limited in its ability to compose dynamic rules, particularly when
# both the prerequisites and the target sprawl across multiple directories. By
# breaking the guts of the build out into this separate makefile, we can
# compose the rules from static variables passed from the parent makefile.

include platform.mk

resources/native/$(platform)/libNRJavaSerial$(variant).$(lib_type): $(addprefix build/$(platform)/,$(join $(addsuffix $(variant),$(basename $(objects))),$(suffix $(objects)))) | resources/native/$(platform)
	$(LD) $(LDFLAGS) $^ $(LDLIBS) -o $@

resources/native/$(platform):
	$(call mkdir,$@)

build/$(platform)/%$(variant).o: src/%.c | build/$(platform)
	$(CC) $(CFLAGS) $^ -o $@

build/$(platform)/%$(variant).o: src/windows/%.c | build/$(platform)
	$(CC) $(CFLAGS) $^ -o $@

build/$(platform):
	$(call mkdir,$@)
