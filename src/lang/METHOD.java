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

import java.util.Objects;

public enum METHOD {
	EXIT((m, argTokens) -> {
		if (argTokens != null && argTokens.length > 0) {
			return _LANG_COMPILER.AssemblyMake.valueInstructions(argTokens[0]) + "\tmov %r10, %rax\n\tcall exit\n";
		} else
			return "\tmovq $0, %rax\n\tcall exit\n";
	}),

//	ERROR((m, argTokens) -> {
//		if (argTokens.length == 1 && argTokens[0].length == 1) {
//			if (argTokens[0][0] instanceof StringToken) {
//				_LANG_COMPILER.addNewVar("str_" + ++_LANG_COMPILER.strCode, ((StringToken) argTokens[0][0]).str + ", 10, 0");
//				return "\tmov eax, 4\n\tmov ebx, 2\n\tmov ecx, str_" + _LANG_COMPILER.strCode + "\n\tmov edx, " + (((StringToken) argTokens[0][0]).str.length()) + "\n\tsyscall\n";
//			} else if (argTokens[0][0] instanceof NumberToken) {
//				String val = Integer.toString(((NumberToken) argTokens[0][0]).v);
//				_LANG_COMPILER.addNewVar("str_" + ++_LANG_COMPILER.strCode, val);
//				return "\tmov eax, 4\n\tmov ebx, 2\n\tmov ecx, str_" + _LANG_COMPILER.strCode + "\n\tmov edx, " + (val.length()) + "\n\tsyscall\n\tcall printNewLine\n";
//			} else if (argTokens[0][0] instanceof IdentifierToken) {
//				return "\tmov r8, 2\n" + _LANG_COMPILER.printIdentifier(argTokens[0][0]);
//			}
//		}
//		return "";
//	}),

	DEFINED_METHOD((m, argTokens) -> {
		StringBuilder methodBody = new StringBuilder();
		if (argTokens != null && argTokens.length != 0)
			for (int i = argTokens.length - 1; i >= 0; i--) {
				_LANG_COMPILER.rec_ind = 0;
				methodBody.append(_LANG_COMPILER.AssemblyMake.valueInstructions(argTokens[i])).append("\tpushq %r10\n");
			}
		methodBody.append("\tcall ").append(m.name).append("\n");
		if (argTokens != null && argTokens.length != 0)
			methodBody.append("\tpopq %r10\n".repeat(argTokens.length));
		return methodBody.toString();
	}),

