PACKAGE = pseudo-runtime-lib
VERSION = 1.0
SO_VERSION = 0

all: libpseudo-std.so

libpseudo-std.so: stdlib.o stdlib.o2
	gcc -shared -o $@ $^

stdlib.o: stdlib.asm
	as -o $@ $<

stdlib.o2: stdlib.s
	as -o $@ $<

stdlib.s: stdlib.c
	gcc -O3 -S -fPIC $<

clean:
	rm -f libpseudo-std.so stdlib.o stdlib.o2 stdlib.s

install: libpseudo-std.so
	cp libpseudo-std.so "$(DESTDIR)/lib64/libpseudo-std.so.$(SO_VERSION)";\
	rm -f "$(DESTDIR)/lib64/libpseudo-std.so";\
	ln -s "$(DESTDIR)/lib64/libpseudo-std.so.$(SO_VERSION)" "$(DESTDIR)/lib64/libpseudo-std.so"