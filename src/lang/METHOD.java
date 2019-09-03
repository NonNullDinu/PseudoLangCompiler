/*
 * Copyright (C) 2018-2019  Dinu Blanovschi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package lang;

import main._LANG_COMPILER;
import tokens.*;
import variables.DATA_TYPE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static main._LANG_COMPILER.AssemblyMake.value;
import static main._LANG_COMPILER.preparations;

public enum METHOD {
	@SuppressWarnings("unused")
	EXIT((m, argTokens) -> {
		if (argTokens != null && argTokens.length > 0) {
			return _LANG_COMPILER.AssemblyMake.valueInstructions(argTokens[0]) + "\tmov %r10, %rax\n\tjmp _pseudo_exit@PLT\n";
		} else
			return "\tmovq $0, %rax\n\tjmp _pseudo_exit@PLT\n";
	}),

	@SuppressWarnings("unused")
	DEFINED_METHOD((m, argTokens) -> {
		StringBuilder methodBody = new StringBuilder();
		if (argTokens != null && argTokens.length != 0)
			for (int i = argTokens.length - 1; i >= 0; i--) {
				_LANG_COMPILER.rec_ind = 0;
				methodBody.append(_LANG_COMPILER.AssemblyMake.valueInstructions(argTokens[i])).append("\tpushq %r10\n");
			}
		methodBody.append("\tcall _").append(m.name).append("\n");
		if (argTokens != null && argTokens.length != 0)
			methodBody.append("\tpopq %r10\n".repeat(argTokens.length));
		return methodBody.toString();
	}),

	@SuppressWarnings("unused")
	PRINT_DIGIT((m, argTokens) -> {
		_LANG_COMPILER.rec_ind = 0;
		return _LANG_COMPILER.AssemblyMake.valueInstructions(argTokens[0]) + "\tleaq digits(,%r10,1), %rax\n\tcall _print_char@PLT\n";
	}),

	@SuppressWarnings("unused")
	ASM((m, argTokens) -> {
		StringBuilder asm = new StringBuilder();
		for (Token[] t : argTokens)
			asm.append('\t').append(((StringToken) t[0]).str, 1, ((StringToken) t[0]).str.length() - 1).append('\n');
		return asm.toString();
	}),

	@SuppressWarnings("unused")
	READ((m, argTokens) -> {
		StringBuilder asm = new StringBuilder();
		if (argTokens[argTokens.length - 1].length > 2 && argTokens[argTokens.length - 1][argTokens[argTokens.length - 1].length - 2] instanceof FROM_TO_SPECIFIER_TOKEN) {
			String file_varname = ((FILE_FULL_TOKEN) argTokens[argTokens.length - 1][argTokens[argTokens.length - 1].length - 1]).var_name;
			if (file_varname.equals("internal____console"))
				asm.append("\tmovq $0, %r8\n");
			else
				asm.append("\tmovzxw var_").append(_LANG_COMPILER.var_indices.get(file_varname)).append(", %r8\n");
			int lenm1 = argTokens.length - 1;
			Token[] cpy = new Token[argTokens[lenm1].length - 1];
			System.arraycopy(argTokens[lenm1], 0, cpy, 0, cpy.length);
			argTokens[lenm1] = cpy;
		} else asm.append("\tmov $0, %r8\n");

		for (Token[] tk : argTokens) {
			_LANG_COMPILER.rec_ind = 0;
			asm.append("\tcall _read_value@PLT\n\tpushq %rax\n");
			preparations = "";
			String v = value(tk[0]);
			asm.append(preparations).append("\tpopq %rax\n\tmovq %rax, ").append(v).append("//POINTER\n");
		}
		return asm.toString();
	}),

	@SuppressWarnings("unused")
	WRITE((m, argTokens) -> {
		StringBuilder asm = new StringBuilder();
		if (argTokens[argTokens.length - 1].length >= 2 && argTokens[argTokens.length - 1][argTokens[argTokens.length - 1].length - 2] instanceof FROM_TO_SPECIFIER_TOKEN) {
			String file_varname = ((FILE_FULL_TOKEN) argTokens[argTokens.length - 1][argTokens[argTokens.length - 1].length - 1]).var_name;
			if (file_varname.equals("internal____console"))
				asm.append("\tmovq $1, %r8\n");
			else
				asm.append("\tmovzxw var_").append(_LANG_COMPILER.var_indices.get(file_varname)).append(", %r8\n");
			int lenm1 = argTokens.length - 1;
			Token[] cpy = new Token[argTokens[lenm1].length - 2];
			System.arraycopy(argTokens[lenm1], 0, cpy, 0, cpy.length);
			argTokens[lenm1] = cpy;
		} else asm.append("\tmov $1, %r8\n");
		System.out.println(Arrays.deepToString(argTokens));
		for (Token[] a : argTokens) {
			if (a[0] instanceof StringToken) {
				if (!((StringToken) a[0]).str.equals("\"\\n\"")) {
					_LANG_COMPILER.VarManager.VAR_ str = _LANG_COMPILER.lookupStringVar(".asciz " + ((StringToken) a[0]).str);
					if (str == null) {
						_LANG_COMPILER.addNewVar("str_" + ++_LANG_COMPILER.strCode, ".asciz " + ((StringToken) a[0]).str);
						str = _LANG_COMPILER.lookupStringVar(".asciz " + ((StringToken) a[0]).str);
						if (str == null)
							throw new RuntimeException("Could not create variable");
					}
					asm.append("\tmovq $").append(str.name).append(", %rax\n\tmovq $").append(((StringToken) a[0]).str.length() - 2).append(", %rbx\n\tcall _print_string@PLT\n");
				} else
					asm.append("\tcall _print_new_line@PLT\n");
			} else {
				_LANG_COMPILER.rec_ind = 0;
				preparations = "";
				String v = _LANG_COMPILER.AssemblyMake.valueInstructions(a);
				asm.append(preparations).append(v).append("\tmov %r10, %rax\n\tcall _print_number@PLT\n");
			}
		}
		return asm.toString();
	}),

	@SuppressWarnings("unused")
	PRINT_BINARY((m, argTokens) -> {
		StringBuilder asm = new StringBuilder();
		if (argTokens[argTokens.length - 1].length >= 2 && argTokens[argTokens.length - 1][argTokens[argTokens.length - 1].length - 2] instanceof FROM_TO_SPECIFIER_TOKEN) {
			String file_varname = ((FILE_FULL_TOKEN) argTokens[argTokens.length - 1][argTokens[argTokens.length - 1].length - 1]).var_name;
			if (file_varname.equals("internal____console"))
				asm.append("\tmovq $1, %r8\n");
			else
				asm.append("\tmovzxw var_").append(_LANG_COMPILER.var_indices.get(file_varname)).append(", %r8\n");
			int lenm1 = argTokens.length - 1;
			Token[] cpy = new Token[lenm1 - 1];
			System.arraycopy(argTokens[lenm1], 0, cpy, 0, cpy.length);
			argTokens[lenm1] = cpy;
		} else asm.append("\tmov $1, %r8\n");

		for (Token[] a : argTokens) {
			_LANG_COMPILER.rec_ind = 0;
			preparations = "";
			String v = _LANG_COMPILER.AssemblyMake.valueInstructions(a);
			asm.append(preparations).append(v).append("\tmov %r10, %rax\n\tcall _print_bin_number@PLT\n");
		}
		return asm.toString();
	}),

	@SuppressWarnings("unused")
	OPEN(((m, argTokens) -> {
		String asm = "";
		int lastLength = argTokens[argTokens.length - 1].length;
		if (argTokens[argTokens.length - 1][lastLength - 2] instanceof AS_TOKEN && argTokens[argTokens.length - 1][lastLength - 1] instanceof FILE_FULL_TOKEN) {
			FILE_FULL_TOKEN fileToken = ((FILE_FULL_TOKEN) argTokens[argTokens.length - 1][lastLength - 1]);
			String name = fileToken.var_name;
			int size;
			int begin = 0, end = 0;
			for (int i = 0; i < argTokens[0].length; i++) {
				if (argTokens[0][i] instanceof ParenthesisOpenedToken) {
					begin = i;
				}
				if (argTokens[0][i] instanceof ParenthesisClosedToken) {
					end = i - 1;
				}
			}
			size = end - begin + 1;
			Token[] tkns = new Token[size];
			System.arraycopy(argTokens[0], begin, tkns, 0, size);
			Token[][] split = _LANG_COMPILER.Parser.split_by_commas(tkns);
			if (split == null)
				throw new NullPointerException("OPEN with no parameters provided");
			String file = ((StringToken) split[0][0]).str;
			FILE_ACCESS_TOKEN access = ((FILE_ACCESS_TOKEN) split[1][0]);
			_LANG_COMPILER.addNewVar("file_" + ++_LANG_COMPILER.fileCode + "_path", ".asciz " + file);
			if (access.access.equals(FILE_ACCESS.WRITE_ONLY)) {
				if (split.length == 3) {
					int perm = ((NumberToken) split[2][0]).v;
					asm = "\tmovq $file_" + _LANG_COMPILER.fileCode + "_path, %rax\n" +
							"\tmovq $0" + perm + ", %rbx\n" +
							"\tcall " + access.access.func_open() + "\n" +
							"\tmovw %ax, var_" + _LANG_COMPILER.var_indices.get(name) + "//NO_DELETE\n";
				} else {
					asm = "\tmovq $file_" + _LANG_COMPILER.fileCode + "_path, %rax\n" +
							"\tmovq $0744, %rbx\n" + // Default is 0744
							"\tcall " + access.access.func_open() + "\n" +
							"\tmovw %ax, var_" + _LANG_COMPILER.var_indices.get(name) + "//NO_DELETE\n";
				}
			} else {
				asm = "\tmovq $file_" + _LANG_COMPILER.fileCode + "_path, %rax\n" +
						"\tcall " + access.access.func_open() + "\n" +
						"\tmovw %ax, var_" + _LANG_COMPILER.var_indices.get(name) + "//NO_DELETE\n";
			}
		}
		return asm;
	})),

	@SuppressWarnings("unused")
	CLOSE(((m, argTokens) -> {
		if (argTokens[0][0] instanceof FILE_FULL_TOKEN) {
			return "\tmovzxw var_" + _LANG_COMPILER.var_indices.get(((FILE_FULL_TOKEN) argTokens[0][0]).var_name) + ", %rax\n" +
					"\tcall _f_close@PLT\n";
		}
		return "";
	})),

	@SuppressWarnings("unused")
	STATIC_ALLOCATE((m, argTokens) -> {
		int size = Objects.requireNonNull(_LANG_COMPILER.VarManager.getVarType(((IdentifierToken) argTokens[0][0]).identifier)).bytesize * Objects.requireNonNull(_LANG_COMPILER.AssemblyMake.evaluate(argTokens[1])).vi;
		_LANG_COMPILER.addNewBssVar("arr_mem_" + (++_LANG_COMPILER.arr_mem_ind), _LANG_COMPILER.VarManager.getVarArrElemType(((IdentifierToken) argTokens[0][0]).identifier));
		_LANG_COMPILER.VarManager.setVarSize("arr_mem_" + _LANG_COMPILER.arr_mem_ind, size);
		return "\tmovq $arr_mem_" + _LANG_COMPILER.arr_mem_ind + ", var_" + _LANG_COMPILER.var_indices.get(((IdentifierToken) argTokens[0][0]).identifier) + "\n";
	}),

	@SuppressWarnings("unused")
	DYNAMIC_ALLOCATE((m, argTokens) -> _LANG_COMPILER.AssemblyMake.valueInstructions(argTokens[1]) + "\tmovq %r10, %rsi\n\tmovq $" + Objects.requireNonNull(_LANG_COMPILER.VarManager.getVarType(((IdentifierToken) argTokens[0][0]).identifier)).bytesize + ", %rdi\n\tcall _pseudo_lib_malloc@PLT\n\tmovq %rax, var_" + _LANG_COMPILER.var_indices.get(((IdentifierToken) argTokens[0][0]).identifier) + "\n"),

	@SuppressWarnings("unused")
	DYNAMIC_DEALLOCATE((m, argTokens) -> "\tmovq var_" + _LANG_COMPILER.var_indices.get(((IdentifierToken) argTokens[0][0]).identifier) + ", %rdi\n\tcall _pseudo_lib_free@PLT\n"),

	@SuppressWarnings("unused")
	ALLOCATE((m, argTokens) -> STATIC_ALLOCATE.callback.assembly(m, argTokens)),

	@SuppressWarnings("unused")
	SORT(((m, argTokens) -> {
		String prep = "\tmovq $_ll_i_cmp, %rdi\n\tcall _prepare_for_sort@PLT\n";
		//BEGIN
		_LANG_COMPILER.rec_ind = 0;
		String v1 = _LANG_COMPILER.AssemblyMake.valueInstructions(argTokens[0]) + "\tpushq %r10\n";
		//END
		_LANG_COMPILER.rec_ind = 0;
		String v2 = _LANG_COMPILER.AssemblyMake.valueInstructions(argTokens[1]) + "\tmovq %r10, %rsi\n";
		return prep + v1 + v2 + "\tpopq %rdi\n\tcall _merge_sort@PLT\n";
	})),

	@SuppressWarnings("unused")
	REVERSE(((m, argTokens) -> {
		//BEGIN
		_LANG_COMPILER.rec_ind = 0;
		String v1 = _LANG_COMPILER.AssemblyMake.valueInstructions(argTokens[0]) + "\tpushq %r10\n";
		//END
		_LANG_COMPILER.rec_ind = 0;
		String v2 = _LANG_COMPILER.AssemblyMake.valueInstructions(argTokens[1]) + "\tmovq %r10, %rbx\n";
		return v1 + v2 + "\tpopq %rax\n\tcall _reverse@PLT\n";
	})),

	@SuppressWarnings("unused")
	REVERSE_SORT((m, argTokens) -> {
		String prep = "\tmovq $_ll_i_cmp_less, %rdi\n\tcall _prepare_for_sort@PLT\n";
		_LANG_COMPILER.rec_ind = 0;
		String v1 = _LANG_COMPILER.AssemblyMake.valueInstructions(argTokens[0]) + "\tpushq %r10\n";
		//END
		_LANG_COMPILER.rec_ind = 0;
		String v2 = _LANG_COMPILER.AssemblyMake.valueInstructions(argTokens[1]) + "\tmovq %r10, %rsi\n";
		return prep + v1 + v2 + "\tpopq %rdi\n\tcall _merge_sort@PLT\n";
	}),

	@SuppressWarnings("unused")
	SWAP((m, argTokens) -> "\tmovq $var_" + _LANG_COMPILER.var_indices.get(((IdentifierToken) argTokens[0][0]).identifier) + ", %rcx\n\tmovq $var_" + _LANG_COMPILER.var_indices.get(((IdentifierToken) argTokens[1][0]).identifier) + ", %rdx\n\tcall _swap@PLT\n"),

	@SuppressWarnings("unused")
	PRIME((m, argTokens) -> {
		_LANG_COMPILER.rec_ind = 0;
		return _LANG_COMPILER.AssemblyMake.valueInstructions(argTokens[0]) + "\tmovq %r10, %rax\n\tcall _prime@PLT\n\tmovq %rax, " + value(argTokens[1][0]) + "\n";
	}),

	@SuppressWarnings("unused")
	DIV_SUM((m, argTokens) -> {
		//1 - value
		//2 - idf of result target
		preparations = "";
		String v = value(argTokens[1][0]);
		_LANG_COMPILER.rec_ind = 0;
		return _LANG_COMPILER.AssemblyMake.valueInstructions(argTokens[0]) + "\tmovq %r10, %rbx\n\tcall _div_sum@PLT\n\tpushq %rcx\n" + preparations + "\tpopq %rcx\n\tmovq %rcx, " + v + "\n";
	}),

	@SuppressWarnings("unused")
	PERFECT((m, argTokens) -> {
		preparations = "";
		String v = value(argTokens[1][0]);
		_LANG_COMPILER.rec_ind = 0;
		return _LANG_COMPILER.AssemblyMake.valueInstructions(argTokens[0]) + "\tmovq %r10, %rax\n\tcall _perfect@PLT\n\tpushq %rax\n" + preparations + "\tpopq %rax\n\tmovq %rax, " + v + "\n";
	}),

	@SuppressWarnings("unused")
	DECLARE((m, argTokens) -> {
		TypeToken tt;
		if (argTokens[argTokens.length - 1][(argTokens[argTokens.length - 1].length - 2)] instanceof TYPE_SPECIFIER_TOKEN)
			tt = ((TypeToken) argTokens[argTokens.length - 1][(argTokens[argTokens.length - 1].length - 1)]);
		else {
			System.err.println("Type not specified, assuming int");
			tt = new TypeToken("int");
		}
		DATA_TYPE type = tt.data_type();
		StringBuilder asm = new StringBuilder();
		for (Token[] arg : argTokens) {
			_LANG_COMPILER.VarManager.VAR_ var = new _LANG_COMPILER.VarManager.VAR_(((IdentifierToken) arg[0]).identifier, type);
			_LANG_COMPILER.vars.add(var);
			IdentifierToken.identifiers.forEach((IdentifierToken id) -> {
				if (id.var == null && var.name.equals(id.identifier)) {
					id.var = var;
					id.data_type = var.type;
				}
			});
			for (Array array : Array.arrays) {
				if (array.type == null && array.name.equals(var.name)) {
					array.type = tt.arrayElementsType();
				}
			}
			String trueName = "var_" + _LANG_COMPILER.var_ind;
			_LANG_COMPILER.memory_values.put(trueName, 0);
			_LANG_COMPILER.memory_constant.put(trueName, false);
			_LANG_COMPILER.memory_required.put(trueName, true);
			_LANG_COMPILER.var_indices.put(var.name, _LANG_COMPILER.var_ind++);
			if (arg.length > 1 && arg[1] instanceof AssignmentToken) {
				_LANG_COMPILER.rec_ind = 0;
				asm.append("CLEAR_CACHE\n");
				asm.append(_LANG_COMPILER.AssemblyMake.valueInstructions(_LANG_COMPILER.tokensFrom(arg, 2)));
				asm.append("movq %r10, var_").append(_LANG_COMPILER.var_indices.get(((IdentifierToken) arg[0]).identifier)).append('\n');
				asm.append("CLEAR_CACHE\n");
			}
		}
		return asm.toString();
	}),
	@SuppressWarnings("unused")
	DEFINE((m, argTokens) -> {
		int as_ind = 0;
		System.out.println(Arrays.deepToString(argTokens));
		while (!(argTokens[0][as_ind] instanceof AS_TOKEN))
			as_ind++;
		Token[] expression = new Token[as_ind - 1];
		System.arraycopy(argTokens[0], 0, expression, 0, expression.length);
		String expressionName = ((IdentifierToken) expression[0]).identifier;
		List<EXPRESSION_PARAMETER> parameters = new ArrayList<>();
		if (expression.length > 1 && expression[1] instanceof ParenthesisOpenedToken) {
			Token[] t = new Token[expression.length - 2];
			System.arraycopy(expression, 2, t, 0, t.length);
			System.out.println(Arrays.deepToString(t));
			Token[][] tk = Objects.requireNonNull(_LANG_COMPILER.Parser.split_by_commas(t));
			System.out.println(Arrays.deepToString(tk));
			for (Token[] tokens : tk) {
				parameters.add(new EXPRESSION_PARAMETER(((IdentifierToken) tokens[0]).identifier));
			}
		}
		Token[] valueTokens = new Token[argTokens[0].length - as_ind - 1];
		System.arraycopy(argTokens[0], as_ind + 1, valueTokens, 0, valueTokens.length);
		System.out.println(Arrays.deepToString(valueTokens));
		_LANG_COMPILER.expressions.add(new EXPRESSION(expressionName, parameters, _LANG_COMPILER.AssemblyMake.valueInstructions(valueTokens)));
		return "";
	});
	private CALLBACK callback;

	METHOD(CALLBACK callback) {
		this.callback = callback;
	}

	public String assembly(Method m, Token[][] argTokens) {
		return callback.assembly(m, argTokens);
	}
}