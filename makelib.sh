#!/usr/bin/env bash
cd lib
if [[ -f libstd.so ]]; then rm libstd.so; fi
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
gcc -shared -o libstd.so *.o  *.o2
#rm *.o *.o2