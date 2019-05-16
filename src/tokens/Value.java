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

import variables.*;

public class Value {
	public byte[] vbs;
	public DATA_TYPE type;
	public int vi;
	public boolean vb;
	public String vs;

	public Value(int v) {
		this.type = DATA_TYPE.INT;
		this.vi = v;
	}

	public Value(boolean v) {
		this.type = DATA_TYPE.BOOL;
		this.vb = v;
	}

	public Value(String v) {
		this.type = DATA_TYPE.STRING;
		this.vs = v;
	}

	public Value(Variable v) {
		this.type = v.type;
		switch (type) {
			case INT:
				vi = ((Variable_INT) v).v;
				break;
			case STRING:
				vs = ((Variable_STRING) v).v;
				break;
			case BOOL:
				vb = ((Variable_BOOL) v).v;
				break;
		}
	}

	public Value(Payload payload) {
		type = DATA_TYPE.BYTE_STREAM;
		this.vbs = payload.payload;
	}
}