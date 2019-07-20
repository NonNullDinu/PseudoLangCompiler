			.extern _f_ro_open
			.extern _print_string
			.extern _pseudo_stdlib_init
			.extern _read_value
			.extern _print_number
.section .bss
			.lcomm INTERNAL____CACHE, 524288
			.lcomm var_0, 8
			.lcomm var_1, 8
			.lcomm var_2, 2
			.lcomm var_3, 8
			.lcomm arr_mem_1, 6400
			
			
			#DO NOT EDIT
			#THIS FILE IS COMPUTER GENERATED
			#AS A RESULT OF THE COMPILATION OF "pseudo.psl"
.section .rodata
file_1_path:
			.asciz "pseudo.in"
str_1:
			.asciz " "
.section .text
			.globl		main
main:
			call		_pseudo_stdlib_init@PLT
			movq		$arr_mem_1,					var_3
			movq		$file_1_path,				%rax
			call		_f_ro_open@PLT
			movw		%ax,						var_2
			movzxw		var_2,						%r8
			call		_read_value@PLT
			movq		%rax,						var_0
			movq		$1,							var_1
WHILE_1:
			movq		var_1,						%r10
			cmp			var_0,						%r10
			movq		$0,							%r10
			jg			LOGIC_1
			movq		$1,							%r10
LOGIC_1:
			cmpq		$0,							%r10
			je			WHILE_1_END
			movzxw		var_2,						%r8
			call		_read_value@PLT
			pushq		%rax
			movq		var_1,						%rdi
			movq		var_3,						%r10
			popq		%rax
			movq		%rax,						(%r10,%rdi,8)
			movq		$1,							%r10
			addq		$1,							var_1
			jmp			WHILE_1
WHILE_1_END:
			movq		var_0,						%r10
			movq		%r10,						var_1
WHILE_2:
			movq		var_1,						%r10
			cmpq		$0,							%r10
			movq		$0,							%r10
			jle			LOGIC_2
			movq		$1,							%r10
LOGIC_2:
			cmpq		$0,							%r10
			je			WHILE_2_END
			mov			$1,							%r8
			movq		var_1,						%rdi
			movq		var_3,						%r10
			mov			(%r10,%rdi,8),				%rax
			call		_print_number@PLT
			movq		$str_1,						%rax
			movq		$1,							%rbx
			call		_print_string@PLT
			movq		var_1,						%r10
			sub			$1,							%r10
			movq		%r10,						var_1
			jmp			WHILE_2
WHILE_2_END:
			movq		$0,							%rax
			jmp			_pseudo_exit@PLT
