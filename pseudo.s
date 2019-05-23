.section .bss
	.lcomm INTERNAL____CACHE, 524288
	.lcomm var_0, 8
	.lcomm var_1, 2
	.lcomm var_2, 2
	.lcomm var_3, 8
	.lcomm var_4, 8
	
	
	#DO NOT EDIT
	#THIS FILE IS COMPUTER GENERATED
	#AS A RESULT OF THE COMPILATION OF "pseudo.psl"
.section .rodata
file_1_path:
	.ascii "pseudo.in"
	.zero 1
file_2_path:
	.ascii "pseudo.out"
	.zero 1
.section .text
	.extern print_char
	.extern printNumber
	.extern printNewLine
	.extern readValue
	.extern readChar
	.extern exit
	.extern f_ro_open
	.extern f_wo_open
	.extern f_close
	.extern init
	.globl _start
_start:
	call init
	movq $file_1_path, %rax
	call f_ro_open
	movw %ax, var_1
	movq $file_2_path, %rax
	movq $0744, %rbx
	call f_wo_open
	movw %ax, var_2
	movzxw var_1, %r8
	call readValue
	movq %rax, var_4
	movq $1, %r10
	movq %r10, var_3
WHILE_1:
	movq var_3, %r10
	movq var_4, %r11
	cmpq %r11, %r10
	movq $0, %r10
	jg .LOGIC_1
	movq $1, %r10
.LOGIC_1:
	cmpq $0, %r10
	je WHILE_1_END
	movzxw var_1, %r8
	call readValue
	movq %rax, var_0
	movzxw var_2, %r8
	movq var_0, %r10
	mov %r10, %rax
	call printNumber
	call printNewLine
	movq $1, %r10
	addq %r10, var_3
	jmp WHILE_1
WHILE_1_END:
	movzxw var_1, %rax
	call f_close
	movzxw var_2, %rax
	call f_close
	movq $0, %rax
	call exit