	PRINT_DIGIT((m, argTokens) -> {
		_LANG_COMPILER.rec_ind = 0;
		return _LANG_COMPILER.AssemblyMake.valueInstructions(argTokens[0]) + "\tleaq digits(,%r10,1), %rax\n\tcall print_char\n";
	}),
	ASM((m, argTokens) -> {
		StringBuilder asm = new StringBuilder();
		for (Token[] t : argTokens) asm.append(((StringToken) t[0]).str.replaceAll("\"", "")).append('\n');
		return asm.toString();
	}),
	READ((m, argTokens) -> {
		StringBuilder asm = new StringBuilder();
		for (int i = 0; i < argTokens.length; i++) {
			Token[] tk = argTokens[i];
			if (i == 0 && tk[0] instanceof FILE_TOKEN) {
				asm.append("\tmovzxw var_").append(_LANG_COMPILER.var_indices.get(((IdentifierToken) tk[1]).identifier)).append(", %r8\n");
			} else {
				if (i == 0)
					asm.append("\tmovq $0, %r8\n");
				_LANG_COMPILER.rec_ind = 0;
				asm.append("\tcall readValue\n");
				asm.append("\tmovq %rax, var_").append(_LANG_COMPILER.var_indices.get(((IdentifierToken) tk[0]).identifier)).append("//POINTER\n");
			}
		}
		return asm.toString();
	}),
	WRITE((m, argTokens) -> {
		StringBuilder asm = new StringBuilder();

		for (int i = 0; i < argTokens.length; i++) {
			Token[] a = argTokens[i];
			if (i == 0) {
				if (a[0] instanceof FILE_TOKEN) {
					asm.append("\tmovzxw var_").append(_LANG_COMPILER.var_indices.get(((IdentifierToken) a[1]).identifier)).append(", %r8\n");
					continue;
				} else asm.append("\tmov $1, %r8\n");
			}
			if (a[0] instanceof StringToken) {
				if (!((StringToken) a[0]).str.equals("\"\\n\"")) {
					_LANG_COMPILER.VarManager.VAR_ str = _LANG_COMPILER.lookupStringVar(".asciz " + ((StringToken) a[0]).str);
					if (str == null) {
						_LANG_COMPILER.addNewVar("str_" + ++_LANG_COMPILER.strCode, ".asciz " + ((StringToken) a[0]).str);
						str = _LANG_COMPILER.lookupStringVar(".asciz " + ((StringToken) a[0]).str);
					}
					asm.append("\tmovq $1, %rax\n\tmovq %r8, %rdi\n\tmovq $").append(str.name).append(", %rsi\n\tmovq $").append(((StringToken) a[0]).str.length() - 2).append(", %rdx\n\tsyscall\n");
				} else
					asm.append("\tcall printNewLine\n");
			} else {
				_LANG_COMPILER.rec_ind = 0;
				asm.append(_LANG_COMPILER.AssemblyMake.valueInstructions(a)).append("\tmov %r10, %rax\n\tcall printNumber\n");
			}
		}
		return asm.toString();
	}),
	OPEN(((m, argTokens) -> {
		String asm = "";
		if (argTokens[0][0] instanceof FILE_TOKEN) {
			//argTokens[0][1] = name of variable
			//argTokens[1][0] = name of file
			//argTokens[2][0] = file access
			//argTokens[3][0] = file permissions(not neded if argTokens[3][0] is "read only")
			IdentifierToken name = ((IdentifierToken) argTokens[0][1]);
			String file = ((StringToken) argTokens[1][0]).str;
			FILE_ACCESS_TOKEN access = ((FILE_ACCESS_TOKEN) argTokens[2][0]);
			_LANG_COMPILER.addNewVar("file_" + ++_LANG_COMPILER.fileCode + "_path", ".asciz " + file);
			if (access.access.equals(FILE_ACCESS.WRITE_ONLY)) {
				int perm = ((NumberToken) argTokens[3][0]).v;
				asm = "\tmovq $file_" + _LANG_COMPILER.fileCode + "_path, %rax\n" +
						"\tmovq $0" + perm + ", %rbx\n" +
						"\tcall " + access.access.func_open() + "\n" +
						"\tmovw %ax, var_" + _LANG_COMPILER.var_indices.get(name.identifier) + "//NO_DELETE\n";
			} else {
				asm = "\tmovq $file_" + _LANG_COMPILER.fileCode + "_path, %rax\n" +
						"\tcall " + access.access.func_open() + "\n" +
						"\tmovw %ax, var_" + _LANG_COMPILER.var_indices.get(name.identifier) + "//NO_DELETE\n";
			}
		}
		return asm;
	})),
	CLOSE(((m, argTokens) -> {
		if (argTokens[0][0] instanceof FILE_TOKEN) {
			return "\tmovzxw var_" + _LANG_COMPILER.var_indices.get(((IdentifierToken) argTokens[0][1]).identifier) + ", %rax\n" +
					"\tcall f_close\n";
		}
		return "";
	})),
	ALLOCATE((m, argTokens) -> {
		//argTokens[0] type
		//argTokens[1] name(of type pointer)
		//argTokens[2] size
		TypeToken tt = argTokens[0][0] instanceof TypeToken ? ((TypeToken) argTokens[0][0]) : null;
		CompositeTypeToken ctt = argTokens[0][0] instanceof CompositeTypeToken ? ((CompositeTypeToken) argTokens[0][0]) : null;
		for (Array a : Array.arrays)
			if (a.name.equals(((IdentifierToken) argTokens[1][0]).identifier))
				a.setType(tt != null ? tt.data_type() : ctt != null ? ctt.data_type() : null);
		_LANG_COMPILER.VarManager.setVarSize(((IdentifierToken) argTokens[1][0]).identifier, Objects.requireNonNull(_LANG_COMPILER.AssemblyMake.evaluate(argTokens[2])).vi);
		return "";
	}),
	SORT(((m, argTokens) -> {
		//BEGIN
		_LANG_COMPILER.rec_ind = 0;
		String v1 = _LANG_COMPILER.AssemblyMake.valueInstructions(argTokens[0]) + "\tpushq %r10\n";
		//END
		_LANG_COMPILER.rec_ind = 0;
		String v2 = _LANG_COMPILER.AssemblyMake.valueInstructions(argTokens[1]) + "\tmovq %r10, %rsi\n";
		return v1 + v2 + "\tpopq %rdi\n\tcall merge_sort\n";
	})),
	REVERSE_SORT(((m, argTokens) -> {
		//BEGIN
		_LANG_COMPILER.rec_ind = 0;
		String v1 = _LANG_COMPILER.AssemblyMake.valueInstructions(argTokens[0]) + "\tpushq %r10\n";
		//END
		_LANG_COMPILER.rec_ind = 0;
		String v2 = _LANG_COMPILER.AssemblyMake.valueInstructions(argTokens[1]) + "\tmovq %r10, %rbx\n";
		return v1 + v2 + "\tpopq %rax\n\tcall reverse_sort\n";
	})),
	REVERSE(((m, argTokens) -> {
		//BEGIN
		_LANG_COMPILER.rec_ind = 0;
		String v1 = _LANG_COMPILER.AssemblyMake.valueInstructions(argTokens[0]) + "\tpushq %r10\n";
		//END
		_LANG_COMPILER.rec_ind = 0;
		String v2 = _LANG_COMPILER.AssemblyMake.valueInstructions(argTokens[1]) + "\tmovq %r10, %rbx\n";
		return v1 + v2 + "\tpopq %rax\n\tcall reverse\n";
	})),
	SWAP((m, argTokens) -> {
		return "\tmovq $var_" + _LANG_COMPILER.var_indices.get(((IdentifierToken) argTokens[0][0]).identifier) + ", %rcx\n\tmovq $var_" + _LANG_COMPILER.var_indices.get(((IdentifierToken) argTokens[1][0]).identifier) + ", %rdx\n\tcall swap\n";
	});
	private CALLBACK callback;

	METHOD(CALLBACK callback) {
		this.callback = callback;
	}

//	public String assembly(Token[][] argTokens) {
//		return callback.assembly(null, argTokens);
//	}

	public String assembly(Method m, Token[][] argTokens) {
		return callback.assembly(m, argTokens);
	}
}
