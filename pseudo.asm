			.extern _init
			.extern _print_new_line
			.extern _exit
			.extern _f_wo_open
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
			.lcomm var_6, 8
			.lcomm var_7, 8
			.lcomm var_8, 8
			.lcomm var_9, 8
			
			
			#DO NOT EDIT
			#THIS FILE IS COMPUTER GENERATED
			#AS A RESULT OF THE COMPILATION OF "pseudo.psl"
.section .rodata
file_1_path:
			.asciz "pseudo.maxdiff.out"
.section .text
			.globl		_start
_start:
			call		_init
			movq		$0,						var_5
			movq		$1,						var_8
			movq		$0,						var_9
			movq		$2,						var_3
			movq		$3,						%r10
			movq		$3,						var_0
WHILE_1:
			movq		var_0,					%r10
			cmpq		$1000000,				%r10
			movq		$0,						%r10
			jle			LOGIC_1
			movq		$1,						%r10
LOGIC_1:
			movq		%r10,					INTERNAL____CACHE+0
			test		%r10,					%r10
			mov			$1,						%r10
			je			LOGIC_NOT_1
			mov			$0,						%r10
LOGIC_NOT_1:
			cmpq		$0,						%r10
			je			WHILE_1_END
			movq		var_0,					%r10
			movq		%r10,					%rax
			call		_prime
			movq		%rax,					var_1
			movq		var_1,					%r10
			cmpq		$0,						%r10
			je			COND_1_FINAL_END
COND_1_TRUE:
			movq		var_0,					%r10
			movq		var_3,					%r11
			sub			%r11,					%r10
			movq		%r10,					var_4
			movq		var_5,					%r11
			cmpq		%r11,					%r10
			movq		$0,						%r10
			jle			LOGIC_2
			movq		$1,						%r10
LOGIC_2:
			cmpq		$0,						%r10
			je			COND_2_FINAL_END
COND_2_TRUE:
			movq		var_4,					%r10
			movq		%r10,					var_5
			movq		var_0,					%r10
			movq		%r10,					var_6
			movq		var_3,					%r10
			movq		%r10,					var_7
COND_2_FINAL_END:
			movq		var_0,					%r10
			movq		%r10,					var_3
COND_1_FINAL_END:
			movq		var_0,					%r10
			movq		$2,						%r11
			add			$2,						%r10
			movq		%r10,					var_0
			jmp			WHILE_1
WHILE_1_END:
			mov			$1,						%r8
			movq		var_5,					%r10
			mov			%r10,					%rax
			call		_print_number
			call		_print_new_line
			movq		$file_1_path,			%rax
			movq		$0744,					%rbx
			call		_f_wo_open
			movw		%ax,					var_2
			movzxw		var_2,					%r8
			movq		var_5,					%r10
			mov			%r10,					%rax
			call		_print_number
			call		_print_new_line
			movq		var_7,					%r10
			mov			%r10,					%rax
			call		_print_number
			call		_print_new_line
			movq		var_6,					%r10
			mov			%r10,					%rax
			call		_print_number
			movzxw		var_2,					%rax
			call		_f_close
			mov			$0,						%rax
			call		_exit
