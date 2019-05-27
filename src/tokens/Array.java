package tokens;

import variables.DATA_TYPE;

import java.util.ArrayList;
import java.util.List;

public class Array {
	public static final List<Array> arrays = new ArrayList<>();
	public String name;
	public DATA_TYPE type;

	public Array(String name) {
		this.name = name;
		arrays.add(this);
	}

	public void setType(DATA_TYPE type) {
		this.type = type;
	}
}
