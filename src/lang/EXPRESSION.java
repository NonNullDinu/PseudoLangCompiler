package lang;

import java.util.List;

public class EXPRESSION {
	private final String assembly;
	private String name;
	private List<EXPRESSION_PARAMETER> parameters;

	public EXPRESSION(String name, List<EXPRESSION_PARAMETER> parameters, String assemblyCodeForEvaluation) {
		this.name = name;
		this.parameters = parameters;
		this.assembly = assemblyCodeForEvaluation;
	}
}
