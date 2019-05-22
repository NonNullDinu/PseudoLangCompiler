# SYSCALL numbers
.equ SYS_EXIT,   1
.equ SYS_READ,   3
.equ SYS_WRITE,  4
.equ SYS_OPEN,   5
.equ SYS_CLOSE,  6
.equ SYS_CREATE, 8

# STANDARD STREAMS
.equ STDIN,      0
.equ STDOUT,     1
.equ STDERR,     2


.section .text
.globl init
init:
    movq $0, INTERNAL____READ_PTR
    movq $1, %rdi
    movq $0, INTERNAL____READ_PTR(,%rdi,8)
    retq

.globl print_char
print_char:
	pushq %rax
	movl %eax, %ecx
	movl $SYS_WRITE, %eax
	movl %r8d, %ebx
	movl $1, %edx
	int $0x80
	popq %rax
	retq

.globl printNumber
printNumber:
	pushq %rax
	pushq %rdx
	xor %edx, %edx
	movq $10, %r10
	div %r10d
	test %eax, %eax
	je .l1
	call printNumber
.l1:
	movl $digits, %eax
	addl %edx, %eax
	call print_char
	popq %rdx
	popq %rax
	retq
.globl printNewLine
printNewLine:
	movl $SYS_WRITE, %eax
	movl %r8d, %ebx
	leal new_line, %ecx
	movl $1, %edx
	int $0x80
	retq
.globl readValue
readValue:
	movq INTERNAL____READ_PTR, %r11
	movl $1, %edi
	movq INTERNAL____READ_PTR(,%edi,8), %r12
	cmpq %r12, %r11
	jl .l4
	movl $SYS_READ, %eax
	movl %r8d, %ebx
	movl $INTERNAL____READ, %ecx
	movl $65536, %edx
	int $0x80
	movl %eax, %ebx
	cmpq $STDIN, %r8
	jne .l11
	subl $1, %ebx
.l11:
    movl $1, %edi
	movq %rbx, INTERNAL____READ_PTR(,%edi,8)
	movq $0, %r10
	jmp .l5
.l4:
	movq %r11, %r10
	movq %r12, %rbx
.l5:
	movq $0, %rax
.l2:
	movzx INTERNAL____READ(,%r10,1), %rcx
	cmpl $32, %ecx
	je .l3
	cmpl $10, %ecx
	je .l3
	subq $48, %rcx #get digit from character
	incq %r10
	movq $10, %r9
	mul %r9d
	addq %rcx, %rax
	cmpq %r10, %rbx
	jg .l2
.l3:
	addq $1, %r10
	movq %r10, INTERNAL____READ_PTR
	retq
.globl readChar
readChar:
	movq INTERNAL____READ_PTR, %r11
	movq $1, %rdi
	movq INTERNAL____READ_PTR(,%rdi,8), %r12
	cmpq %r12, %r11
	jl .l6
	movl $SYS_READ, %eax
	movl %r8d, %ebx
	movl INTERNAL____READ, %ecx
	movl $1, %edx
	int $0x80
	movl %eax, %ebx
	cmpq $STDIN, %r8
	jne .l12
	subl $1, %ebx
.l12:
    movq $1, %rdi
	movq %rbx, INTERNAL____READ_PTR(,%rdi,8)
	movq $0, %r10
	jmp .l7
.l6:
	movq %r11, %r10
.l7:
	movq INTERNAL____READ(,%r10,1), %rax
	addq $1, %r10
	movq %r10, INTERNAL____READ_PTR
	retq

.globl f_ro_open
f_ro_open:
    # File_ReadOnly_OPEN
    movq $0, %rcx
    movq %rbx, %rdx
    movq %rax, %rbx
    movq $SYS_OPEN, %rax
    int $0x80
    cmpq $0, %rax
    jl .l8
    # file exists
    retq
.l8:
    # file does not exist
    call exit
    retq

.globl f_wo_open
f_wo_open:
    # File_WriteOnly_OPEN
    movq $1, %rcx
    movq %rbx, %rdx
    movq %rax, %rbx
    movq $SYS_OPEN, %rax
    int $0x80
    cmpq $0, %rax
    jl .l9
    # file exists
    retq
.l9:
    # file does not exist
    movq $SYS_CREATE, %rax
    movq %rdx, %rcx
    int $0x80
    cmpq $0, %rax
    jg .l10
    call exit
.l10:
    retq

.globl f_close
f_close:
    movq %rax, %rbx
    movq $SYS_CLOSE, %rax
    int $0x80
    retq

.globl exit
exit:
    pushq %rax
    movq $SYS_WRITE, %rax
    movq $STDOUT, %rbx
    movq $___end, %rcx
    movq ___end_len, %rdx
    int $0x80
    popq %rax
    movq $1, %r8
    call printNumber
    pushq %rax
    call printNewLine
    movq $SYS_EXIT, %rax
    popq %rbx
    int $0x80
    retq

.section .bss
	.lcomm INTERNAL____READ, 65536
	.lcomm INTERNAL____CACHE, 524288
	.lcomm INTERNAL____READ_PTR, 16

.section .data
	digits:
	    .byte 48,49,50,51,52,53,54,55,56,57
	new_line:
	    .byte 10
	___end:
	    .ascii "Process finished with exit code "
	    .byte 0
	___end_len:
	    .int 32
