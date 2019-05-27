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

import main._LANG_COMPILER;
import variables.DATA_TYPE;

import java.util.ArrayList;
import java.util.List;

public class IdentifierToken extends Token {
	public static final List<IdentifierToken> identifiers = new ArrayList<>();
	public _LANG_COMPILER.VarManager.VAR_ var;
	public String identifier;
	public DATA_TYPE data_type;

	public IdentifierToken(String identifier, DATA_TYPE dt, _LANG_COMPILER.VarManager.VAR_ var) {
		this.identifier = identifier;
		this.data_type = dt;
		this.var = var;
		identifiers.add(this);
	}

	@Override
	public String toString() {
		return "IDENTIFIER(" + identifier + ")";
	}
}