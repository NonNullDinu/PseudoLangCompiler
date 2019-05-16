section .bss
	INTERNAL____READ RESB 19
	a resq 1
	INTERNAL____CACHE RESQ 65536
	
	
	;DO NOT EDIT
	;THIS FILE IS COMPUTER GENERATED
	;AS A RESULT OF THE COMPILATION OF "pseudo.psl"
section .rodata
	const10 dd 10
	digits db 48,49,50,51,52,53,54,55,56,57
	new_line DB 10
	___end DB "Process finished execution and returned code "
	___end_len equ $-___end
section .text
print_char:
	push rax
	mov ecx, eax
	mov eax, 4
	mov ebx, r8d
	mov edx, 1
	int 0x80
	pop rax
	ret
printNumber:
	push rax
	push rdx
	xor edx, edx
	div dword[const10]
	test eax, eax
	je .l1
	call printNumber
.l1:
	lea eax, [digits+edx]
	call print_char
	pop rdx
	pop rax
	ret
printNewLine:
	mov eax, 4
	mov ebx, 1
	mov ecx, new_line
	mov edx, 1
	int 0x80
	ret
readValue:
	mov eax, 3
	mov ebx, 0
	mov ecx, INTERNAL____READ
	mov edx, 18
	int 0x80
	mov ebx, eax
	sub ebx, 1
	mov r10, 0
	mov rax, 0
.l2:
	movzx rcx, BYTE [INTERNAL____READ + r10]
	sub rcx, '0'
	inc r10
	mul DWORD [const10]
	add rax, rcx
	cmp r10d, ebx
	jl .l2
	ret
	global _start
_start:
	mov r10, 0
	add r10, 1
	mov QWORD [a], 1
	mov r10, 1
	add r10, 2
	mov rax, 3
	mov r8, 1
	call printNumber
	call printNewLine
	mov rax, 1
	mov rbx, 0
	int 0x80
