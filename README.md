# Pseudocode Compiler



Pseudocode is a way of describing algorithms in a high-level language that
computers didn't use to understand. Until now that is. This is an implementation
that does just that. It understands **some** high level commands and 
translates them into NASM assembly.

## Program structure
IMPORTANT: A program **ALWAYS** finishes execution with an exit instruction.
If it does not then it is automatically appended and the compiler assumes the exit code is 0.
Note: in the syntax that follows:
- \[x] means x is optional
- &lt;x> means x is mandatory
- Parts that are not enclosed mean that they must be left just as they are
- This is case sensitive and so the token <code>if</code> will be treated differently than <code>iF</code>
### Calling functions
The syntax for calling a function is:
<pre>&lt;name> [&lt;arg1>[,&lt;arg2>[,&lt;arg3>[,...]]]]</pre>
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
<pre>open file &lt;file_descriptor>, &lt;file_name>, &lt;file_access>[, file_permissions]</pre>
The file descriptor is a variable of type file_stream which can be read from or written to.

The file name is the name of the file that should be opened.

The file access is one of the following:
- read only(short "ro") for files that should only be read from.
- write only(short "wo") for files that should only be written to.

The file permissions parameter is required only if the file access is write only, else it is ignored

The file permissions is a base-8 number (from 3 digits):
4 for read, 2 for write and 1 for execute. Do an or operation on these digits for to get the desired value for the permission.

More on file permissions:[Wikipedia](https://en.wikipedia.org/wiki/File_system_permissions)

### Close a file
Syntax:
<pre>close file &lt;file_descriptor></pre>
The file descriptor is the file_stream that was opened.

### Writing to a file
Syntax is the same as writing to the console, but the first parameter is <code>file &lt;file_descriptor></code>:
<pre>write file &lt;file_descriptor>, &lt;arg1>[,arg2[,arg3[,...]]]</pre>

### Reading from a file
Same as writing to a file, the first parameter is <code>file &lt;file_descriptor></code>
<pre>read file &lt;file_descriptor>, &lt;identifier1>[,identifier2[,...]]</pre>