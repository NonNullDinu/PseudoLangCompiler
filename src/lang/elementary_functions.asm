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
	xor edx,edx
	div dword[const10]
	test eax,eax
	je .l1
	call printNumber
.l1:
	lea eax,[digits+edx]
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

