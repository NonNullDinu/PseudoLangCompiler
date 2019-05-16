section .bss
	INTERNAL____READ RESB 65536
	INTERNAL____CACHE RESQ 65536
	INTERNAL____READ_PTR RESQ 2
	a resq 1
	b resq 1
	
	
	;DO NOT EDIT
	;THIS FILE IS COMPUTER GENERATED
	;AS A RESULT OF THE COMPILATION OF "pseudo.psl"
section .rodata
	const10 dd 10
	digits db 48,49,50,51,52,53,54,55,56,57
	new_line DB 10
	___end DB "Process finished with exit code "
	___end_len equ $-___end
	str_1 DB " ", 0
section .text
	extern print_char
	extern printNumber
	extern printNewLine
	extern readValue
	extern readChar
	global _start
_start:
	call readValue
	mov [a], rax
	call readValue
	mov [b], rax
	mov r10, QWORD [a]
	mov rax, r10
	mov r8, 1
	call printNumber
	mov eax, 4
	mov ebx, 1
	mov ecx, str_1
	mov edx, 1
	int 0x80
	mov r10, QWORD [b]
	mov rax, r10
	mov r8, 1
	call printNumber
	call printNewLine
	mov eax, 4
	mov ebx, 1
	mov ecx, ___end
	mov edx, ___end_len
	int 0x80
	lea rax, [digits]
	call print_char
	call printNewLine
	mov eax, 1
	mov ebx, r10d
	int 0x80
