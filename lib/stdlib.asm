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
or %rax, %rax
jns printNumber.l2
neg %rax
pushq   %rax
movq neg_sign@GOTPCREL(%rip), %rax
call _print_char@PLT
popq    %rax
printNumber.l2:
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
movq    %rdi, %rdx
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
je      ._exception_if_rax_negativel1
or      %rax, %rax
jns     ._exception_if_rax_negativel1
call    _exception@PLT
._exception_if_rax_negativel1:
retq

#.global                             _read_value
#.type                               _read_value, @function
#_read_value:
#.cfi_startproc
#movq    INTERNAL____READ_PTR@GOTPCREL(%rip), %r11
#movq    8(%r11), %r12
#movq    (%r11), %r11
#cmpq    %r12,%r11
#jl      readValue.l1
#movq    $65536, %rax
#call    _internal_read@PLT
#movq    %rax,%rbx
#cmpq    $STDIN, %r8
#jne     readValue.l3
#subq    $1, %rbx
#readValue.l3:
#movq    INTERNAL____READ_PTR@GOTPCREL(%rip), %r15
#movq    %rbx, 8(%r15)
#movq    $0, %r10
#jmp     readValue.l4
#readValue.l1:
#movq    %r11,%r10
#movq    %r12, %rbx
#readValue.l4:
#movq    INTERNAL____READ@GOTPCREL(%rip), %r15
#cmpq    $'-', (%r15, %r10)
#movq    $0, %r14
#jne     readValue.l8
#movq    $1, %r14
#addq    $1, %r10
#readValue.l8:
#movq    $0, %rax
#readValue.l5:
#movq    INTERNAL____READ@GOTPCREL(%rip), %rcx
#movzxb  (%rcx,%r10), %rcx
#cmpq    $0x20, %rcx
#je      readValue.l6
#cmpq    $0xa, %rcx
#je      readValue.l6
#movq    INTERNAL____READ_PTR@GOTPCREL(%rip), %r15
#cmpq    8(%r15), %r10
#jge     readValue.l6
#subq    $0x30, %rcx# rcx=rcx-'0'
#incq    %r10
#movq    $10, %r13
#mul     %r13
#addq    %rcx, %rax
#cmpq    %rbx, %r10
#jl      readValue.l5
#readValue.l6:
#incq    %r10
#movq    INTERNAL____READ_PTR@GOTPCREL(%rip), %r15
#cmpq    8(%r15), %r10
#jge     readValue.l7
#movq    INTERNAL____READ@GOTPCREL(%rip), %rcx
#movzxb  (%rcx, %r10), %rcx
#cmpq    $0xA, %rcx# '\n'
#je      readValue.l6
#cmpq    $0x20, %rcx# ' '
#je      readValue.l6
#readValue.l7:
#movq    INTERNAL____READ_PTR@GOTPCREL(%rip), %r15
#movq    %r10, (%r15)
#cmpq    $0, %r14
#je      readValue.l9
#neg     %rax
#readValue.l9:
#retq
#.cfi_endproc

        .globl  _read_value
        .type   _read_value, @function
_read_value:
.LFB0:
        .cfi_startproc
        pushq   %rbp
        .cfi_def_cfa_offset 16
        .cfi_offset 6, -16
        movq    %rsp, %rbp
        .cfi_def_cfa_register 6
        pushq   %r15
        pushq   %r14
        pushq   %r13
        pushq   %r12
        pushq   %rbx
        subq    $8, %rsp
        .cfi_offset 15, -24
        .cfi_offset 14, -32
        .cfi_offset 13, -40
        .cfi_offset 12, -48
        .cfi_offset 3, -56
        movl    $0, %r15d
        movq    INTERNAL____READ_PTR@GOTPCREL(%rip), %rax
        movl    (%rax), %ebx
        movq    INTERNAL____READ_PTR@GOTPCREL(%rip), %rax
        movl    4(%rax), %r13d
        cmpl    %r13d, %ebx
        jl      ._read_valueL2
        movl    $0, %ebx
        movl    $65536, %edi
        call    _internal_read@PLT
        movl    %eax, %r13d
        movq    INTERNAL____READ_PTR@GOTPCREL(%rip), %rax
        movl    %r13d, 4(%rax)
._read_valueL2:
        movq    INTERNAL____READ@GOTPCREL(%rip), %rdx
        movslq  %ebx, %rax
        movzbl  (%rdx,%rax), %eax
        cmpb    $45, %al
        jne     ._read_valueL3
        addl    $1, %ebx
        movl    $1, %r15d
._read_valueL3:
        movl    $0, %r12d
        jmp     ._read_valueL4
._read_valueL6:
        movq    INTERNAL____READ@GOTPCREL(%rip), %rdx
        movslq  %ebx, %rax
        movzbl  (%rdx,%rax), %r14d
        cmpb    $10, %r14b
        je      ._read_valueL5
        cmpb    $32, %r14b
        je      ._read_valueL5
        movq    %r12, %rax
        salq    $2, %rax
        addq    %r12, %rax
        addq    %rax, %rax
        movq    %rax, %r12
        movsbl  %r14b, %eax
        subl    $48, %eax
        cltq
        addq    %rax, %r12
        addl    $1, %ebx
