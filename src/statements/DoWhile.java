package statements;

import tokens.Token;
import tree.Statements;

import java.util.Arrays;

public class DoWhile extends Statement {
	public Statements repeat;
	public Token[] condtokens;

	public DoWhile(Statements repeat, Token[] condtkns) {
		super(Statement_TYPE.DO_WHILE);
		this.repeat = repeat;
		this.condtokens = condtkns;
		System.out.println(Arrays.deepToString(this.condtokens));
	}
}
