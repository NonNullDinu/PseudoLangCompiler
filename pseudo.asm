			.extern _print_new_line
			.extern _pseudo_exit
			.extern _print_string
			.extern _pseudo_stdlib_init
			.extern _read_value
			.extern _print_number
.section .bss
			.lcomm INTERNAL____CACHE, 524288
			.lcomm var_0, 8
			.lcomm var_1, 8
			.lcomm var_2, 8
			.lcomm var_3, 8
			.lcomm var_4, 8
			.lcomm var_5, 8
			.lcomm var_6, 8
			
			
			#DO NOT EDIT
			#THIS FILE IS COMPUTER GENERATED
			#AS A RESULT OF THE COMPILATION OF "pseudo.psl"
.section .rodata
str_1:
			.asciz "{"
str_2:
			.asciz ","
str_3:
			.asciz "}"
str_4:
			.asciz "doesn't exist"
.section .text
			.globl		main
main:
			call		_pseudo_stdlib_init@PLT
			movq		$0,							var_6
			movq		$0,							%r8
			call		_read_value@PLT
			movq		%rax,						var_0
			call		_read_value@PLT
			movq		%rax,						var_1
			call		_read_value@PLT
			movq		%rax,						var_2
			movq		var_0,						%r10
			movq		%r10,						var_3
WHILE_1:
			movq		var_3,						%r10
			cmp			var_1,						%r10
			movq		$0,							%r10
			jg			LOGIC_1
			movq		$1,							%r10
LOGIC_1:
			movq		%r10,						INTERNAL____CACHE+0
			cmpq		$0,							%r10
			je			WHILE_1_END
			movq		var_3,						%r10
			movq		%r10,						INTERNAL____CACHE+8
			movq		%r10,						var_4
WHILE_2:
			movq		var_4,						%r10
			movq		%r10,						INTERNAL____CACHE+16
			movq		var_2,						%r10
			movq		var_3,						%r11
			sub			%r11,						%r10
			movq		%r10,						INTERNAL____CACHE+24
			movq		INTERNAL____CACHE+16,		%r10
			cmp			INTERNAL____CACHE+24,		%r10
			movq		$0,							%r10
			jg			LOGIC_2
			movq		$1,							%r10
LOGIC_2:
			cmpq		$0,							%r10
			je			WHILE_2_END
			movq		var_2,						%r10
			movq		var_3,						%r11
			sub			%r11,						%r10
			movq		%r10,						INTERNAL____CACHE+0
			movq		var_4,						%r10
			movq		%r10,						INTERNAL____CACHE+8
			movq		INTERNAL____CACHE+0,		%r10
			movq		INTERNAL____CACHE+8,		%r11
			sub			%r11,						%r10
			movq		%r10,						var_5
			movq		var_4,						%r10
			cmp			var_5,						%r10
			movq		$0,							%r10
			jg			LOGIC_3
			movq		$1,							%r10
LOGIC_3:
			movq		%r10,						INTERNAL____CACHE+0
			cmpq		$0,							INTERNAL____CACHE+0
			je			COND_1_FINAL_END
			movq		var_5,						%r10
			cmp			var_1,						%r10
			movq		$0,							%r10
			jg			LOGIC_4
			movq		$1,							%r10
LOGIC_4:
			movq		%r10,						INTERNAL____CACHE+8
			movq		INTERNAL____CACHE+0,		%r10
			movq		INTERNAL____CACHE+8,		%r11
			and			%r10,						%r11
			and			$1,							%r10
			cmpq		$0,							%r10
			je			COND_1_FINAL_END
COND_1_TRUE:
			movq		var_6,						%r10
			add			$1,							%r10
			movq		%r10,						var_6
			mov			$1,							%r8
			movq		$str_1,						%rax
			movq		$1,							%rbx
			call		_print_string@PLT
			mov			var_3,						%rax
			call		_print_number@PLT
			movq		$str_2,						%rax
			movq		$1,							%rbx
			call		_print_string@PLT
			mov			var_4,						%rax
			call		_print_number@PLT
			movq		$str_2,						%rax
			movq		$1,							%rbx
			call		_print_string@PLT
			mov			var_5,						%rax
			call		_print_number@PLT
			movq		$str_3,						%rax
			movq		$1,							%rbx
			call		_print_string@PLT
			call		_print_new_line@PLT
COND_1_FINAL_END:
			movq		$1,							%r10
			addq		$1,							var_4
			jmp			WHILE_2
WHILE_2_END:
			movq		$1,							%r10
			addq		$1,							var_3
			jmp			WHILE_1
WHILE_1_END:
			movq		var_6,						%r10
			cmpq		$0,							%r10
			movq		$0,							%r10
			jne			LOGIC_5
			movq		$1,							%r10
LOGIC_5:
			cmpq		$0,							%r10
			je			COND_2_FINAL_END
COND_2_TRUE:
			mov			$1,							%r8
			movq		$str_4,						%rax
			movq		$13,						%rbx
			call		_print_string@PLT
			call		_print_new_line@PLT
COND_2_FINAL_END:
			movq		$0,							%rax
			call		_pseudo_exit@PLT
