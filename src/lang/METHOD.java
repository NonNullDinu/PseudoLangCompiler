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

import run._LANG_COMPILER;
import tokens.*;

import java.util.Arrays;

public enum METHOD {
	EXIT((m, argTokens) -> {
		if (argTokens != null && argTokens.length == 1) {
			return _LANG_COMPILER.valueInstructions(argTokens[0]) + "\tmov eax, 4\n\tmov ebx, 1\n\tmov ecx, ___end\n\tmov edx, ___end_len\n\tint 0x80\n\tmov rax, r10\n\tcall printNumber\n\tcall printNewLine\n\tmov eax, 1\n\tmov ebx, r10d\n\tint 0x80\n";
		} else
			return "\tmov eax, 4\n\tmov ebx, 1\n\tmov ecx, ___end\n\tmov edx, ___end_len\n\tint 0x80\n\tlea rax, [digits]\n\tcall print_char\n\tcall printNewLine\n\tmov eax, 1\n\tmov ebx, r10d\n\tint 0x80\n";
	}),

	WRITE_TO((m, argTokens) -> {
		String asm = "NULL\n";
		if (argTokens != null && argTokens.length == 2) {
			String name = ((StringToken) argTokens[0][0]).str;
			((PayloadToken) argTokens[1][0]).bind();
			Payload pl = ((PayloadToken) argTokens[1][0]).payload;
			_LANG_COMPILER.addNewVar("file_" + ++_LANG_COMPILER.fileCode + "_path", name + ", 0");
			_LANG_COMPILER.addNewRESWVar("file_" + _LANG_COMPILER.fileCode + "_desc");
			_LANG_COMPILER.addNewVar("file_" + _LANG_COMPILER.fileCode + "_content", pl.payload);
			asm = "\tmov eax, 8\n\tmov ebx, file_" + _LANG_COMPILER.fileCode + "_path\n\tmov ecx, 0q644\n\tint 0x80\n\tmov [file_" + _LANG_COMPILER.fileCode + "_desc], eax\n\tmov eax, 4\n\tmov ebx, [file_" + _LANG_COMPILER.fileCode + "_desc]\n\tmov ecx, file_" + _LANG_COMPILER.fileCode + "_content\n\tmov edx, " + pl.payload.length + "\n\tint 0x80\n\tmov eax, 6\n\tmov ebx, [file_" + _LANG_COMPILER.fileCode + "_desc]\n\tint 0x80\n";
		}
		return asm;
	}),

	DEBUG_PRINT((m, argTokens) -> {
		if (argTokens.length == 1 && argTokens[0].length == 1) {
			if (argTokens[0][0] instanceof StringToken) {
				_LANG_COMPILER.addNewVar("str_" + ++_LANG_COMPILER.strCode, ((StringToken) argTokens[0][0]).str + ", 10, 0");
				return "\tmov eax, 4\n\tmov ebx, 1\n\tmov ecx, str_" + _LANG_COMPILER.strCode + "\n\tmov edx, " + (((StringToken) argTokens[0][0]).str.length()) + "\n\tint 0x80\n";
			} else if (argTokens[0][0] instanceof NumberToken) {
				String val = Integer.toString(((NumberToken) argTokens[0][0]).v);
				_LANG_COMPILER.addNewVar("str_" + ++_LANG_COMPILER.strCode, val);
				return "\tmov eax, 4\n\tmov ebx, 1\n\tmov ecx, str_" + _LANG_COMPILER.strCode + "\n\tmov edx, " + (val.length()) + "\n\tint 0x80\n\tcall printNewLine\n";
			} else if (argTokens[0][0] instanceof IdentifierToken) {
				return "\tmov r8, 1\n" + _LANG_COMPILER.printIdentifier(argTokens[0][0]);
			}
		}
		return "";
	}),

	CONTENT_OF_FILE_EQUALS((m, argTokens) -> null),

