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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompositeTypeToken extends Token {
	public List<TypeToken> tkns = new ArrayList<>();

	public CompositeTypeToken(TypeToken... tokens) {
		this.tkns.addAll(Arrays.asList(tokens));
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("COMPOSITE_TYPE_T(");
		for (TypeToken t : tkns)
			str.append("(").append(t.toString()).append("),");
		return str.substring(0, str.length() - 1) + ")";
	}

	public DATA_TYPE data_type() {
		return null;
	}
}
