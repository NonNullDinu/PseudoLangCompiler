package tokens;

import java.util.Arrays;

public class ArrayIdentifier extends Token {
	public Array array;
	public Token[] indexTokens;

	public ArrayIdentifier(Array array, Token[] indexTokens) {
		this.array = array;
		this.indexTokens = indexTokens;
	}

	@Override
	public String toString() {
		return "ARRAY_IDENTIFIER_T(" + array.name + ", [" + Arrays.deepToString(indexTokens) + "])";
	}
}
