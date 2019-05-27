# SYSCALL numbers

.equ SYS_READ,                      0
.equ SYS_WRITE,                     1
.equ SYS_OPEN,                      2
.equ SYS_CLOSE,                     3
.equ SYS_EXIT,                      60
.equ SYS_CREATE,                    85

# STANDARD STREAMS
.equ STDIN,                         0
.equ STDOUT,                        1
.equ STDERR,                        2

.section .bss
    .lcomm INTERNAL____READ_PTR,    16
    .lcomm INTERNAL____READ,        65536
    .lcomm MERGE_MEMORY,            524288

.section .rodata
    __exc:
        .asciz                      "An exception occured: "
    __exc_len:
        .long                       23
    const10:
        .word                       10
    digits:
        .byte                       48, 49, 50, 51, 52, 53, 54, 55, 56, 57
    new_line:
        .byte                       10
    __exit:
        .asciz                      "Process finished with exit code "
    __exit_len:
        .long                       33
.section .text

.global init
.type init, @function
init:
movq   $0x0, INTERNAL____READ_PTR
movq   $0x0, INTERNAL____READ_PTR+8
retq

.global exception
.type exception, @function
exception:
negq    %rax
pushq   %rax
movq    $SYS_WRITE, %rax
movq    $STDERR, %rdi
movq    $__exc, %rsi
movq    __exc_len, %rdx
syscall
movq    (%rsp), %rax
movl    $2, %r8d
call    printNumber
call    printNewLine
popq    %rax
call    exit
retq

.global print_char
.type print_char, @function
print_char:
movq    %rax, %rsi
movq    $SYS_WRITE, %rax
movq    %r8, %rdi
movq    $1, %rdx
pushq   %r8
syscall
popq    %r8
or      %rax,%rax
jns     print_char.l1
call    exception
print_char.l1:
mov     %rsi,%rax
ret

.global printNumber
.type printNumber, @function
printNumber:
pushq   %rax
pushq   %rdx
xor     %edx,%edx
movq    $10, %r15
idiv    %r15d
test    %eax,%eax
je      printNumber.l1
call    printNumber
printNumber.l1:
leaq    digits(%rdx), %rax
call    print_char
popq    %rdx
popq    %rax
retq

.global printNewLine
.type printNewLine, @function
printNewLine:
movq    $SYS_WRITE, %rax
movq    %r8, %rdi
movq    $new_line, %rsi
movq    $1, %rdx
syscall
or      %rax,%rax
jns     printNewLine.l1
call    exception
printNewLine.l1:
retq

.global readValue
.type readValue, @function
readValue:
movq    INTERNAL____READ_PTR, %r11
movq    INTERNAL____READ_PTR+8, %r12
cmpq    %r12,%r11
jl      readValue.l1
movq    $SYS_READ, %rax
movq    %r8, %rdi
movq    $INTERNAL____READ, %rsi
movq    $65536, %rdx
syscall
or      %rax,%rax
jns     readValue.l2
call    exception
readValue.l2:
movq    %rax,%rbx
cmpq    $STDIN, %r8
jne     readValue.l3
subq    $1, %rbx
readValue.l3:
movq    %rbx, INTERNAL____READ_PTR+8
movq    $0, %r10
jmp     readValue.l4
readValue.l1:
movq    %r11,%r10
movq    %r12, %rbx
readValue.l4:
movq    $0, %rax
readValue.l5:
movzxb  INTERNAL____READ(%r10), %rcx
cmpq    $0x20, %rcx
je      readValue.l6
cmpq    $0xa, %rcx
je      readValue.l6
cmpq    INTERNAL____READ_PTR+8, %r10
jge     readValue.l6
subq    $0x30, %rcx# rcx=rcx-'0'
incq    %r10
movq    $10, %r13
mul     %r13d
addq    %rcx, %rax
cmpq    %rbx, %r10
jl      readValue.l5
readValue.l6:
incq    %r10
cmpq    INTERNAL____READ_PTR+8, %r10
jge     readValue.l7
movzxb  INTERNAL____READ(%r10), %rcx
cmpq    $0xA, %rcx# '\n'
je      readValue.l6
cmpq    $0x20, %rcx# ' '
je      readValue.l6
readValue.l7:
movq    %r10, INTERNAL____READ_PTR
retq

.global readChar
.type readChar, @function
readChar:
movq    INTERNAL____READ_PTR, %r11
movq    INTERNAL____READ_PTR+8, %r12
cmpq    %r12, %r11
jl      readChar.l1
movq    $SYS_READ, %rax
movq    %r8, %rdi
movq    $INTERNAL____READ, %rsi
movq    $1, %rdx
syscall
or      %rax,%rax
jns     readChar.l2
call    exception
readChar.l2:
movq    %rax,%rbx
cmpq    $STDIN, %r8
jne     readChar.l3
subq    $1, %rbx
readChar.l3:
movq    %rbx, INTERNAL____READ_PTR+8
movq    $0, %r10
jmp     readChar.l4
readChar.l1:
movq    %r11, %r10
readChar.l4:
movzxb  INTERNAL____READ(%r10),%rax
incq    %r10
movq    %r10, INTERNAL____READ_PTR
retq

