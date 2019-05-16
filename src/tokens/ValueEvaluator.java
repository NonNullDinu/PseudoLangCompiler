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

import lang.exceptions.TokenException;
import variables.Variable;
import variables.Variable_INT;

import java.util.Arrays;
import java.util.Map;

public class ValueEvaluator {
	public static Value evaluate(Token[] tokens, Map<String, Variable> variables) {
		if (tokens.length == 1) {
			if (tokens[0] instanceof NumberToken) {
				return new Value(((NumberToken) tokens[0]).v);
			} else if (tokens[0] instanceof IdentifierToken) {
				return new Value(variables.get((((IdentifierToken) tokens[0]).identifier)));
			} else if (tokens[0] instanceof LogicConstantValueToken) {
				return new Value(((LogicConstantValueToken) tokens[0]).v);
			} else if (tokens[0] instanceof PayloadToken) {
				if (((PayloadToken) tokens[0]).payload == null)
					((PayloadToken) tokens[0]).bind();
				return new Value(((PayloadToken) tokens[0]).payload);
			} else if (tokens[0] instanceof StringToken) {
				return new Value(((StringToken) tokens[0]).str);
			} else {
				System.out.println("One token unknown type value: " + tokens[0]);
				return null;
			}
		} else {
			int current = 0;
			for (int i = 0; i < tokens.length; i++) {
				if (tokens[i] instanceof ParenthesisOpenedToken) {
					int d = 1;
					int b = i + 1, e = b;
					for (int j = b; j < tokens.length; j++) {
						if (tokens[j] instanceof ParenthesisOpenedToken) {
							d++;
						} else if (tokens[j] instanceof ParenthesisClosedToken) {
							d--;
							if (d == 0) {
								e = j - 1;
								break;
							}
						}
					}
					if (d != 0)
						throw new TokenException("Parenthesis not closed");
					i = e + 1;
					Token[] tkns = Arrays.copyOfRange(tokens, b, e + 1);
					int len = e - b + 2;
					System.out.println(Arrays.toString(tkns));
					for (int j = 1; j <= 5; j++) {
						tkns = evaluate(tkns, variables, j);
						System.out.println(Arrays.toString(tkns));
					}
					if (tkns.length != 1)
						throw new TokenException("Could not resolve the value");
					Token[] tt = new Token[tokens.length - len];
					if (b - 1 >= 0) System.arraycopy(tokens, 0, tt, 0, b - 1);
					tt[b - 1] = tkns[0];
					if (tokens.length - len - b >= 0) System.arraycopy(tokens, b + len, tt, b, tokens.length - len - b);
					System.out.println(Arrays.toString(tokens));
					tokens = tt;
				}
			}
			System.out.println(Arrays.toString(tokens));
			for (int j = 2; j <= 5; j++)
				tokens = evaluate(tokens, variables, j);
			System.out.println(Arrays.toString(tokens));

			current = ((NumberToken) tokens[0]).v;
			return new Value(current);
		}
	}

