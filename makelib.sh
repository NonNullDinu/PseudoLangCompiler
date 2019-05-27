#!/usr/bin/env bash
cd lib
if [[ -f libstd.a ]]; then rm libstd.a; fi
for fl in *.asm ; do
    flname="$(basename -- $fl)"
    flname="${flname%.*}"
    as $fl -o "$flname".o
done
echo "Successfully compiled"
ar rcs libstd.a *.o
rm *.o