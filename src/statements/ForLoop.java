package statements;

import tokens.IdentifierToken;
import tokens.Token;
import tree.Statements;

public class ForLoop extends Statement {
	public IdentifierToken var;
	public Token[][] forboundtokens;
	public Statements repeat;

	public ForLoop(IdentifierToken var, Token[][] forboundtokens, Statements repeat) {
		super(Statement_TYPE.FOR_LOOP);
		this.var = var;
		this.forboundtokens = forboundtokens;
		this.repeat = repeat;
	}
}
