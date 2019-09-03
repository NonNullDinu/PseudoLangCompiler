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

public class ASMOP {

	public final String OP;
	public final OPERAND arg1, arg2;
	public String comment;
	public boolean isLabel;
	public boolean isJump;

	public ASMOP(String OP, OPERAND arg1, OPERAND arg2) {
		this.OP = OP;
		this.arg1 = arg1;
		this.arg2 = arg2;
		isLabel = OP.endsWith(":");
		isJump = OP.matches("^j(mp|g|ge|l|le|e|ne|nz|z|s|ns|c|nc)$");
	}

	public ASMOP withComment(String comment) {
		this.comment = comment;
		return this;
	}

// --Commented out by Inspection START (7/22/19, 12:24 AM):
//	public void print() {
//		System.out.print(OP);
//		if (arg1 != null) System.out.print(" " + arg1.value);
//		if (arg2 != null) System.out.print(", " + arg2.value);
//		System.out.println();
//	}
// --Commented out by Inspection STOP (7/22/19, 12:24 AM)
}