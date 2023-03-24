import java.util.*;

public class Clause extends Node {
	int assignedVariables, satisfiedVariables;
	Edge firstWatch, secondWatch;
	
	public Clause(int index) {
		super(index);
		clause = true;
		satisfiedVariables = 0;
		assignedVariables = 0;
	}

	public void setWatches() {
		if(incidences.size() != 0) {
			firstWatch = incidences.getFirst();
			secondWatch = incidences.getLast();
		}
		if(firstWatch.getVariable().getAssignmentInt() != 0) {
			for(int i = 1; i < incidences.size(); i++) {
				if(incidences.get(i).getVariable().getAssignmentInt() != 0) continue;
				firstWatch = incidences.get(i);
				break;
			}
		}
		if(secondWatch.getVariable().getAssignmentInt() != 0) {
			for(int i = 1; i < incidences.size(); i++) {
				if(incidences.get(i).getVariable().getAssignmentInt() != 0) continue;
				secondWatch = incidences.get(i);
				break;
			}
		}
	}

	public boolean isUnsat() { 
		//System.out.println("clause " + index + " unsat " + firstWatch.toString() + ", " + secondWatch.toString());
		return firstWatch.getVariable().getAssignmentInt() * firstWatch.getSign() == -1 && secondWatch.getVariable().getAssignmentInt() * secondWatch.getSign() == -1;
		//return assignedVariables == incidences.size() && satisfiedVariables == 0;	
	}

	/*public LinkedList<Assignment> getAssignments() { 
		LinkedList<Assignment> assignments = new LinkedList<Assignment>();
		for(Edge e : incidences) {
			if(e.getVariable().getAssignment() != null) assignments.add(e.getVariable().getAssignment());
		}
		return assignments; 
	}*/

	public LinkedList<Variable> getAssignmentInts() { 
		LinkedList<Variable> assignments = new LinkedList<Variable>();
		for(Edge e : incidences) {
			if(e.getVariable().getAssignmentInt() != 0) assignments.add(e.getVariable());
		}
		return assignments; 
	}	

	public boolean increaseAssignedVariables() { 
		if(assignedVariables == incidences.size()) { 
			System.out.println("shouldn't have happened, too many assignments at clause " + index); return false; 
		} 
		assignedVariables++; 
		return true; 
	}

	public boolean decreaseAssignedVariables() { 
		if(assignedVariables == 0) { 
			System.out.println("shouldn't have happened, too few assignments at clause " + index); return false; 
		} 
		assignedVariables--; 
		return true; 
	}

	public int getMaxDepth() { 
		int depth = 0;
		for (Edge e : incidences ) {
			if(e.getVariable().getLevel() > depth) depth = e.getVariable().getLevel();
		}
		return depth;
	}

	public int getMaxUCPLevel() {
		int ucpLevel = 0;
		int depth = getMaxDepth();
		for (Edge e : incidences ) {
			if(e.getVariable().getUCPLevel() > ucpLevel && e.getVariable().getLevel() == depth) ucpLevel = e.getVariable().getUCPLevel();
		}
		return ucpLevel;
	}

	public int getAssignedVariables() { return assignedVariables; }

	public boolean isSatisfied() { 
		return firstWatch.getVariable().getAssignmentInt() == firstWatch.getSign() || secondWatch.getVariable().getAssignmentInt() == secondWatch.getSign();
		//return satisfiedVariables != 0; 
	}

	public boolean isUnitClause() {
		if((firstWatch.getVariable().getAssignmentInt() == 0 && secondWatch.getVariable().getAssignmentInt() == 0) || isSatisfied()) return false;
		if(firstWatch.getVariable().getAssignmentInt() != 0){
			for(Edge e : incidences) {
				if(e.getSign() == e.getVariable().getAssignmentInt()) {}
				else if(e.getVariable().getAssignmentInt() != 0 || e.equals(secondWatch)) continue;
				firstWatch = e;
				return secondWatch.getVariable().getAssignmentInt() != 0;
			}
			return secondWatch.getVariable().getAssignmentInt() == 0;
		}
		else {
			for(Edge e : incidences) {
				if(e.getSign() == e.getVariable().getAssignmentInt()) {}
				else if(e.getVariable().getAssignmentInt() != 0 || e.equals(firstWatch)) continue;
				secondWatch = e;
				return firstWatch.getVariable().getAssignmentInt() != 0;
			}
			return firstWatch.getVariable().getAssignmentInt() == 0;
		}
	}

	public LinkedList<Variable> getVariables() {
		LinkedList<Variable> variableList = new LinkedList<Variable>();
		for (Edge e : incidences) {
			variableList.add(e.getVariable());
		}
		return variableList;
	}

	public boolean isClause() { return true; }

	public boolean equals(Clause clause) { return index == clause.getIndex(); }

	public boolean equals(Variable variable) { return false; }

	public void increaseSatisfiedVariables() { satisfiedVariables++; }

	public void decreaseSatisfiedVariables() { satisfiedVariables--; }

	public int getSatisfiedVariables() { return satisfiedVariables; }

	public String toString() { 
		String string = "";
		for (int i = 0; i < incidences.size() - 1; i++) {
			string += (incidences.get(i).getSign() == 1 ? "" : "-") + incidences.get(i).getVariable().toString() + ", ";
		}
		string += (incidences.getLast().getSign() == 1 ? "" : "-") + incidences.getLast().getVariable().toString();
		return "Clause " + index + " " + string; 
	}
}