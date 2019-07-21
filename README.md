# Pseudocode Compiler



Pseudocode is a way of describing algorithms in a high-level language that
computers didn't use to understand. Until now that is. This is an implementation
that does just that. It understands **some** high level commands and 
translates them into GAS assembly(supports only linux syscalls).

## Program structure
IMPORTANT: A program **ALWAYS** finishes execution with an exit instruction.
If it does not then it is automatically appended and the compiler assumes the exit code is 0.
Note: in the syntax that follows:

\[x] means x is optional

&lt;x> means x is mandatory

- Parts that are not enclosed mean that they must be left just as they are
- This is case sensitive and so the token <code>if</code> will be treated differently than <code>iF</code>

### Declaring variables
Syntax:
<pre>&lt;type> &lt;var_name>[= &lt;value>]</pre>, where type is one of the following:
- <code>int</code> for integer types
- <code>file_stream</code> for file descriptors
- <code>pointer</code> for arrays

The value can only be assigned to integer types.

This method only accepts one variable declared / line, see [declare](#declare)

### Calling functions
The syntax for calling a function is:
<pre>&lt;name> [arg1[,arg2[,arg3[,...]]]]</pre>
Every function call instruction ends with a new line character ('\n', '0xA')

Functions from the standard library can be found
[here](#standard-library).

### Syntax of structures
#### Block instructions
Block instructions are blocks of code that are treated as a single instruction.

They begin with either <code>{</code>(as they do in some programming languages) or with <code>begin</code>.

They end with either <code>}</code> or with <code>end</code>.

Since both <code>{</code> and <code>begin</code> result in the same token at tokenization, they can be used interchangeably.The same can be said about <code>}</code> and <code>end</code>

#### Decision structures (if)
The decisions structures have the following syntax:
<pre>if(&lt;condition>)
then &lt;instructions_true>
[else &lt;instructions_false>]
</pre>
Here, instructions_true and/or instructions_false can be either one instruction or one block of instructions

#### Repetitive structures
##### While loops
Syntax:
<pre>while(&lt;condition>) do
    &lt;instructions>
</pre>

##### For loops
The syntax for <code>for</code> loops is as follows:
<pre>for &lt;var> &lt;- &lt;initial_value>, &lt;final_value>[,step] do
    &lt;instructions>
</pre>
Completely equivalent with:
<pre>&lt;var>&lt;-&lt;initial_value>
while(&lt;var>&lt;=&lt;final_value>) do
begin
    &lt;instructions>
    &lt;var> &lt;- &lt;var> + step
end
</pre>

Step has a default value of <code>1</code>.

# Standard library

## Exit
Finishes execution with the code of the first argument
Syntax:
<pre>exit [arg1]</pre>
arg1 defaults to 0 when not present.

## Declare
The method of declaring multiple variables in a single line is as follows:
<pre>declare &lt;var_1>[ = &lt;value1>][, var2[ = &lt;value2>][, var_3 [ = &lt;value3>] [, ...]]] of type &lt;type></pre>

It is equivalent to declaring each variable (with or without the initial value) on its own line, but it is also considered a function by the compiler for the most part.

## Writing to the console
Syntax:
<pre>write &lt;arg1>[,arg2[,arg3[,...]]]</pre>
Writes to the console the values of the arguments in order

## Reading from the console
Syntax:
<pre>read &lt;identifier1>[,identifier2[,...]]</pre>
Reads from the console and puts the values in the variables with those identifiers

## Working with files
### Open a file
Syntax:
<pre>open (&lt;file_name>, &lt;file_access>[, file_permissions]) as file &lt;file_descriptor></pre>
The file descriptor is a variable of type file_stream which can be read from or written to.

The file name is the name of the file that should be opened.

The file access is one of the following:
- read only(short "ro") for files that should only be read from.
- write only(short "wo") for files that should only be written to.

The file permissions parameter is required only if the file access is write only, else it is ignored

The file permissions is a base-8 number (from 3 digits):
4 for read, 2 for write and 1 for execute. Do an or operation on these digits for to get the desired value for the permission.

More on file permissions [here](https://en.wikipedia.org/wiki/File_system_permissions)

### Close a file
Syntax:
<pre>close file &lt;file_descriptor></pre>
The file descriptor is the file_stream that was opened.

### Writing to a file
Syntax is the same as writing to the console, but has at the end <code>to file &lt;file_descriptor></code>:
<pre>write &lt;arg1>[,arg2[,arg3[,...]]] to file &lt;file_descriptor></pre>

### Reading from a file
Same as writing to a file, has at the end <code>from file &lt;file_descriptor></code>
<pre>read &lt;identifier1>[,identifier2[,...]] from file &lt;file_descriptor></pre>

## Mathematical functions
### Checking if a number is prime
Syntax:
<pre>prime x, p</pre>
After the execution of this, p will have the value <code>1</code> if x is prime or <code>0</code> else.
p must be a variable.
### Checking if a number is perfect
Syntax:
<pre>perfect x, p</pre>
After the execution of this, p will have the value <code>1</code> if x is perfect or <code>0</code> else.
p must be a variable.

## Working with arrays
### Static allocation of an array
Syntax:
<pre>allocate &lt;name>, &lt;size></pre>
or
<pre>static_allocate &lt;name>, &lt;size></pre>

The parameter name is the name of the array.
The parameter size is a constant number(expressions not allowed here).

### Dynamic allocation
Syntax:
<pre>dynamic_allocate &lt;name>, &lt;size></pre>
The parameter name is the name of the array.
The parameter size is a constant number(expressions not allowed here).

### Dynamic deallocation
Syntax:
<pre>dynamic_deallocate &lt;name></pre>
Deallocates previously allocated memory for the array. 

### Addressing an element in the array
Syntax
<pre>&lt;name>[&lt;ind>]</pre>
Where name is the name of the array and the ind represents the index of the element that is going to be used.
ind can be either a constant number, a variable or an expression.

### Sorting an array of int types
If it is in increasing order, just call the sort function:
<pre>sort &lt;first>, &lt;length></pre>.
The parameters are:
- the parameter first represents the first byte of the array to be sorted:
an example here is array+1 -> translates to the address of the element array\[1].
- the parameter length represents the length of the array to be sorted - the count of elements.

### Reversing an array of int types
<pre>reverse &lt;m1>, &lt;count></pre>
This reverses the memory between m1 and m1+count*8.

Example: if we have an array A = (1, 2, 3, 7, 8, 9) and n is the size of the array(in this case 6), calling <code>reverse A, n</code> will transform A into (9, 8, 7, 3, 2, 1)

### Sorting and reversing an array of int types
This can be accomplished either by sorting and calling reverse on it or in the following way:
<pre>reverse_sort &lt;first>, &lt;length></pre>
The parameters are the same as on normal sorting, but the elements end up in decreasing order. 

Note that this method is faster than sorting normally and reversing.