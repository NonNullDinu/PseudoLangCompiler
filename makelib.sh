#!/usr/bin/env bash
cd lib
if [[ -f libstd.a ]]; then rm libstd.a; fi
for fl in *.asm ; do
    flname="$(basename -- $fl)"
    flname="${flname%.*}"
    as $fl -o "$flname".o
done

for fl in *.c ; do
    flname="$(basename -- $fl)"
    flname="${flname%.*}"
    gcc -O3 $fl -S
done
for fl in *.s ; do
    flname="$(basename -- $fl)"
    flname="${flname%.*}"
    as $fl -o "$flname".o2
done
echo "Successfully compiled"
ar rcs libstd.a *.o *.o2
rm *.o