section .bss
	INTERNAL____CACHE RESQ 65536
	var_0 resq 1
	var_1 resw 1
	var_2 resw 1
	var_3 resq 1
	var_4 resq 1
	
	
	;DO NOT EDIT
	;THIS FILE IS COMPUTER GENERATED
	;AS A RESULT OF THE COMPILATION OF "pseudo.psl"
section .rodata
	file_1_path DB "pseudo.in", 0
	file_2_path DB "pseudo.out", 0
section .text
	extern print_char
	extern printNumber
	extern printNewLine
	extern readValue
	extern readChar
	extern exit
	extern f_ro_open
	extern f_wo_open
	extern f_close
	extern init
	global _start
_start:
	call init
	mov rax, file_1_path
	call f_ro_open
	mov WORD [var_1], ax
	mov rax, file_2_path
	mov rbx, 0q744
	call f_wo_open
	mov WORD [var_2], ax
	movzx r8, WORD [var_1]
	call readValue
	mov QWORD [var_4], rax
	mov r10, 1
	mov QWORD [var_3], 1
.WHILE_1:
	mov r10, QWORD [var_3]
	cmp r10, QWORD [var_4]
	mov r10, 0
	jg .LOGIC_1
	mov r10, 1
.LOGIC_1:
	cmp r10, 0
	je .WHILE_1_END
	movzx r8, WORD [var_1]
	call readValue
	mov QWORD [var_0], rax
	movzx r8, WORD [var_2]
	mov r10, QWORD [var_0]
	mov rax, r10
	call printNumber
	call printNewLine
	mov r10, 1
	add QWORD [var_3], 1
	jmp .WHILE_1
.WHILE_1_END:
	movzx rax, WORD [var_1]
	call f_close
	movzx rax, WORD [var_2]
	call f_close
	mov rax, 0
	call exit
