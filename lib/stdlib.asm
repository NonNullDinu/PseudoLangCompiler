 # Copyright (C) 2018-2019  Dinu Blanovschi
 #
 # This program is free software: you can redistribute it and/or modify
 # it under the terms of the GNU General Public License as published by
 # the Free Software Foundation, either version 3 of the License, or
 # (at your option) any later version.
 #
 # This program is distributed in the hope that it will be useful,
 # but WITHOUT ANY WARRANTY; without even the implied warranty of
 # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 # GNU General Public License for more details.
 #
 # You should have received a copy of the GNU General Public License
 # along with this program.  If not, see <https://www.gnu.org/licenses/>.

# SYSCALL numbers

.equ SYS_READ,                      0
.equ SYS_WRITE,                     1
.equ SYS_OPEN,                      2
.equ SYS_CLOSE,                     3
.equ SYS_EXIT,                      60
.equ SYS_CREATE,                    85

# STANDARD STREAMS
.equ STDIN,                         0
.equ STDOUT,                        1
.equ STDERR,                        2
.equ __exc_len,                     22
.equ __exit_len,                    32

.ifdef WIN
.extern
.endif

	.text
	.section	.rodata
__exc:
	.asciz                          "An exception occured: "

	.text
    .section	.rodata
__exit:
    .asciz                          "Process finished with exit code "

.text
.p2align 4,,15
.global                             _pseudo_stdlib_init
.type                               _pseudo_stdlib_init, @function
_pseudo_stdlib_init:
movq    INTERNAL____READ_PTR@GOTPCREL(%rip), %rdi
movq    $0x0, (%rdi)
movq    $0x0, 8(%rdi)
movq    EXCEPTION@GOTPCREL(%rip), %rdi
movq    $0x0, (%rdi)
retq

.global                             _exception
.type                               _exception, @function
_exception:
.cfi_startproc
negq    %rax
pushq   %rax
movq    EXCEPTION@GOTPCREL(%rip), %rdi
movq    %rax, (%rdi)
movq    __exc@GOTPCREL(%rip), %rax
movq    $__exc_len, %rbx
movq    $STDERR, %r8
call    _print_string@PLT
movq    (%rsp), %rax
movq    $STDERR, %r8
call    _print_number@PLT
call    _print_new_line@PLT
popq    %rax
jmp     _pseudo_exit@PLT
retq
.cfi_endproc

.global                             _print_char
.type                               _print_char, @function
_print_char:
.cfi_startproc
.ifdef WIN

movq    %rax, %rcx
movq    $1, %rdx
call    _win_print_string@PLT
call    _exception_if_rax_negative@PLT

.else
movq    %rax, %rsi
movq    $SYS_WRITE, %rax
movq    %r8, %rdi
movq    $1, %rdx
pushq   %r8
syscall
popq    %r8
call    _exception_if_rax_negative@PLT
mov     %rsi,%rax
.endif
ret
.cfi_endproc

.global                             _print_number
.type                               _print_number, @function
_print_number:
.cfi_startproc
pushq   %rax
pushq   %rdx
xor     %edx,%edx
movq    $10, %r15
idiv    %r15d
test    %eax,%eax
je      printNumber.l1
call    _print_number@PLT
printNumber.l1:
movq    digits@GOTPCREL(%rip), %rax
addq    %rdx, %rax
call    _print_char@PLT
popq    %rdx
popq    %rax
retq
.cfi_endproc

.global                             _print_bin_number
.type                               _print_bin_number, @function
_print_bin_number:
.cfi_startproc
pushq   %rax
pushq   %rdx
movq    %rax, %rdx
and     $1, %rdx
shr     $1, %rax
test    %rax,%rax
je      printBinNumber.l1
call    _print_bin_number@PLT
printBinNumber.l1:
movq    digits@GOTPCREL(%rip), %rax
addq    %rdx, %rax
call    _print_char@PLT
popq    %rdx
popq    %rax
retq
.cfi_endproc

.global                             _print_new_line
.type                               _print_new_line, @function
_print_new_line:
.cfi_startproc
movq    new_line@GOTPCREL(%rip), %rax
movq    $1, %rbx
call    _print_string@PLT
call    _exception_if_rax_negative@PLT
retq
.cfi_endproc

.type                               _internal_read, @function
_internal_read:
.ifdef WIN

movq    %rax, %rdx
movq    INTERNAL____READ@GOTPCREL(%rip), %rcx
call    _win_internal_read@PLT
call    _exception_if_rax_negative@PLT

.else
movq    %rax, %rdx
movq    $SYS_READ, %rax
movq    %r8, %rdi
movq    INTERNAL____READ@GOTPCREL(%rip), %rsi
syscall
call    _exception_if_rax_negative@PLT
.endif
retq

.global                             _exception_if_rax_negative
.type                               _exception_if_rax_negative, @function
_exception_if_rax_negative:
movq    EXCEPTION@GOTPCREL(%rip), %rdi
cmpq    $0, (%rdi)
je      _exception_if_rax_negative.l1
or      %rax, %rax
jns     _exception_if_rax_negative.l1
call    _exception@PLT
_exception_if_rax_negative.l1:
retq

