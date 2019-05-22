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

package tokens;

import run._LANG_COMPILER;

public class OperatorToken extends Token {
	public Math_Operator mop;

	public OperatorToken(Math_Operator op) {
		this.mop = op;
	}

	public int result(int a, int b) {
		return mop.res.op(a, b);
	}

	public String asm_code(String a, String b) {
		return mop.asm_code(a, b);
	}

	@Override
	public String toString() {
		return "OP(" + mop.name() + ")";
	}

	static int LOGIC_TAG = 0;

	public enum Math_Operator {
		ADD(Integer::sum), SUBTRACT((a, b) -> a - b), MULTIPLY((a, b) -> a * b), DIVIDE((a, b) -> a / b), LOGIC_AND((a, b) -> a != 0 && b != 0 ? 1 : 0), LOGIC_OR((a, b) -> a != 0 || b != 0 ? 1 : 0), LOGIC_XOR((a, b) -> a != 0 ^ b != 0 ? 1 : 0),
		LOGIC_E((a, b) -> a == b ? 1 : 0), LOGIC_NE((a, b) -> a != b ? 1 : 0), LOGIC_G((a, b) -> a > b ? 1 : 0), LOGIC_GE((a, b) -> a >= b ? 1 : 0), LOGIC_S((a, b) -> a < b ? 1 : 0), LOGIC_SE((a, b) -> a <= b ? 1 : 0), MODULO((a, b) -> a % b), SHIFT_LEFT((a, b) -> a << b), SHIFT_RIGHT((a, b) -> a >> b), BITWISE_AND((a, b) -> a & b), BITWISE_OR((a, b) -> a | b), BITWISE_XOR((a, b) -> a ^ b);
		Math_Result res;

		Math_Operator(Math_Result res) {
			this.res = res;
		}

		public float op(int a, int b) {
			return res.op(a, b);
		}

		public static int log2(int bits) // returns 0 for bits=0
		{
			int log = 0;
			if ((bits & 0xffff0000) != 0) {
				bits >>>= 16;
				log = 16;
			}
			if (bits >= 256) {
				bits >>>= 8;
				log += 8;
			}
			if (bits >= 16) {
				bits >>>= 4;
				log += 4;
			}
			if (bits >= 4) {
				bits >>>= 2;
				log += 2;
			}
			return log + (bits >>> 1);
		}

		public boolean isPow2(int x) {
			int bits = 0;
			while (bits < 2 && x != 0) {
				bits += x & 1;
				x >>= 1;
			}
			return bits == 1;
		}

