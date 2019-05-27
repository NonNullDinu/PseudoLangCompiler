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

import variables.DATA_TYPE;

public class TypeToken extends Token {
	private final String type;

	public TypeToken(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "TT(\"" + type + "\")";
	}

	public DATA_TYPE data_type() {
		switch (type) {
			case "int":
			case "intreg":
				return DATA_TYPE.LONG;
			case "file_stream":
				return DATA_TYPE.SHORT_INT;
			case "pointer":
				return DATA_TYPE.POINTER;
			default:
				return null;
		}
	}
}
