# Configuration
## The Standard Library
In the lib folder, run the following commands:
<pre>
gcc -c -fPIC -O3 stdlib.c
as stdlib.asm -o stdlib.o2
gcc --shared -o pseudo-std.dll ./*.o ./*.o2</pre>
The above lines will produce the needed pseudo-std.dll library.