		public String asm_code(String a, String b) {
			String asm = "";
			boolean bvalue = b.matches(NumberToken.number);
			int bv = 0;
			int log_int = 0;
			boolean pow2 = false;
			if (bvalue) {
				bv = Integer.parseInt(b);
				pow2 = isPow2(bv);
				log_int = log2(bv);
			}
			switch (this) {
				case ADD:
					if (_LANG_COMPILER.isConstant(b))
						asm = "add " + a + ", " + _LANG_COMPILER.cvalue(b) + "\n";
					else asm = "add " + a + ", " + b + "\n";
					break;
				case SUBTRACT:
					if (_LANG_COMPILER.isConstant(b))
						asm = "sub " + a + ", " + _LANG_COMPILER.cvalue(b) + "\n";
					else asm = "sub " + a + ", " + b + "\n";
					break;
				case DIVIDE: {
					if (bvalue && pow2)
						asm = "shr " + a + ", " + log_int + "\n";
					else
						asm = "mov eax, " + _LANG_COMPILER.reg(a).addressing.x32.name + "\n\tmov edx, 0\n\tdiv " + _LANG_COMPILER.reg(b).addressing.x32.name + "\n\tmov " + _LANG_COMPILER.reg(a).addressing.x32.name + ", eax\n";
					break;
				}
				case MULTIPLY:
					if (bvalue && pow2)
						asm = "shl " + a + ", " + log_int + "\n";
					else
						asm = "mov edx, 0\n\tmov eax, " + _LANG_COMPILER.reg(a).addressing.x32.name + "\n\tmul " + _LANG_COMPILER.reg(b).addressing.x32.name + "\n\tmov " + _LANG_COMPILER.reg(a).addressing.x32.name + ", edx\n\tshl " + _LANG_COMPILER.reg(a).addressing.x64.name + ", 32\n\tor " + _LANG_COMPILER.reg(a).addressing.x32.name + ", eax\n";
					break;
				case MODULO:
					if (bvalue && pow2)
						asm = "and " + a + ", " + (bv - 1);
					else
						asm = "mov eax, " + _LANG_COMPILER.reg(a).addressing.x32.name + "\n\tmov edx, 0\n\tdiv " + _LANG_COMPILER.reg(b).addressing.x32.name + "\n\tmov " + _LANG_COMPILER.reg(a).addressing.x32.name + ", edx\n";
					break;
				case LOGIC_E:
					++LOGIC_TAG;
					asm = "cmp " + a + ", " + b + "\n\tmov " + a + ", 0\njne .LOGIC_" + (LOGIC_TAG) + "\n\tmov " + a + ", 1\n.LOGIC_" + LOGIC_TAG + ":\n";
					break;
				case LOGIC_NE:
					++LOGIC_TAG;
					asm = "cmp " + a + ", " + b + "\n\tmov " + a + ", 0\nje .LOGIC_" + (LOGIC_TAG) + "\n\tmov " + a + ", 1\n.LOGIC_" + LOGIC_TAG + ":\n";
					break;
				case LOGIC_S:
					++LOGIC_TAG;
					asm = "cmp " + a + ", " + b + "\n\tmov " + a + ", 0\njge .LOGIC_" + (LOGIC_TAG) + "\n\tmov " + a + ", 1\n.LOGIC_" + LOGIC_TAG + ":\n";
					break;
				case LOGIC_SE:
					++LOGIC_TAG;
					asm = "cmp " + a + ", " + b + "\n\tmov " + a + ", 0\njg .LOGIC_" + (LOGIC_TAG) + "\n\tmov " + a + ", 1\n.LOGIC_" + LOGIC_TAG + ":\n";
					break;
				case LOGIC_G:
					++LOGIC_TAG;
					asm = "cmp " + a + ", " + b + "\n\tmov " + a + ", 0\njle .LOGIC_" + (LOGIC_TAG) + "\n\tmov " + a + ", 1\n.LOGIC_" + LOGIC_TAG + ":\n";
					break;
				case LOGIC_GE:
					++LOGIC_TAG;
					asm = "cmp " + a + ", " + b + "\n\tmov " + a + ", 0\njl .LOGIC_" + (LOGIC_TAG) + "\n\tmov " + a + ", 1\n.LOGIC_" + LOGIC_TAG + ":\n";
					break;
				case LOGIC_AND:
					asm = "and " + a + ", " + b + "\n\tand " + a + ", 1\n";
					break;
				case LOGIC_OR:
					asm = "or " + a + ", " + b + "\n\tand " + a + ", 1\n";
					break;
				case LOGIC_XOR:
					asm = "xor " + a + ", " + b + "\n\tand " + a + ", 1\n";
					break;
				case SHIFT_LEFT:
					if (bvalue)
						asm = "shl " + a + ", " + bv + "\n";
					else
						asm = "mov cl, " + _LANG_COMPILER.reg(b).addressing.x8.name + "\n\tshl " + a + ", cl\n";
					break;
				case SHIFT_RIGHT:
					if (bvalue)
						asm = "shl " + a + ", " + bv + "\n";
					else
						asm = "mov cl, " + _LANG_COMPILER.reg(b).addressing.x8.name + "\n\tshr " + a + ", cl\n";
					break;
				case BITWISE_AND:
					asm = "and " + a + ", " + b + "\n";
					break;
				case BITWISE_OR:
					asm = "or " + a + ", " + b + "\n";
					break;
				case BITWISE_XOR:
					asm = "xor " + a + ", " + b + "\n";
					break;
			}
			return "\t" + asm;
		}
	}

	private interface Math_Result {
		int op(int a, int b);
	}
}