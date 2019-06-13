			.extern _f_ro_open
			.extern _merge_sort
			.extern _pseudo_exit
			.extern _prepare_for_sort
			.extern _print_string
			.extern _pseudo_stdlib_init
			.extern _f_wo_open
			.extern _read_value
			.extern _prime
			.extern _f_close
			.extern _print_number
.section .bss
			.lcomm INTERNAL____CACHE, 524288
			.lcomm var_0, 8
			.lcomm var_1, 8
			.lcomm var_2, 8
			.lcomm var_3, 8
			.lcomm var_4, 8
			.lcomm var_5, 8
			.lcomm var_6, 524288
			.lcomm var_7, 2
			
			
			#DO NOT EDIT
			#THIS FILE IS COMPUTER GENERATED
			#AS A RESULT OF THE COMPILATION OF "pseudo.psl"
.section .rodata
file_1_path:
			.asciz "pseudo.in"
file_2_path:
			.asciz "pseudo.out"
str_1:
			.asciz " "
.section .text
			.globl		main
main:
			call		_pseudo_stdlib_init@PLT
			movq		$0,							var_5
			movq		$file_1_path,				%rax
			call		_f_ro_open@PLT
			movw		%ax,						var_7
			movzxw		var_7,						%r8
			call		_read_value@PLT
			movq		%rax,						var_0
			movq		$1,							var_4
			movq		$1,							var_2
WHILE_1:
			movq		var_2,						%r10
			cmp			var_0,						%r10
			movq		$0,							%r10
			jg			LOGIC_1
			movq		$1,							%r10
LOGIC_1:
			cmpq		$0,							%r10
			je			WHILE_1_END
			movzxw		var_7,						%r8
			call		_read_value@PLT
			movq		%rax,						var_3
			call		_prime@PLT
			movq		%rax,						var_1
			movq		var_1,						%r10
			cmpq		$1,							%r10
			movq		$0,							%r10
			jne			LOGIC_2
			movq		$1,							%r10
LOGIC_2:
			cmpq		$0,							%r10
			je			COND_1_FINAL_END
COND_1_TRUE:
			movq		var_3,						%rax
			movq		var_4,						%rdi
			movq		%rax,						var_6(,%rdi,8)
			movq		var_4,						%r10
			add			$1,							%r10
			movq		%r10,						var_4
COND_1_FINAL_END:
			movq		$1,							%r10
			addq		$1,							var_2
			jmp			WHILE_1
WHILE_1_END:
			movzxw		var_7,						%rax
			call		_f_close@PLT
			movq		var_4,						%r10
			sub			$1,							%r10
			movq		%r10,						var_4
			movq		$_ll_i_cmp_less,			%rdi
			call		_prepare_for_sort@PLT
			movq		$var_6,						%r10
			movq		$8,							%r11
			add			%r11,						%r10
			pushq		%r10
			movq		var_4,						%rsi
			popq		%rdi
			call		_merge_sort@PLT
			movq		$file_2_path,				%rax
			movq		$0744,						%rbx
			call		_f_wo_open@PLT
			movw		%ax,						var_7
			movq		$1,							var_2
WHILE_2:
			movq		var_2,						%r10
			cmp			var_4,						%r10
			movq		$0,							%r10
			jg			LOGIC_3
			movq		$1,							%r10
LOGIC_3:
			cmpq		$0,							%r10
			je			WHILE_2_END
			movzxw		var_7,						%r8
			movq		var_2,						%rdi
			mov			var_6(,%rdi,8),				%rax
			call		_print_number@PLT
			movq		$str_1,						%rax
			movq		$1,							%rbx
			call		_print_string@PLT
			movq		$1,							%r10
			addq		$1,							var_2
			jmp			WHILE_2
WHILE_2_END:
			movzxw		var_7,						%rax
			call		_f_close@PLT
			movq		$0,							%rax
			call		_pseudo_exit@PLT
