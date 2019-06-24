PACKAGE = pseudo-runtime-lib
VERSION = 1.0
SO_VERSION = 0
AS = as
AS_OPTS =
CC = gcc
CC_OPTS = -O3 -S -fPIC
LINKER = gcc
LINKER_SHARED_OPTS = -shared
all: libpseudo-std.so

libpseudo-std.so: stdlib.o stdlib.o2
	$(LINKER) $(LINKER_SHARED_OPTS) -o $@ $^

%.o: %.asm
	$(AS) $(AS_OPTS) -o $@ $<

%.o2: %.s
	$(AS) $(AS_OPTS) -o $@ $<

%.s: %.c
	$(CC) $(CC_OPTS) $<

clean:
	rm -f libpseudo-std.so stdlib.o stdlib.o2 stdlib.s

install: libpseudo-std.so
	cp libpseudo-std.so "$(DESTDIR)/lib64/libpseudo-std.so.$(SO_VERSION)";\
	rm -f "$(DESTDIR)/lib64/libpseudo-std.so";\
	ln -s "$(DESTDIR)/lib64/libpseudo-std.so.$(SO_VERSION)" "$(DESTDIR)/lib64/libpseudo-std.so"