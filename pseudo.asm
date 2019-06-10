			.extern _f_ro_open
			.extern _merge_sort
			.extern _print_new_line
			.extern _print_string
			.extern _exit
			.extern _pseudo_stdlib_init
			.extern _f_wo_open
			.extern _read_value
			.extern _prime
			.extern _print_number
			.extern _f_close
.section .bss
			.lcomm INTERNAL____CACHE, 524288
			.lcomm var_0, 8
			.lcomm var_1, 8
			.lcomm var_2, 2
			.lcomm var_3, 8
			.lcomm var_4, 8
			.lcomm var_5, 8
			.lcomm var_6, 800
			.lcomm var_7, 2
			
			
			#DO NOT EDIT
			#THIS FILE IS COMPUTER GENERATED
			#AS A RESULT OF THE COMPILATION OF "pseudo.psl"
.section .rodata
file_1_path:
			.asciz "pseudo.in"
str_1:
			.asciz "found "
str_2:
			.asciz " primes"
file_2_path:
			.asciz "pseudo.maxdiff.out"
str_3:
			.asciz " "
.section .text
			.globl		main
main:
			call		_pseudo_stdlib_init@PLT
			movq		$file_1_path,			%rax
			call		_f_ro_open@PLT
			movw		%ax,					var_7
			movzxw		var_7,					%r8
			call		_read_value@PLT
			movq		%rax,					var_0
			movq		$1,						var_5
			movq		$1,						var_3
WHILE_1:
			movq		var_3,					%r10
			cmp			var_0,					%r10
			movq		$0,						%r10
			jg			LOGIC_1
			movq		$1,						%r10
LOGIC_1:
			movq		%r10,					INTERNAL____CACHE+0
			cmpq		$0,						%r10
			je			WHILE_1_END
			movzxw		var_7,					%r8
			call		_read_value@PLT
			movq		%rax,					var_4
			call		_prime@PLT
			movq		%rax,					var_1
			movq		var_1,					%r10
			cmpq		$1,						%r10
			movq		$0,						%r10
			jne			LOGIC_2
			movq		$1,						%r10
LOGIC_2:
			cmpq		$0,						%r10
			je			COND_1_FINAL_END
COND_1_TRUE:
			movq		var_4,					%rax
			movq		var_5,					%rdi
			movq		%rax,					var_6(,%rdi,8)
			movq		var_5,					%r10
			movq		$1,						%r11
			add			$1,						%r10
			movq		%r10,					var_5
COND_1_FINAL_END:
			movq		$1,						%r10
			addq		$1,						var_3
			jmp			WHILE_1
WHILE_1_END:
			mov			$1,						%r8
			movq		$str_1,					%rax
			movq		$6,						%rbx
			call		_print_string@PLT
			movq		var_5,					%r10
			sub			$1,						%r10
			mov			%r10,					%rax
			call		_print_number@PLT
			movq		$str_2,					%rax
			movq		$7,						%rbx
			call		_print_string@PLT
			call		_print_new_line@PLT
			movq		$var_6,					%r10
			add			$8,						%r10
			pushq		%r10
			movq		var_5,					%r10
			movq		$1,						%r11
			sub			$1,						%r10
			movq		%r10,					%rsi
			popq		%rdi
			call		_merge_sort@PLT
			movzxw		var_7,					%rax
			call		_f_close@PLT
			movq		$file_2_path,			%rax
			movq		$0744,					%rbx
			call		_f_wo_open@PLT
			movw		%ax,					var_2
			movq		$1,						var_3
WHILE_2:
			movq		var_3,					%r10
			movq		%r10,					INTERNAL____CACHE+0
			movq		var_5,					%r10
			sub			$1,						%r10
			movq		%r10,					INTERNAL____CACHE+8
			movq		INTERNAL____CACHE+0,	%r10
			cmp			INTERNAL____CACHE+8,	%r10
			movq		$0,						%r10
			jg			LOGIC_3
			movq		$1,						%r10
LOGIC_3:
			cmpq		$0,						%r10
			je			WHILE_2_END
			movzxw		var_2,					%r8
			movq		var_3,					%rdi
			mov			var_6(,%rdi,8),			%rax
			call		_print_number@PLT
			movq		$str_3,					%rax
			movq		$1,						%rbx
			call		_print_string@PLT
			movq		$1,						%r10
			addq		$1,						var_3
			jmp			WHILE_2
WHILE_2_END:
			movzxw		var_2,					%rax
			call		_f_close@PLT
			mov			$0,						%rax
			call		_exit@PLT
