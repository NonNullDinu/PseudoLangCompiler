package tokens;

public class FILE_FULL_TOKEN extends Token {
	public String var_name;

	public FILE_FULL_TOKEN(String var_name) {
		this.var_name = var_name;
	}

	@Override
	public String toString() {
		return "FILE_TOKEN(\"file " + var_name + "\")";
	}
}