._read_valueL4:
        cmpl    %r13d, %ebx
        jl      ._read_valueL6
._read_valueL5:
        addl    $1, %ebx
        jmp     ._read_valueL7
._read_valueL10:
        movq    INTERNAL____READ@GOTPCREL(%rip), %rdx
        movslq  %ebx, %rax
        movzbl  (%rdx,%rax), %eax
        cmpb    $10, %al
        je      ._read_valueL8
        movq    INTERNAL____READ@GOTPCREL(%rip), %rdx
        movslq  %ebx, %rax
        movzbl  (%rdx,%rax), %eax
        cmpb    $32, %al
        jne     ._read_valueL13
._read_valueL8:
        addl    $1, %ebx
._read_valueL7:
        cmpl    %r13d, %ebx
        jl      ._read_valueL10
        jmp     ._read_valueL9
._read_valueL13:
        nop
._read_valueL9:
        movq    INTERNAL____READ_PTR@GOTPCREL(%rip), %rax
        movl    %ebx, (%rax)
        cmpl    $1, %r15d
        jne     ._read_valueL11
        negq    %r12
._read_valueL11:
        movq    %r12, %rax
        addq    $8, %rsp
        popq    %rbx
        popq    %r12
        popq    %r13
        popq    %r14
        popq    %r15
        popq    %rbp
        .cfi_def_cfa 7, 8
        ret
        .cfi_endproc


.global                             _read_char
.type                               _read_char, @function
_read_char:
.cfi_startproc
movq    INTERNAL____READ_PTR@GOTPCREL(%rip), %r11
movq    (%r11), %r11
movq    8(%r11), %r12
cmpq    %r12, %r11
jl      ._read_charl1
movq    $1, %rax
call    _internal_read@PLT
call    _exception_if_rax_negative@PLT
movq    %rax,%rbx
cmpq    $STDIN, %r8
jne     ._read_charl3
subq    $1, %rbx
._read_charl3:
movq    INTERNAL____READ_PTR@GOTPCREL(%rip), %r15
movq    %rbx, 8(%r15)
movq    $0, %r10
jmp     ._read_charl4
._read_charl1:
movq    %r11, %r10
._read_charl4:
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
jl      ._f_wo_openl1
retq
._f_wo_openl1:
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
._reversel1:
call    _swap@PLT # swaps rcx and rdx
addq    $0x8, %rcx
subq    $0x8, %rdx
cmpq    %rdx, %rcx
jl      ._reversel1
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
#movq    __exit@GOTPCREL(%rip), %rax
#movq    $__exit_len, %rbx
#movq    $STDERR, %r8
#call    _print_string@PLT
#movq    (%rsp), %rax
#movq    $STDERR, %r8
#call    _print_number@PLT
#call    _print_new_line@PLT
popq    %rdi
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
jmp     _exception_if_rax_negative@PLT
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
jl      ._primefalse # 0 and 1
cmpq    $4, %rax
jl      ._primetrue # 2 and 3
movq    %rax, %r10
andq    $1, %r10
jz      ._primefalse # if arg & 1 == 0 then arg is odd
movq    %rax, %rbx
movq    $0, %rdx
movq    $6, %r10
idiv    %r10
cmpq    $1, %rdx
je      ._primel2
cmpq    $5, %rdx
je      ._primel2
jmp     ._primefalse # except for 2 and 3, there is no prime that cannot be written as 6n+1 or 6n-1
._primel2:
movq    $3, %r10 # i
._primel1:
movq    %r10, %rax
xorq    %rdx, %rdx
imul    %r10
cmpq    %rbx, %rax
jg      ._primetrue # i * i > arg
movq    %rbx, %rax
movq    $0, %rdx
idiv    %r10
cmpq    $0, %rdx
je      ._primefalse # arg % i == 0
addq    $2, %r10
jmp     ._primel1
._primetrue:
movq    $1, %rax
retq
._primefalse:
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
._div_suml1:
movq    %rbx, %rax
xorq    %rdx, %rdx
div     %rdi
test    %rdx, %rdx
jne     ._div_suml2
addq    %rdi, %rcx
._div_suml2:
incq    %rdi
cmpq    %rbx, %rdi
jle     ._div_suml1
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
.l1:
movq    %rbx, %rax
xorq    %rdx, %rdx
div     %rdi
test    %rdx, %rdx
jne     .l2
add     %rdi, %rcx
.l2:
incq    %rdi
test    %rdi, %rdi
je      .false
leaq    (,%rdi,2), %rax
cmpq    %rbx, %rax
jle     .l1
cmpq    %rbx, %rcx
jne     .false
.true:
movq    $1, %rax
retq
.false:
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
neg_sign:
	.byte '-'

	.globl	new_line
	.section	.rodata
	.type	new_line, @object
	.size	new_line, 1
new_line:
	.byte	10


	.comm INTERNAL____READ_PTR,    16,32
	.comm INTERNAL____READ,        65536,32
	.comm EXCEPTION, 8
