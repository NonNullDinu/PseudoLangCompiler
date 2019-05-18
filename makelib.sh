#!/usr/bin/env bash
cd lib
if [[ -f libstd.a ]]; then rm libstd.a; fi
for fl in *.s ; do
    flname="$(basename -- $fl)"
    flname="${flname%.*}"
    echo $fl
    as $fl -o "$flname".o -Wall
done
echo "Successfully compiled"
ar rcs libstd.a *.o
rm *.o