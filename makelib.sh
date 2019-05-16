#!/usr/bin/env bash
cd lib
for fl in *.asm ; do
    flname="$(basename -- $fl)"
    flname="${flname%.*}"
    nasm -f elf64 $fl -o "$flname".o -Wall
done
echo "Successfully compiled"
ar rcs libstd.a *.o
rm *.o