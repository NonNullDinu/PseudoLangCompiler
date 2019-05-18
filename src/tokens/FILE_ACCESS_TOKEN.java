package tokens;

public class FILE_ACCESS_TOKEN extends Token {
	public FILE_ACCESS access;

	public FILE_ACCESS_TOKEN(FILE_ACCESS access) {
		this.access = access;
	}

	@Override
	public String toString() {
		return "FILE_ACCESS_T(" + access.name() + ")";
	}
}
