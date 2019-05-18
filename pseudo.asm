section .bss
	INTERNAL____READ RESB 65536
	INTERNAL____CACHE RESQ 65536
	INTERNAL____READ_PTR RESQ 2
	a resq 1
	fl resw 1
	i resq 1
	
	
	;DO NOT EDIT
	;THIS FILE IS COMPUTER GENERATED
	;AS A RESULT OF THE COMPILATION OF "pseudo.psl"
section .rodata
	file_1_path DB "pseudo.in", 0
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
	mov rbx, 0q744
	call f_ro_open
	mov WORD [fl], ax
	mov r10, 1
	mov QWORD [i], 1
.WHILE_1:
	mov r10, QWORD [i]
	mov r11, 9
	cmp r10, 9
	jg .LOGIC_1_FALSE
	mov r10, 1
	jmp .LOGIC_1_END
.LOGIC_1_FALSE:
	mov r10, 0
.LOGIC_1_END:
	cmp r10, 0
	je .WHILE_1_END
	movzx r8, WORD [fl]
	call readValue
	mov QWORD [a], rax
	mov r10, QWORD [a]
	mov rax, r10
	mov r8, 1
	call printNumber
	call printNewLine
	mov r10, QWORD [i]
	mov r11, 2
	add r10, 2
	mov QWORD [i], r10
	jmp .WHILE_1
.WHILE_1_END:
	movzx rax, WORD [fl]
	call f_close
	mov rax, 0
	call exit
