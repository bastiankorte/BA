import java.util.*;

public class Graph {
	LinkedList<Clause> clauses = new LinkedList<Clause>();
	Variable[] variables;
	//LinkedList<Edge> edges = new LinkedList<Edge>();
	int firstUnassigned = 1;
	Stack<Edge> edgeStack = new Stack<Edge>();
	int assignedAmount = 0;
	boolean info = false;

	public Graph(int variables, int clauses, boolean info) {
		this.info = info;
		this.variables = new Variable[variables];
		for (int i = 0; i < variables; i++) {
			this.variables[i] = (new Variable(i+1));
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
		assignedAmount++;
		return true;

		//set firstUnassigned counter on next unassigned variable
		/*if(variable.getIndex() == firstUnassigned && variable.getIndex() < variables.length){
			for( int i = ++firstUnassigned; i <= variables.length; i++ ) {
				if(variables[i-1].getAssignmentInt() == 0) {
					firstUnassigned = i;
					break;
				}
			}
		}*/

		//build assignment tree if assignment was by unit clause
		/*if(ucpClause != null) {
			for( Edge e : ucpClause.getIncidences() ) {
				if(e.isEnabled() && !e.getVariable().equals(variable)) {
					AssignmentEdge assignmentEdge = new AssignmentEdge(e.getVariable().getAssignment(), ucpClause, assignment);
					e.getVariable().getAssignment().addDerivatives(assignmentEdge);
					assignment.addDerivedFrom(assignmentEdge);
					//System.out.println(assignmentEdge.toString());
				}
			}
		}*/

		//add assignment to all clauses with occurance of assigned variable and disable edges to clauses with same assignment
		/*for( Edge e : variable.getIncidences() ) {
			if(e.getSign() == assignmentInt) {
				e.disableEdge();
				e.getClause().increaseSatisfiedVariables();
			}
			e.getClause().increaseAssignedVariables();
		}*/
	}

	/*public void unassignUCPs(Variable variable) {
		if(variable.getAssignment() == null) return;
		for ( Edge e : variable.getIncidences() ) {
			if(!e.isEnabled()) { 
				e.getClause().decreaseSatisfiedVariables();
		 		e.enableEdge();
		 	}
		 	//if(variable.getIndex() == 14) System.out.println("decreased assigned variables in ucp");
		 	if(!e.getClause().decreaseAssignedVariables()) { System.out.println("too few assigned variables at unassigning " + variable.getIndex()); System.exit(0); }
		}
		if(!variable.getAssignment().isUCP()) {System.out.println(variable.getAssignment().toString()); System.exit(0);}
 		assignedAmount--;
		variable.setAssignment(null);
		if(firstUnassigned > variable.getIndex()) firstUnassigned = variable.getIndex();
		if(info) System.out.println("unassigned " + variable.getIndex());
	}

	public void unassignAllImplications(Variable variable) {
		//if(info) System.out.println("unassigning " + variable.getIndex() + "' implications");
		if(variable.getAssignment() == null) return;
		for ( AssignmentEdge edge : variable.getAssignment().getDerivatives() ) {
			unassignAllImplications(edge.getConclusion().getVariable());
			unassignUCPs(edge.getConclusion().getVariable());
		}
	}*/

	public void unassign(Variable variable) {
		if(variable.getAssignmentInt() == 0) return;
		/*for ( Edge e : variable.getIncidences() ) {
			if(!e.isEnabled()) { 
				e.getClause().decreaseSatisfiedVariables();
		 		e.enableEdge();
		 	}
		 	if(!e.getClause().decreaseAssignedVariables()) { System.out.println("too few assigned variables at unassigning " + variable.getIndex()); /*System.exit(0); }
		}*/
		assignedAmount--;
		if(assignedAmount < 0) System.out.println("ERROR");
		if(info) System.out.println("unassigned " + variable.toString());
		variable.unassign();
		if(firstUnassigned > variable.getIndex()) firstUnassigned = variable.getIndex();
	}

	//adds an edge to the edgelist and to the incidencelists of the two nodes
	public void addEdge(Edge edge) {
		//edges.add(edge);
		edge.getClause().getIncidences().add(edge);
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
		/*if(getSatisfied(index)) return false;
		int count = 0;
		for (Edge e : getClause(index).getIncidences()) {
			if(getAssignment(e.getVariable().getIndex()) == null) count++;
		}
		return count == 1;*/
	}

	public void printAssignments() { 
		for ( Variable variable : variables ) {
			if(variable.getAssignmentInt() != 0) System.out.println(variable.toString() + " assigned with " + variable.getAssignmentInt() + (variable.getAntecedent() != null ? " via clause " + variable.getAntecedent().toString() : "") + " at depth " + variable.getLevel());
		}
	}

	public boolean isSAT() {
		loop : for (Clause clause : clauses) {
			for(Edge e : clause.getIncidences()) {
				if(e.getSign() == e.getVariable().getAssignmentInt()) continue loop;
			}
			return false;
			//if(!clause.isSatisfied()) return false;
		}
		return true;
	}

	//returns a clause node by clause index (starting at 1)
	public Clause getClause(int i) { return clauses.get(i-1); }

	//returns a variable node by variable index (starting at 1)
	public Variable getVariable(int i) { return variables[i-1]; }

	public LinkedList<Clause> getClauses() { return clauses; }

	public Variable[] getVariables() { return variables; }

	//public LinkedList<Edge> getEdges() { return edges; }

	public void addClause(Clause clause) { clauses.addLast(clause); }

	public int getAssignedAmount() { return assignedAmount; }

	public boolean getSatisfied(int index) { return getClause(index).isSatisfied(); }

	public Variable getNextUnassigned() {
		Variable nextUnassigned = null;
		int satisfiedClauses = 0;
		for ( Variable variable : variables ) {
			if(variable.getAssignmentInt() != 0) continue;
			if(variable.getPositiveLiterals() >= satisfiedClauses) { nextUnassigned = variable; satisfiedClauses = variable.getPositiveLiterals(); } 
			else if(variable.getNegativeLiterals() >= satisfiedClauses) { nextUnassigned = variable; satisfiedClauses = variable.getNegativeLiterals(); }
		}
		return nextUnassigned;
	}

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

	/*public String edgesToString() {
		String edgeString = "";
		for (Edge e : edges) {
			edgeString += e.toString();
			edgeString += "\n";
		}
		return edgeString;
	}*/
}