.global                             _read_value
.type                               _read_value, @function
_read_value:
.cfi_startproc
movq    INTERNAL____READ_PTR@GOTPCREL(%rip), %r11
movq    8(%r11), %r12
movq    (%r11), %r11
cmpq    %r12,%r11
jl      readValue.l1
movq    $65536, %rax
call    _internal_read@PLT
movq    %rax,%rbx
cmpq    $STDIN, %r8
jne     readValue.l3
subq    $1, %rbx
readValue.l3:
movq    INTERNAL____READ_PTR@GOTPCREL(%rip), %r15
movq    %rbx, 8(%r15)
movq    $0, %r10
jmp     readValue.l4
readValue.l1:
movq    %r11,%r10
movq    %r12, %rbx
readValue.l4:
movq    $0, %rax
readValue.l5:
movq    INTERNAL____READ@GOTPCREL(%rip), %rcx
addq    %r10, %rcx
movzxb  (%rcx), %rcx
cmpq    $0x20, %rcx
je      readValue.l6
cmpq    $0xa, %rcx
je      readValue.l6
movq    INTERNAL____READ_PTR@GOTPCREL(%rip), %r15
cmpq    8(%r15), %r10
jge     readValue.l6
subq    $0x30, %rcx# rcx=rcx-'0'
incq    %r10
movq    $10, %r13
mul     %r13d
addq    %rcx, %rax
cmpq    %rbx, %r10
jl      readValue.l5
readValue.l6:
incq    %r10
movq    INTERNAL____READ_PTR@GOTPCREL(%rip), %r15
cmpq    8(%r15), %r10
jge     readValue.l7
movq    INTERNAL____READ@GOTPCREL(%rip), %rcx
addq    %r10, %rcx
movzxb  (%rcx), %rcx
cmpq    $0xA, %rcx# '\n'
je      readValue.l6
cmpq    $0x20, %rcx# ' '
je      readValue.l6
readValue.l7:
movq    INTERNAL____READ_PTR@GOTPCREL(%rip), %r15
movq    %r10, (%r15)
retq
.cfi_endproc

.global                             _read_char
.type                               _read_char, @function
_read_char:
.cfi_startproc
movq    INTERNAL____READ_PTR@GOTPCREL(%rip), %r11
movq    (%r11), %r11
movq    8(%r11), %r12
cmpq    %r12, %r11
jl      readChar.l1
movq    $1, %rax
call    _internal_read@PLT
call    _exception_if_rax_negative@PLT
movq    %rax,%rbx
cmpq    $STDIN, %r8
jne     readChar.l3
subq    $1, %rbx
readChar.l3:
movq    INTERNAL____READ_PTR@GOTPCREL(%rip), %r15
movq    %rbx, 8(%r15)
movq    $0, %r10
jmp     readChar.l4
readChar.l1:
movq    %r11, %r10
readChar.l4:
movq    INTERNAL____READ@GOTPCREL(%rip),%rax
addq    %r10, %rax
movzxb  (%rax), %rax
incq    %r10
movq    INTERNAL____READ_PTR@GOTPCREL(%rip), %r15
movq    %r10, (%r15)
retq
.cfi_endproc

.global                             _f_ro_open
.type                               _f_ro_open, @function
_f_ro_open:
.cfi_startproc
.ifdef WIN
movq    %rax, %rcx
call    _win_ro_open_file@PLT
.else
movq    $0, %rsi
movq    %rax, %rdi
movq    $SYS_OPEN, %rax
syscall
.endif
call    _exception_if_rax_negative@PLT
retq
.cfi_endproc

.global                             _f_wo_open
.type                               _f_wo_open, @function
_f_wo_open:
.cfi_startproc
.ifdef WIN
movq %rax, %rcx
call _win_wo_open_file@PLT
.else
movq    $577, %rsi # O_TRUNC | O_CREAT | O_WRONLY
movq    %rbx, %rdx
movq    %rax, %rdi
movq    $SYS_OPEN, %rax
syscall
cmpq    $0x0, %rax
jl      f_wo_open.l1
retq
f_wo_open.l1:
call    _exception@PLT
.endif
retq
.cfi_endproc

.global                             _f_close
.type                               _f_close, @function
_f_close:
.cfi_startproc
.ifdef WIN
movq %rax, %rcx
call _win_close@PLT
.else
movq    %rax, %rdi
movq    $SYS_CLOSE, %rax
syscall
call    _exception_if_rax_negative@PLT
.endif
retq
.cfi_endproc

.global                             _swap
.type                               _swap, @function
_swap:
.cfi_startproc
movq    (%rcx), %r9
movq    (%rdx), %r8
movq    %r8, (%rcx)
movq    %r9, (%rdx)
retq
.cfi_endproc

