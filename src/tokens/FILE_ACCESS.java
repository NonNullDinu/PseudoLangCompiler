package tokens;

public enum FILE_ACCESS {
	READ_ONLY("_f_ro_open", "read only", "ro"), WRITE_ONLY("_f_wo_open", "write only", "wo");
	public static FILE_ACCESS last = null;
	public final String[] type_declarators;
	private String func_open;

	FILE_ACCESS(String func_open, String... type_declarators) {
		this.type_declarators = type_declarators;
		this.func_open = func_open;
	}

	public static FILE_ACCESS access(String value) {
		for (FILE_ACCESS fa : values())
			for (String td : fa.type_declarators)
				if (value.equals(td)) {
					last = fa;
					return fa;
				}
		last = null;
		return null;
	}

	public String func_open() {
		return func_open;
	}
}
