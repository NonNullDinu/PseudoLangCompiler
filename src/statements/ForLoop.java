package statements;

import tokens.IdentifierToken;
import tokens.Token;
import tree.Statements;

import java.util.Arrays;

public class ForLoop extends Statement {
	public IdentifierToken var;
	public Token[][] forboundtokens;
	public Statements repeat;

	public ForLoop(IdentifierToken var, Token[][] forboundtokens, Statements repeat) {
		super(Statement_TYPE.FOR_LOOP);
		System.out.println(Arrays.deepToString(forboundtokens[0]));
		System.out.println(Arrays.deepToString(forboundtokens[1]));
		System.out.println(Arrays.deepToString(forboundtokens[2]));
		this.var = var;
		this.forboundtokens = forboundtokens;
		this.repeat = repeat;
	}
}
