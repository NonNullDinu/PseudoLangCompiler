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

import java.util.regex.Pattern;

public class UnaryOperatorToken extends Token {
	public static String referenceVar = "";
	public final OP op;

	public UnaryOperatorToken(OP op) {
		this.op = op;
	}

	@Override
	public String toString() {
		return "UOT(" + op.name() + ")";
	}

	public String asm_code(String reg) {
		return op.asm_code(reg);
	}

	public enum OP {
		BITWISE_NOT("~");
		String pattern;

		OP(String pattern) {
			this.pattern = Pattern.quote(pattern);
		}

		public String asm_code(String reg) {
			String asm = "";
			switch (this) {
				case BITWISE_NOT:
					asm = "not " + reg;
					break;
			}
			return "\t" + asm + "\n";
		}
	}
}