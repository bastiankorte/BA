public class Edge {
	Clause clause;
	Variable variable;
	int sign = 0;
	boolean enabled = true;

	public Edge(Clause clause, Variable variable, int signed) {
		this.clause = clause;
		this.variable = variable;
		sign = signed;
	}

	public void enableEdge() { enabled = true; }

	public void disableEdge() { enabled = false; }

	public boolean isEnabled() { return enabled; }

	public int getSign() { return sign;	}

	public Clause getClause() {	return clause; }

	public Variable getVariable() {	return variable; }

	public boolean equals(Edge edge) {
		return clause.equals(edge.getClause()) 
		&& variable.equals(edge.getVariable()) 
		&& sign == edge.getSign();
	}

	public String toString() { return "Variable " + variable.getIndex() + ", clause " + clause.getIndex() + ", sign " + sign; }
}