; SYSCALL numbers

SYS_EXIT   equ 1
SYS_READ   equ 3
SYS_WRITE  equ 4
SYS_OPEN   equ 5
SYS_CLOSE  equ 6
SYS_CREATE equ 8

; STANDARD STREAMS
STDIN      equ 0
STDOUT     equ 1
STDERR     equ 2


section .text
global print_char
print_char:
	push rax
	mov ecx, eax
	mov eax, SYS_WRITE
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
	mov eax, SYS_WRITE
	mov ebx, STDOUT
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
	mov eax, SYS_READ
	mov ebx, r8d
	mov ecx, INTERNAL____READ
	mov edx, 65536
	int 0x80
	mov ebx, eax
	cmp r8, STDIN
	jne .l11
	sub ebx, 1
.l11:
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
	mov eax, SYS_READ
	mov ebx, r8d
	mov ecx, INTERNAL____READ
	mov edx, 1
	int 0x80
	mov ebx, eax
	cmp r8, STDIN
	jne .l12
	sub ebx, 1
.l12:
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

global f_ro_open
f_ro_open:
    ; File_ReadOnly_OPEN
    mov rcx, 0
    mov rdx, rbx
    mov rbx, rax
    mov rax, SYS_OPEN
    int 0x80
    cmp rax, 0
    jl .l8
    ; file exists
    ret
.l8:
    ; file does not exist
    call exit
    ret

global f_wo_open
f_wo_open:
    ; File_WriteOnly_OPEN
    mov rcx, 1
    mov rdx, rbx
    mov rbx, rax
    mov rax, SYS_OPEN
    int 0x80
    cmp rax, 0
    jl .l9
    ; file exists
    ret
.l9:
    ; file does not exist
    mov rax, SYS_CREATE
    mov rcx, rdx
    int 0x80
    cmp rax, 0
    jg .l10
    call exit
.l10:
    ret

global f_close
f_close:
    mov rbx, rax
    mov rax, SYS_CLOSE
    int 0x80
    ret

global exit
exit:
    push rax
    mov rax, SYS_WRITE
    mov rbx, STDOUT
    mov rcx, ___end
    mov rdx, ___end_len
    int 0x80
    pop rax
    call printNumber
    push rax
    call printNewLine
    mov rax, SYS_EXIT
    pop rbx
    int 0x80
    ret

section .bss
	INTERNAL____READ RESB 65536
	INTERNAL____CACHE RESQ 65536
	INTERNAL____READ_PTR RESQ 2

section .rodata
	const10 dd 10
	digits db 48,49,50,51,52,53,54,55,56,57
	new_line DB 10
	___end DB "Process finished with exit code "
	___end_len equ $-___end