.global f_ro_open
.type f_ro_open, @function
f_ro_open:
movq    $0, %rsi
movq    %rbx, %rdx
movq    %rax, %rdi
movq    $SYS_OPEN, %rax
syscall
cmpq    $0, %rax
jl      f_ro_open.l1
retq
f_ro_open.l1:
call    exception
retq

.global f_wo_open
.type f_wo_open, @function
f_wo_open:
movq    $577, %rsi # O_TRUNC | O_CREAT | O_WRONLY
movq    %rbx, %rdx
movq    %rax, %rdi
movq    $SYS_OPEN, %rax
syscall
cmpq    $0x0, %rax
jl      f_wo_open.l1
retq
f_wo_open.l1:
call    exception
retq

.global f_close
.type f_close, @function
f_close:
movq    %rax, %rdi
movq    $SYS_CLOSE, %rax
syscall
or      %rax, %rax
jns     f_close.l1
call    exception
f_close.l1:
retq

.global swap
.type swap, @function
swap:
movq    (%rcx), %r9
movq    (%rdx), %r8
movq    %r8, (%rcx)
movq    %r9, (%rdx)
retq

.global sort
.type sort, @function
sort:
subq    %rax, %rbx
sort.l1:
movq    $0x1, %r10
movq    $0x0, %r11
sort.l2:
movq    (%rax, %r10, 8), %r8
cmpq    -8(%rax, %r10, 8), %r8
jge     sort.l3
leaq    (%rax, %r10, 8), %rcx
leaq    -8(%rcx), %rdx
call    swap
movq    $0x1, %r11
sort.l3:
incq    %r10
cmpq    %rbx, %r10
jl      sort.l2
cmpq    $0x0, %r11
jne     sort.l1
retq

.global reverse_sort
.type reverse_sort, @function
reverse_sort:
movq    $0x0, %r11
subq    %rax,%rbx
reverse_sort.l1:
movq    $0x1, %r10
movq    $0x0, %r11
reverse_sort.l2:
movq    (%rax, %r10, 8), %r8
cmpq    -0x8(%rax, %r10, 8), %r8
jle     reverse_sort.l3
leaq    (%rax, %r10, 8), %rcx
leaq    -0x8(%rax, %r10, 8), %rdx
call    swap
movq    $0x1, %r11
reverse_sort.l3:
incq    %r10
cmpq    %rbx, %r10
jl      reverse_sort.l2
cmpq    $0x0, %r11
jne     reverse_sort.l1
retq

.global reverse
.type reverse, @function
reverse:
# rax = begin in memory
# rbx = begin in memory + size + 1
movq    %rax, %rcx
subq    $0x1, %rbx# rbx = begin in memory + size
subq    %rax, %rbx# rbx = size
leaq    (,%rbx,8), %rbx# rbx = size in memory
leaq    (%rax, %rbx), %rdx# rdx = end in memory
reverse.l1:
call    swap # swaps rcx and rdx
addq    $0x8, %rcx
subq    $0x8, %rdx
cmpq    %rdx, %rcx
jl      reverse.l1
retq

.global exit
.type exit, @function
exit:
pushq   %rax
movq    $STDOUT, %r8
movq    $SYS_WRITE, %rax
movq    $STDOUT, %rdi
movq    $__exit, %rsi
movq    __exit_len, %rdx
syscall
movq    (%rsp), %rax
call    printNumber
call    printNewLine
movq    $SYS_EXIT, %rax
popq    %rdi
syscall
retq

        # COMPILED C CODE
        # ONWARD
        .globl  merge
        .type   merge, @function
merge:
.LFB6:
        .cfi_startproc
        pushq   %rbp
        .cfi_def_cfa_offset 16
        .cfi_offset 6, -16
        movq    %rsp, %rbp
        .cfi_def_cfa_register 6
        pushq   %r14
        pushq   %r13
        pushq   %r12
        pushq   %rbx
        .cfi_offset 14, -24
        .cfi_offset 13, -32
        .cfi_offset 12, -40
        .cfi_offset 3, -48
        movq    %rsi, %rax
        movl    $0, %r12d
        movl    $0, %r13d
        movl    $0, %ebx
        leaq    MERGE_MEMORY(%rip), %r14
        jmp     .L2
.L5:
        movq    %r12, %rsi
        salq    $3, %rsi
        addq    %rdi, %rsi
        movq    (%rsi), %r8
        movq    %r13, %rsi
        salq    $3, %rsi
        addq    %rax, %rsi
        movq    (%rsi), %rsi
        cmpq    %rsi, %r8
        jge     .L3
        movq    %r12, %rsi
        leaq    1(%rsi), %r12
        salq    $3, %rsi
        leaq    (%rdi,%rsi), %r9
        movq    %rbx, %rsi
        leaq    1(%rsi), %rbx
        salq    $3, %rsi
        leaq    (%r14,%rsi), %r8
        movq    (%r9), %rsi
        movq    %rsi, (%r8)
        jmp     .L2