	ERROR((m, argTokens) -> {
		if (argTokens.length == 1 && argTokens[0].length == 1) {
			if (argTokens[0][0] instanceof StringToken) {
				_LANG_COMPILER.addNewVar("str_" + ++_LANG_COMPILER.strCode, ((StringToken) argTokens[0][0]).str + ", 10, 0");
				return "\tmov eax, 4\n\tmov ebx, 2\n\tmov ecx, str_" + _LANG_COMPILER.strCode + "\n\tmov edx, " + (((StringToken) argTokens[0][0]).str.length()) + "\n\tint 0x80\n";
			} else if (argTokens[0][0] instanceof NumberToken) {
				String val = Integer.toString(((NumberToken) argTokens[0][0]).v);
				_LANG_COMPILER.addNewVar("str_" + ++_LANG_COMPILER.strCode, val);
				return "\tmov eax, 4\n\tmov ebx, 2\n\tmov ecx, str_" + _LANG_COMPILER.strCode + "\n\tmov edx, " + (val.length()) + "\n\tint 0x80\n\tcall printNewLine\n";
			} else if (argTokens[0][0] instanceof IdentifierToken) {
				return "\tmov r8, 2\n" + _LANG_COMPILER.printIdentifier(argTokens[0][0]);
			}
		}
		return "";
	}),
	DEFINED_METHOD((m, argTokens) -> {
		StringBuilder methodBody = new StringBuilder();
		if (argTokens != null && argTokens.length != 0)
			for (int i = argTokens.length - 1; i >= 0; i--) {
				_LANG_COMPILER.rec_ind = 0;
				methodBody.append(_LANG_COMPILER.valueInstructions(argTokens[i])).append("\tpush r10\n");
			}
		methodBody.append("\tcall ").append(m.name).append("\n");
		if (argTokens != null && argTokens.length != 0)
			methodBody.append("\tpop r10\n".repeat(argTokens.length));
		return methodBody.toString();
	}),
	PRINT_DIGIT((m, argTokens) -> {
		_LANG_COMPILER.rec_ind = 0;
		return _LANG_COMPILER.valueInstructions(argTokens[0]) + "\tlea eax, [digits + r10]\n\tcall print_char\n";
	}),
	ASM((m, argTokens) -> {
		StringBuilder asm = new StringBuilder();
		for (Token[] t : argTokens) asm.append(((StringToken) t[0]).str.replaceAll("\"", "")).append('\n');
		return asm.toString();
	}),
	READ((m, argTokens) -> {
		StringBuilder methodBody = new StringBuilder();
		for (Token[] tk :
				argTokens) {
			_LANG_COMPILER.rec_ind = 0;
			methodBody.append("\n\tcall readValue\n");
			methodBody.append("\tmov [" + ((IdentifierToken) tk[0]).identifier + "], rax//POINTER\n");
		}
		return methodBody.toString();
	}),
	WRITE((m, argTokens) -> {
		StringBuilder asm = new StringBuilder();
		for (Token[] a : argTokens) {
			if (a[0] instanceof StringToken) {
				if (!((StringToken) a[0]).str.equals("\"\\n\"")) {
					_LANG_COMPILER.addNewVar("str_" + ++_LANG_COMPILER.strCode, ((StringToken) a[0]).str + ", 0");
					asm.append("\tmov eax, 4\n\tmov ebx, 1\n\tmov ecx, str_").append(_LANG_COMPILER.strCode).append("\n\tmov edx, ").append(((StringToken) a[0]).str.length() - 2).append("\n\tint 0x80\n");
				} else
					asm.append("\tcall printNewLine\n");
			} else {
				_LANG_COMPILER.rec_ind = 0;
				System.out.println(Arrays.deepToString(a));
				asm.append(_LANG_COMPILER.valueInstructions(a)).append("\tmov rax, r10\n\tmov r8, 1\n\tcall printNumber\n");
			}
		}
		return asm.toString();
	});
	private CALLBACK callback;

	METHOD(CALLBACK callback) {
		this.callback = callback;
	}

	public String assembly(Token[][] argTokens) {
		return callback.assembly(null, argTokens);
	}

	public String assembly(Method m, Token[][] argTokens) {
		return callback.assembly(m, argTokens);
	}
}
