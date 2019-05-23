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
						asm = "add $" + _LANG_COMPILER.cvalue(b) + ", " + a + "\n";
					else asm = "add " + b + ", " + a + "\n";
					break;
				case SUBTRACT:
					if (_LANG_COMPILER.isConstant(b))
						asm = "sub $" + _LANG_COMPILER.cvalue(b) + ", " + a + "\n";
					else asm = "sub " + b + ", " + a + "\n";
					break;
				case DIVIDE: {
					if (bvalue && pow2)
						asm = "shr $" + log_int + ", " + a + "\n";
					else
						asm = "movl %" + _LANG_COMPILER.reg(a).addressing.x32.name + ", %eax\n\tmovl $0, %edx\n\tdiv %" + _LANG_COMPILER.reg(b).addressing.x32.name + "\n\tmovl %eax, %" + _LANG_COMPILER.reg(a).addressing.x32.name + "\n";
					break;
				}
				case MULTIPLY:
					if (bvalue && pow2)
						asm = "shl $" + log_int + ", " + a + "\n";
					else
						asm = "movl $0, %edx\n\tmovl %" + _LANG_COMPILER.reg(a).addressing.x32.name + ", %eax\n\tmul %" + _LANG_COMPILER.reg(b).addressing.x32.name + "\n\tmovl %edx, %" + _LANG_COMPILER.reg(a).addressing.x32.name + "\n\tshl $32, %" + _LANG_COMPILER.reg(a).addressing.x64.name + "\n\tor %eax, %" + _LANG_COMPILER.reg(a).addressing.x32.name + "\n";
					break;
				case MODULO:
					if (bvalue && pow2)
						asm = "and $" + (bv - 1) + ", " + a;
					else
						asm = "movl %" + _LANG_COMPILER.reg(a).addressing.x32.name + ", %eax\n\tmovl $0, %edx\n\tdiv %" + _LANG_COMPILER.reg(b).addressing.x32.name + "\n\tmovl %edx, %" + _LANG_COMPILER.reg(a).addressing.x32.name + "\n";
					break;
				case LOGIC_E:
					++LOGIC_TAG;
					asm = "cmpq %" + b + ", %" + a + "\n\tmovq $0, %" + a + "\njne .LOGIC_" + (LOGIC_TAG) + "\n\tmovq $1, %" + a + "\n.LOGIC_" + LOGIC_TAG + ":\n";
					break;
				case LOGIC_NE:
					++LOGIC_TAG;
					asm = "cmpq %" + b + ", %" + a + "\n\tmovq $0, %" + a + "\nje .LOGIC_" + (LOGIC_TAG) + "\n\tmovq $1, %" + a + "\n.LOGIC_" + LOGIC_TAG + ":\n";
					break;
				case LOGIC_S:
					++LOGIC_TAG;
					asm = "cmpq %" + b + ", %" + a + "\n\tmovq $0, %" + a + "\njge .LOGIC_" + (LOGIC_TAG) + "\n\tmovq $1, %" + a + "\n.LOGIC_" + LOGIC_TAG + ":\n";
					break;
				case LOGIC_SE:
					++LOGIC_TAG;
					asm = "cmpq %" + b + ", %" + a + "\n\tmovq $0, %" + a + "\njg .LOGIC_" + (LOGIC_TAG) + "\n\tmovq $1, %" + a + "\n.LOGIC_" + LOGIC_TAG + ":\n";
					break;
				case LOGIC_G:
					++LOGIC_TAG;
					asm = "cmpq %" + b + ", %" + a + "\n\tmovq $0, %" + a + "\njle .LOGIC_" + (LOGIC_TAG) + "\n\tmovq $1, %" + a + "\n.LOGIC_" + LOGIC_TAG + ":\n";
					break;
				case LOGIC_GE:
					++LOGIC_TAG;
					asm = "cmpq %" + b + ", %" + a + "\n\tmovq $0, %" + a + "\njl .LOGIC_" + (LOGIC_TAG) + "\n\tmovq $1, %" + a + "\n.LOGIC_" + LOGIC_TAG + ":\n";
					break;
				case LOGIC_AND:
					asm = "and %" + a + ", %" + b + "\n\tand $1, %" + a + "\n";
					break;
				case LOGIC_OR:
					asm = "or %" + a + ", %" + b + "\n\tand $1, %" + a + "\n";
					break;
				case LOGIC_XOR:
					asm = "xor %" + a + ", %" + b + "\n\tand $1, %" + a + "\n";
					break;
				case SHIFT_LEFT:
					if (bvalue)
						asm = "shl $" + bv + ", %" + a + "\n";
					else
						asm = "movb %" + _LANG_COMPILER.reg(b).addressing.x8.name + ", %cl\n\tshl %cl, %" + a + "\n";
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