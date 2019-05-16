section .text
global print_char
print_char:
	push rax
	mov ecx, eax
	mov eax, 4
	mov ebx, r8d
	mov edx, 1
	int 0x80
	pop rax
	ret
global printNumber
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
global printNewLine
printNewLine:
	mov eax, 4
	mov ebx, 1
	mov ecx, new_line
	mov edx, 1
	int 0x80
	ret
global readValue
readValue:
	mov r11, QWORD [INTERNAL____READ_PTR]
	mov r12, QWORD [INTERNAL____READ_PTR+8]
	cmp r11, r12
	jl .l4
	mov eax, 3
	mov ebx, 0
	mov ecx, INTERNAL____READ
	mov edx, 65536
	int 0x80
	mov ebx, eax
	sub ebx, 1
	mov QWORD [INTERNAL____READ_PTR+8], rbx
	mov r10, 0
	jmp .l5
.l4:
	mov r10, r11
	mov rbx, r12
.l5:
	mov rax, 0
.l2:
	movzx rcx, BYTE [INTERNAL____READ + r10]
	cmp rcx, 32
	je .l3
	cmp rcx, 10
	je .l3
	sub rcx, '0'
	inc r10
	mul DWORD [const10]
	add rax, rcx
	cmp r10d, ebx
	jl .l2
.l3:
	add r10, 1
	mov QWORD [INTERNAL____READ_PTR], r10
	ret
global readChar
readChar:
	mov r11, QWORD [INTERNAL____READ_PTR]
	mov r12, QWORD [INTERNAL____READ_PTR+8]
	cmp r11, r12
	jl .l6
	mov eax, 3
	mov ebx, 0
	mov ecx, INTERNAL____READ
	mov edx, 1
	int 0x80
	mov ebx, eax
	sub ebx, 1
	mov QWORD [INTERNAL____READ_PTR+8], rbx
	mov r10, 0
	jmp .l7
.l6:
	mov r10, r11
.l7:
	mov rax, QWORD [INTERNAL____READ + r10]
	add r10, 1
	mov QWORD [INTERNAL____READ_PTR], r10
	ret

section .bss
	INTERNAL____READ RESB 65536
	INTERNAL____CACHE RESQ 65536
	INTERNAL____READ_PTR RESQ 2

section .rodata
	const10 dd 10
	digits db 48,49,50,51,52,53,54,55,56,57
	new_line DB 10