#!/usr/bin/env bash
cd lib
if [[ -f libpseudo-std.so ]]; then rm libpseudo-std.so; fi
for fl in *.asm ; do
    flname="$(basename -- $fl)"
    flname="${flname%.*}"
    as $fl -o "$flname".o
done

gcc -O3 -c *.c -S -fPIC

for fl in *.s ; do
    flname="$(basename -- $fl)"
    flname="${flname%.*}"
    as "$fl" -o "$flname".o2
done
echo "Successfully compiled"
gcc -shared -o libpseudo-std.so *.o  *.o2
#rm *.o *.o2