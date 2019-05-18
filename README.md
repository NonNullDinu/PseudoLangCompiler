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
- <x> means x is mandatory
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
//TODO: COMPLETE