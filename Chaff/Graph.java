import java.util.*;

public class Graph {
	LinkedList<Clause> clauses = new LinkedList<Clause>();
	LinkedList<Variable> variableList = new LinkedList<Variable>();
	Variable[] variables;
	int firstUnassigned = 1;
	Stack<Edge> edgeStack = new Stack<Edge>();
	int assignedAmount = 0;
	boolean info = false;

	public Graph(int variables, int clauses, boolean info) {
		this.info = info;
		this.variables = new Variable[variables];
		for (int i = 0; i < variables; i++) {
			this.variables[i] = (new Variable(i+1));
			variableList.addLast(this.variables[i]);
		}
		for (int i = 0; i < clauses; i++) {
			this.clauses.addLast(new Clause(i+1));
		}
	}

	//assigns a int value to a variable by index, returns true if successfull or already assigned with same value, else returns false
	public boolean assign(Variable variable, int assignmentInt, int level, Clause ucpClause) {
		//check if variable is already assigned with diverging assignment
		if(variable.getAssignmentInt()*(-1) == assignmentInt) return false;
		else if(variable.getAssignmentInt() == assignmentInt) return true;

		//Assignment assignment = new Assignment(variable, assignmentInt, level, ucpClause);
		variable.assign(assignmentInt, level, ucpClause);
		for(Edge e : variable.getIncidences()) e.getClause().assign(variable);
		assignedAmount++;
		return true;
	}

	public Variable unassign(Variable variable) {
		if(variable.getAssignmentInt() == 0) return variable;
		assignedAmount--;
		if(assignedAmount < 0) System.out.println("ERROR");
		if(info) System.out.println("unassigned " + variable.toString());
		variable.unassign();
		//if(firstUnassigned > variable.getIndex()) firstUnassigned = variable.getIndex();
		return variable;
	}

	//adds an edge to the edgelist and to the incidencelists of the two nodes
	public void addEdge(Edge edge) {
		edge.getClause().getIncidencesList().add(edge);
		edge.getVariable().getIncidences().add(edge);
	}

	//assumes that the argument clause is already UC and not satisfied
	public Edge getUnitEdge(Clause clause) {
		for (Edge e : clause.getIncidences() ) {
			if(e.getVariable().getAssignmentInt() == 0) return e;
		}
		return null;
	}

	public boolean isUnitClause(int index) { 
		return getClause(index).isUnitClause();
	}

	public void printAssignments() { 
		for ( Variable variable : variables ) {
			if(variable.getAssignmentInt() != 0) System.out.println(variable.toString() + " assigned with " + variable.getAssignmentInt() + (variable.getAntecedent() != null ? " via " + variable.getAntecedent().toString() : "") + " at depth " + variable.getLevel());
		}
	}

	public boolean isSAT() {
		loop : for (Clause clause : clauses) {
			for(Edge e : clause.getIncidences()) {
				if(e.getSign() == e.getVariable().getAssignmentInt()) continue loop;
			}
			if(info) System.out.println(clause.toString() + " is unsatisfied");
			return false;
			//if(!clause.isSatisfied()) return false;
		}
		return true;
	}

	//returns a clause node by clause index (starting at 1)
	public Clause getClause(int i) { return clauses.get(i-1); }

	//returns a variable node by variable index (starting at 1)
	public Variable getVariable(int i) { try {return variables[i-1];} catch(Exception e) {System.out.println(i); e.printStackTrace(); System.exit(0);} return null; }

	public LinkedList<Clause> getClauses() { return clauses; }

	public Variable[] getVariables() { return variables; }

	public void addClause(Clause clause) { clauses.addLast(clause); }

	public int getAssignedAmount() { return assignedAmount; }

	public boolean getSatisfied(int index) { return getClause(index).isSatisfied(); }

	public Variable getNextUnassigned() {
		Variable nextUnassigned = null;
		double satisfiedClauses = 0;
		for ( Variable variable : variables ) {
			if(variable.getAssignmentInt() != 0) continue;
			if(variable.getPositiveLiterals() >= satisfiedClauses) { nextUnassigned = variable; satisfiedClauses = variable.getPositiveLiterals(); } 
			else if(variable.getNegativeLiterals() >= satisfiedClauses) { nextUnassigned = variable; satisfiedClauses = variable.getNegativeLiterals(); }
		}
		return nextUnassigned;
	}

	public LinkedList<Variable> getVariableList() { return variableList; }

	public boolean allAssigned() { return assignedAmount == variables.length; }

	public void printSatisfyingVariables() {
		int count = 0;
		for( Clause clause : clauses) {
			for ( Edge e : clause.getIncidences() ) {
				if(e.getSign() == e.getVariable().getAssignmentInt()) {
					System.out.println(e.getVariable().toString() + " satisfies " + clause.toString());
					count++;
					break;
				}
			}
		}
		for(Variable variable : variables) {
			for(Edge e : variable.getIncidences()) {
				if(e.getSign() == e.getVariable().getAssignmentInt()) System.out.println(variable + " satisfies " + e.getClause().toString());
			}
		}
		System.out.println(count);
	}
}