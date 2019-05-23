; SYSCALL numbers

SYS_READ   equ 0
SYS_WRITE  equ 1
SYS_OPEN   equ 2
SYS_CLOSE  equ 3
SYS_EXIT   equ 60
SYS_CREATE equ 85

; STANDARD STREAMS
STDIN      equ 0
STDOUT     equ 1
STDERR     equ 2

section .text
global init
init:
    mov QWORD [INTERNAL____READ_PTR], 0
    mov QWORD [INTERNAL____READ_PTR+8], 0
    ret

global exception
exception:
    neg rax
    push rax
    mov eax, SYS_WRITE
    mov rdi, STDERR
    mov rsi, ___exc
    mov rdx, ___exc_len
    syscall

    mov rax, QWORD [rsp]
    mov r8, STDERR
    call printNumber
    call printNewLine

    pop rax
    call exit
    ret

global print_char
print_char:
	mov rsi, rax
	mov rax, SYS_WRITE
	mov rdi, r8
	mov rdx, 1
	push r8
	syscall
	pop r8
	or rax, rax
	jns .l16
	call exception
.l16:
	mov rax, rsi
	ret

global printNumber
printNumber:
	push rax
	push rdx
	xor edx, edx
	idiv dword[const10]
	test eax, eax
	je .l1
	call printNumber
.l1:
	lea rax, [digits+rdx]
	call print_char
	pop rdx
	pop rax
	ret

global printNewLine
printNewLine:
	mov rax, SYS_WRITE
	mov rdi, r8
	mov rsi, new_line
	mov rdx, 1
	syscall
	or rax, rax
	jns .l15
	call exception
.l15:
	ret

global readValue
readValue:
	mov r11, QWORD [INTERNAL____READ_PTR]
	mov r12, QWORD [INTERNAL____READ_PTR+8]
	cmp r11, r12
	jl .l4
	mov rax, SYS_READ
	mov rdi, r8
	mov rsi, INTERNAL____READ
	mov rdx, 65536
	syscall
	or rax, rax
	jns .l14
	call exception
.l14:
	mov rbx, rax
	cmp r8, STDIN
	jne .l11
	sub rbx, 1
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
	cmp r10, [INTERNAL____READ_PTR + 8]
	jge .l3
	sub rcx, '0'; 0x30
	inc r10
	mul DWORD [const10]
	add rax, rcx
	cmp r10d, ebx
	jl .l2
.l3:
	inc r10
	cmp r10, [INTERNAL____READ_PTR + 8]
	jge .l13
	movzx rcx, BYTE [INTERNAL____READ + r10]
	cmp rcx, 10
	je .l3
	cmp rcx, 32
	je .l3
.l13:
	mov QWORD [INTERNAL____READ_PTR], r10
	ret

global readChar
readChar:
	mov r11, QWORD [INTERNAL____READ_PTR]
	mov r12, QWORD [INTERNAL____READ_PTR+8]
	cmp r11, r12
	jl .l6
	mov rax, SYS_READ
	mov rdi, r8
	mov rsi, INTERNAL____READ
	mov rdx, 1
	syscall
	or rax, rax
	jns .l17
	call exception
.l17:
	mov rbx, rax
	cmp r8, STDIN
	jne .l12
	sub rbx, 1
.l12:
	mov QWORD [INTERNAL____READ_PTR+8], rbx
	mov r10, 0
	jmp .l7
.l6:
	mov r10, r11
.l7:
	mov rax, QWORD [INTERNAL____READ + r10]
	inc r10
	mov QWORD [INTERNAL____READ_PTR], r10
	ret

global f_ro_open
f_ro_open:
    ; File_ReadOnly_OPEN
    mov rsi, 0; O_RDONLY
    mov rdx, rbx
    mov rdi, rax
    mov rax, SYS_OPEN
    syscall
    cmp rax, 0
    jl .l8
    ; file exists
    ret
.l8:
    ; file does not exist
    call exception
    ret

global f_wo_open
f_wo_open:
    ; File_WriteOnly_OPEN
    mov rsi, 577; O_CREAT | O_WRONLY | O_TRUNC
    mov rdx, rbx
    mov rdi, rax
    mov rax, SYS_OPEN
    syscall
    cmp rax, 0
    jl .l9
    ; file could be opened
    ret
.l9:
    ; file could not be opened
    call exception
    ret

global f_close
f_close:
    mov rdi, rax
    mov rax, SYS_CLOSE
    syscall
	or rax, rax
	jns .l18
	call exception
.l18:
    ret

global exit
exit:
    push rax
    mov r8, STDOUT
    mov rax, SYS_WRITE
    mov rdi, STDOUT
    mov rsi, ___end
    mov rdx, ___end_len
    syscall
    mov rax, QWORD [rsp]
    call printNumber
    call printNewLine
    mov rax, SYS_EXIT
    pop rdi
    syscall
    ret

section .bss
	INTERNAL____READ RESB 65536
	INTERNAL____CACHE RESQ 65536
	INTERNAL____READ_PTR RESQ 2

section .rodata
	const10 dd 10
	digits db 48,49,50,51,52,53,54,55,56,57
	new_line DB 10
	___end DB "Process finished with exit code ", 0
	___end_len equ $-___end
	___exc DB "An exception occurred: ", 0
	___exc_len equ $-___exc