.global                             _reverse
.type                               _reverse, @function
_reverse:
.cfi_startproc
# rax = begin in memory
# rbx = size of array
movq    %rax, %rcx
leaq    (,%rbx,8), %rbx # rbx = size in memory
leaq    (%rax, %rbx), %rdx# rdx = end in memory
reverse.l1:
call    _swap@PLT # swaps rcx and rdx
addq    $0x8, %rcx
subq    $0x8, %rdx
cmpq    %rdx, %rcx
jl      reverse.l1
retq
.cfi_endproc

.global                             _pseudo_exit
.type                               _pseudo_exit, @function
_pseudo_exit:
.cfi_startproc
.ifdef WIN
movq    %rax, %rcx
call    _win_exit@PLT
.else
pushq   %rax
movq    __exit@GOTPCREL(%rip), %rax
movq    $__exit_len, %rbx
movq    $STDOUT, %r8
call    _print_string@PLT
movq    (%rsp), %rax
movq    $STDOUT, %r8
call    _print_number@PLT
call    _print_new_line@PLT
popq    %rdi
movq    $0, %rdi
movq    $SYS_EXIT, %rax
syscall
.endif
retq
.cfi_endproc

.global                             _print_string
.type                               _print_string, @function
_print_string:
# rax = address of first char
# rbx = size
# r8 = target file descriptor
.ifdef WIN
movq %rax, %rcx
movq %rbx, %rdx
call _win_print_string@PLT
.else
movq    %r8, %rdi
movq    %rax, %rsi
movq    %rbx, %rdx
pushq   %r8
movq    $SYS_WRITE, %rax
syscall
popq    %r8
or      %rax, %rax
jns     printString.l1
call    _exception@PLT
printString.l1:
.endif
retq

## Template:
# .globl <name>
# .type <name>, @function
# <name
#   .cfi_startproc
#   <code>
#   .cfi_endproc

.globl _prime
.type _prime, @function
_prime:
.cfi_startproc
#rax = value
cmpq    $2, %rax
jl      _prime.false # 0 and 1
cmpq    $4, %rax
jl      _prime.true # 2 and 3
movq    %rax, %r10
andq    $1, %r10
jz      _prime.false # if arg & 1 == 0 then arg is odd
movq    %rax, %rbx
movq    $0, %rdx
movq    $6, %r10
idiv    %r10
cmpq    $1, %rdx
je      _prime.l2
cmpq    $5, %rdx
je      _prime.l2
jmp     _prime.false # except for 2 and 3, there is no prime that cannot be written as 6n+1 or 6n-1
_prime.l2:
movq    $3, %r10 # i
_prime.l1:
movq    %r10, %rax
xorq    %rdx, %rdx
imul    %r10
cmpq    %rbx, %rax
jg      _prime.true # i * i > arg
movq    %rbx, %rax
movq    $0, %rdx
idiv    %r10
cmpq    $0, %rdx
je      _prime.false # arg % i == 0
addq    $2, %r10
jmp     _prime.l1
_prime.true:
movq    $1, %rax
retq
_prime.false:
movq    $0, %rax
retq
.cfi_endproc
.size _prime, .-_prime

.global                             _div_sum
.type                               _div_sum, @function
_div_sum:
.cfi_startproc
# rbx = arg
# rcx = return value
movq    $1, %rcx
movq    $2, %rdi # i
_div_sum.l1:
movq    %rbx, %rax
xorq    %rdx, %rdx
div     %rdi
test    %rdx, %rdx
jne     _div_sum.l2
addq    %rdi, %rcx
_div_sum.l2:
incq    %rdi
cmpq    %rbx, %rdi
jle     _div_sum.l1
retq
.cfi_endproc
.size                               _div_sum, .-_div_sum

.global                             _perfect
.type                               _perfect, @function
_perfect:
# rax = value
movq    %rax, %rbx
movq    $1, %rcx
movq    $2, %rdi
_perfect.l1:
movq    %rbx, %rax
xorq    %rdx, %rdx
div     %rdi
test    %rdx, %rdx
jne     _perfect.l2
add     %rdi, %rcx
_perfect.l2:
incq    %rdi
test    %rdi, %rdi
je      _perfect.false
leaq    (,%rdi,2), %rax
cmpq    %rbx, %rax
jle     _perfect.l1
cmpq    %rbx, %rcx
jne     _perfect.false
_perfect.true:
movq    $1, %rax
retq
_perfect.false:
movq    $0, %rax
retq
.size                               _perfect, .-_perfect

	.section    .rodata
	.globl  digits
	.type   digits, @object
	.align 16
digits:
	.byte 48
	.byte 49
	.byte 50
	.byte 51
	.byte 52
	.byte 53
	.byte 54
	.byte 55
	.byte 56
	.byte 57

	.globl	new_line
	.section	.rodata
	.type	new_line, @object
	.size	new_line, 1
new_line:
	.byte	10


	.comm INTERNAL____READ_PTR,    16,32
	.comm INTERNAL____READ,        65536,32
	.comm EXCEPTION, 8
