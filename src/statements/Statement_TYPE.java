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

package statements;

import tokens.*;

public enum Statement_TYPE {
	VAR_DECLARE,
	VAR_UPDATE,
	CONDITIONAL,
	WHILE_LOOP,
	INCREMENT,
	FOR_LOOP,
	METHOD_CALL;

	public boolean fits(Token[] t, int ind) {
		boolean ret = false;
		switch (this) {
			case VAR_DECLARE: {
				ret = ind < t.length - 2 && (t[ind] instanceof CompositeTypeToken || t[ind] instanceof TypeToken) && (t[ind + 1] instanceof NameToken || t[ind + 1] instanceof IdentifierToken) && (t[ind + 2] instanceof AssignmentToken || t[ind + 2] instanceof NewLineToken);
				break;
			}
			case VAR_UPDATE: {
				ret = ind < t.length - 1 && t[ind] instanceof IdentifierToken && t[ind + 1] instanceof AssignmentToken;
				break;
			}
			case CONDITIONAL:
				ret = ind < t.length - 1 && t[ind] instanceof IfToken && t[ind + 1] instanceof ParenthesisOpenedToken;
				break;
			case WHILE_LOOP:
				ret = ind < t.length - 1 && t[ind] instanceof WhileToken && t[ind + 1] instanceof ParenthesisOpenedToken;
				break;
			case INCREMENT:
				ret = ind < t.length - 1 && t[ind] instanceof IdentifierToken && t[ind + 1] instanceof IncrementToken;
				break;
			case METHOD_CALL:
				ret = t[ind] instanceof IdentifierToken;
				break;
			case FOR_LOOP:
				ret = t[ind] instanceof ForToken && t[ind + 1] instanceof IdentifierToken && t[ind + 2] instanceof AssignmentToken;
				break;
		}
		return ret;
	}
}