.L3:
        movq    %r13, %rsi
        leaq    1(%rsi), %r13
        salq    $3, %rsi
        leaq    (%rax,%rsi), %r9
        movq    %rbx, %rsi
        leaq    1(%rsi), %rbx
        salq    $3, %rsi
        leaq    (%r14,%rsi), %r8
        movq    (%r9), %rsi
        movq    %rsi, (%r8)
.L2:
        cmpq    %rdx, %r12
        jge     .L4
        cmpq    %rcx, %r13
        jl      .L5
.L4:
        cmpq    %rdx, %r12
        jne     .L10
        jmp     .L7
.L8:
        movq    %r13, %rsi
        leaq    1(%rsi), %r13
        salq    $3, %rsi
        leaq    (%rax,%rsi), %r9
        movq    %rbx, %rsi
        leaq    1(%rsi), %rbx
        salq    $3, %rsi
        leaq    (%r14,%rsi), %r8
        movq    (%r9), %rsi
        movq    %rsi, (%r8)
.L7:
        cmpq    %rcx, %r13
        jl      .L8
        jmp     .L9
.L11:
        movq    %r12, %rsi
        leaq    1(%rsi), %r12
        salq    $3, %rsi
        leaq    (%rdi,%rsi), %r9
        movq    %rbx, %rsi
        leaq    1(%rsi), %rbx
        salq    $3, %rsi
        leaq    (%r14,%rsi), %r8
        movq    (%r9), %rsi
        movq    %rsi, (%r8)
.L10:
        cmpq    %rdx, %r12
        jl      .L11
.L9:
        movl    $0, %ebx
        movl    $0, %r12d
        jmp     .L12
.L13:
        movq    %rbx, %rsi
        leaq    1(%rsi), %rbx
        salq    $3, %rsi
        addq    %r14, %rsi
        movq    %r12, %r8
        salq    $3, %r8
        addq    %rdi, %r8
        movq    (%rsi), %rsi
        movq    %rsi, (%r8)
        addq    $1, %r12
.L12:
        cmpq    %rdx, %r12
        jl      .L13
        movl    $0, %r13d
        jmp     .L14
.L15:
        movq    %rbx, %rdx
        leaq    1(%rdx), %rbx
        salq    $3, %rdx
        addq    %r14, %rdx
        movq    %r13, %rsi
        salq    $3, %rsi
        addq    %rax, %rsi
        movq    (%rdx), %rdx
        movq    %rdx, (%rsi)
        addq    $1, %r13
.L14:
        cmpq    %rcx, %r13
        jl      .L15
        nop
        popq    %rbx
        popq    %r12
        popq    %r13
        popq    %r14
        popq    %rbp
        .cfi_def_cfa 7, 8
        ret
        .cfi_endproc
.LFE6:
        .size   merge, .-merge

        .globl  merge_sort
        .type   merge_sort, @function
merge_sort:
.LFB7:
        .cfi_startproc
        pushq   %rbp
        .cfi_def_cfa_offset 16
        .cfi_offset 6, -16
        movq    %rsp, %rbp
        .cfi_def_cfa_register 6
        pushq   %r14
        pushq   %r13
        pushq   %r12
        pushq   %rbx
        .cfi_offset 14, -24
        .cfi_offset 13, -32
        .cfi_offset 12, -40
        .cfi_offset 3, -48
        movq    %rdi, %r13
        movq    %rsi, %r12
        cmpq    $1, %r12
        jle     .L19
        movq    %r12, %rax
        shrq    $63, %rax
        addq    %r12, %rax
        sarq    %rax
        movq    %rax, %rbx
        movq    %rbx, %rax
        salq    $3, %rax
        leaq    0(%r13,%rax), %r14
        movq    %rbx, %rsi
        movq    %r13, %rdi
        call    merge_sort
        movq    %r12, %rdx
        subq    %rbx, %rdx
        movq    %rbx, %rax
        salq    $3, %rax
        addq    %r13, %rax
        movq    %rdx, %rsi
        movq    %rax, %rdi
        call    merge_sort
        movq    %r12, %rax
        subq    %rbx, %rax
        movq    %rax, %rcx
        movq    %rbx, %rdx
        movq    %r14, %rsi
        movq    %r13, %rdi
        call    merge
        jmp     .L16
.L19:
        nop
.L16:
        popq    %rbx
        popq    %r12
        popq    %r13
        popq    %r14
        popq    %rbp
        .cfi_def_cfa 7, 8
        ret
        .cfi_endproc
.LFE7:
        .size   merge_sort, .-merge_sort