	private static Token[] evaluate(Token[] tokens, Map<String, Variable> variables, int stage) {
		if (tokens.length == 1) {
			return tokens;
		} else if (stage == 1) {
			for (int i = 0; i < tokens.length; i++) {
				if (tokens[i] instanceof ParenthesisOpenedToken) {
					int d = 1;
					int b = i + 1, e = b;
					for (int j = b; j < tokens.length; j++) {
						if (tokens[j] instanceof ParenthesisOpenedToken) {
							d++;
						} else if (tokens[j] instanceof ParenthesisClosedToken) {
							d--;
							if (d == 0) {
								e = j - 1;
								break;
							}
						}
					}
					if (d != 0)
						throw new TokenException("Parenthesis not closed");
					i = e + 1;
					Token[] tkns = Arrays.copyOfRange(tokens, b, e + 1);
					for (int j = 1; j <= 5; j++) {
						tkns = evaluate(tkns, variables, j);
						System.out.println(Arrays.toString(tkns));
					}
					if (tkns.length != 1)
						throw new TokenException("Could not resolve the value");
					return tkns;
				}
			}
		} else if (stage == 2) {
			for (int i = 0; i < tokens.length; i++) {
				if (tokens[i] instanceof OperatorToken) {
					if (((OperatorToken) tokens[i]).mop.name().matches("^(DIVIDE|MULTIPLY)$")) {
						int vim1 = tokens[i - 1] instanceof NumberToken ? ((NumberToken) tokens[i - 1]).v : ((Variable_INT) variables.get(((IdentifierToken) tokens[i - 1]).identifier)).v, vip1 = tokens[i + 1] instanceof NumberToken ? ((NumberToken) tokens[i + 1]).v : ((Variable_INT) variables.get(((IdentifierToken) tokens[i + 1]).identifier)).v;
						int v = ((OperatorToken) tokens[i]).result(vim1, vip1);
						Token[] tkns = new Token[tokens.length - 2];
						if (i - 1 >= 0) System.arraycopy(tokens, 0, tkns, 0, i - 1);
						tkns[i - 1] = new NumberToken(v);
						if (tokens.length - 2 - i >= 0) System.arraycopy(tokens, i + 2, tkns, i, tokens.length - 2 - i);
						i--;
						tokens = tkns;
					}
				}
			}
		} else if (stage == 3) {
			for (int i = 0; i < tokens.length; i++) {
				if (tokens[i] instanceof OperatorToken) {
					if (((OperatorToken) tokens[i]).mop.name().matches("^(ADD|SUBTRACT)$")) {
						int vim1 = tokens[i - 1] instanceof NumberToken ? ((NumberToken) tokens[i - 1]).v : ((Variable_INT) variables.get(((IdentifierToken) tokens[i - 1]).identifier)).v, vip1 = tokens[i + 1] instanceof NumberToken ? ((NumberToken) tokens[i + 1]).v : ((Variable_INT) variables.get(((IdentifierToken) tokens[i + 1]).identifier)).v;
						int v = ((OperatorToken) tokens[i]).result(vim1, vip1);
						Token[] tkns = new Token[tokens.length - 2];
						if (i - 1 >= 0) System.arraycopy(tokens, 0, tkns, 0, i - 1);
						tkns[i - 1] = new NumberToken(v);
						if (tokens.length - 2 - i >= 0) System.arraycopy(tokens, i + 2, tkns, i, tokens.length - 2 - i);
						i--;
						tokens = tkns;
					}
				}
			}
		} else if (stage == 4) {
			for (int i = 0; i < tokens.length; i++) {
				if (tokens[i] instanceof OperatorToken) {
					if (((OperatorToken) tokens[i]).mop.name().matches("^(LOGIC_E|LOGIC_G|LOGIC_GE|LOGIC_NE|LOGIC_S|LOGIC_SE)$")) {
						int vim1 = tokens[i - 1] instanceof NumberToken ? ((NumberToken) tokens[i - 1]).v : (tokens[i - 1] instanceof LogicConstantValueToken ? (((LogicConstantValueToken) tokens[i - 1]).v ? 1 : 0) : ((Variable_INT) variables.get(((IdentifierToken) tokens[i - 1]).identifier)).v);
						int vip1 = tokens[i + 1] instanceof NumberToken ? ((NumberToken) tokens[i + 1]).v : (tokens[i + 1] instanceof LogicConstantValueToken ? (((LogicConstantValueToken) tokens[i + 1]).v ? 1 : 0) : ((Variable_INT) variables.get(((IdentifierToken) tokens[i + 1]).identifier)).v);
						int v = ((OperatorToken) tokens[i]).result(vim1, vip1);
						Token[] tkns = new Token[tokens.length - 2];
						if (i - 1 >= 0) System.arraycopy(tokens, 0, tkns, 0, i - 1);
						tkns[i - 1] = new NumberToken(v);
						if (tokens.length - 2 - i >= 0) System.arraycopy(tokens, i + 2, tkns, i, tokens.length - 2 - i);
						i--;
						tokens = tkns;
					}
				}
			}
		} else if (stage == 5) {
			for (int i = 0; i < tokens.length; i++) {
				if (tokens[i] instanceof OperatorToken) {
					if (((OperatorToken) tokens[i]).mop.name().matches("^(LOGIC_AND|LOGIC_OR|LOGIC_XOR)$")) {
						int vim1 = tokens[i - 1] instanceof NumberToken ? ((NumberToken) tokens[i - 1]).v : ((Variable_INT) variables.get(((IdentifierToken) tokens[i - 1]).identifier)).v, vip1 = tokens[i + 1] instanceof NumberToken ? ((NumberToken) tokens[i + 1]).v : ((Variable_INT) variables.get(((IdentifierToken) tokens[i + 1]).identifier)).v;
						int v = ((OperatorToken) tokens[i]).result(vim1, vip1);
						Token[] tkns = new Token[tokens.length - 2];
						if (i - 1 >= 0) System.arraycopy(tokens, 0, tkns, 0, i - 1);
						tkns[i - 1] = new NumberToken(v);
						if (tokens.length - 2 - i >= 0) System.arraycopy(tokens, i + 2, tkns, i, tokens.length - 2 - i);
						i--;
						tokens = tkns;
					}
				}
			}
		}
		return tokens;
	}
}