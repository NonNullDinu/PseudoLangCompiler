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

package variables;

public enum DATA_TYPE {
	LONG("^long(\\sint)?$", "resq", "QWORD", "QWORD", 8),
	INT("^int$", "resd", "DWORD", "DWORD", 4),
	STRING("^[Ss]tring$", "resb", "BYTE", "BYTE", 8),
	BOOL("^bool(ean)?$", "resb", "BYTE", "BYTE", 1),
	SHORT_INT("^short(\\sint)?$", "resw", "WORD", "WORD", 2),
	POINTER("^(.|\\s)+\\*+$", "resq", "QWORD", "QWORD", 8),
	BYTE_STREAM("[^.]*", "resb", "BYTE", "BYTE", 8);
	public String wrdtype;
	public String asm_type;
	public String pattern;
	public String pushkeyword;
	public int bytesize;

	DATA_TYPE(String regex, String asm_type, String wrdtype, String pushkeyword, int bytesize) {
		this.pattern = regex;
		this.asm_type = asm_type;
		this.wrdtype = wrdtype;
		this.pushkeyword = pushkeyword;
		this.bytesize = bytesize;
	}
}
