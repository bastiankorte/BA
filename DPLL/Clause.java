import java.util.*;

public class Clause {
	int assignedVariables, satisfiedVariables, index, firstWatch, secondWatch, creationTime;
	LinkedList<Edge> incidencesList= new LinkedList<Edge>();
	Edge[] incidences;
	
	public Clause(int index) {
		this.index = index;
		satisfiedVariables = 0;
		assignedVariables = 0;
	}

	public Clause(int index, int creationTime) {
		this.index = index;
		satisfiedVariables = 0;
		assignedVariables = 0;
		this.creationTime = creationTime;
	}

	public int getCreationTime() { return creationTime; }

	public void setWatches() {
		if(incidences.length != 0) {
			firstWatch = 0;
			secondWatch = incidences.length - 1;
		}
		if(incidences[firstWatch].getVariable().getAssignmentInt() * incidences[firstWatch].getSign() == -1) {
			for(int i = firstWatch + 1; i < incidences.length; i++) {
				if(incidences[i].getVariable().getAssignmentInt() * incidences[i].getSign() == -1 || i == secondWatch) continue;
				firstWatch = i;
				break;
			}
		}
		if(incidences[secondWatch].getVariable().getAssignmentInt() * incidences[secondWatch].getSign() == -1) {
			for(int i = secondWatch - 1; i > -1; i--) {
				if(incidences[i].getVariable().getAssignmentInt() * incidences[i].getSign() == -1 || i == firstWatch) continue;
				secondWatch = i;
				break;
			}
		}
	}

	public void setLearntClauseWatches() {
		//setWatches();
		if(incidences.length != 0) {
			firstWatch = 0;
			secondWatch = incidences.length - 1;
		}

		for(int i = incidences.length-1; i > 0; i--) {
			if(incidences[i].getVariable().getLevel() > incidences[secondWatch].getVariable().getLevel()) secondWatch = i;
		}
		/*if(incidences[firstWatch].getVariable().getAssignmentInt()*incidences[firstWatch].getSign() == -1) {
			int depth = getMaxDepth();
			for(int i = 0; i < incidences.length; i++) {
				if(incidences[i].getVariable().getLevel() == depth && i != secondWatch) { firstWatch = i; break; } 
			}
		}
		if(incidences[secondWatch].getVariable().getAssignmentInt()*incidences[secondWatch].getSign() == -1) {
			int depth = getMaxDepth();
			for(int i = 0; i < incidences.length; i++) {
				if(incidences[i].getVariable().getLevel() == depth && i != firstWatch) { secondWatch = i; break; } 
			}
		}*/
	}

	public Edge getFirstWatch() { return incidences[firstWatch]; }

	public Edge getSecondWatch() { return incidences[secondWatch]; }

	public void assign(Variable variable) {
		if(!incidences[firstWatch].getVariable().equals(variable) && !incidences[secondWatch].getVariable().equals(variable)) return;
		if(incidences[firstWatch].getVariable().equals(variable) && variable.getAssignmentInt() == incidences[firstWatch].getSign()) return;
		if(incidences[secondWatch].getVariable().equals(variable) && variable.getAssignmentInt() == incidences[secondWatch].getSign()) return;
		if(incidences[firstWatch].getVariable().equals(variable)) {
			for(int i = firstWatch + 1; i < incidences.length; i++) {
				// assignment * sign == -1 => literal is 0, if 0 is u and if 1 is 1
				if(i == secondWatch || incidences[i].getVariable().getAssignmentInt() * incidences[i].getSign() == -1) continue;
				firstWatch = i;
				return;
			}
			for(int i = firstWatch - 1; i > -1; i--) {
				if(i == secondWatch || incidences[i].getVariable().getAssignmentInt() * incidences[i].getSign() == -1) continue;
				firstWatch = i;
				return;
			}
		}
		if(incidences[secondWatch].getVariable().equals(variable)) {
			for(int i = secondWatch - 1; i > -1; i--) {
				if(i == firstWatch || incidences[i].getVariable().getAssignmentInt() * incidences[i].getSign() == -1) continue;
				secondWatch = i;
				return;
			}
			for(int i = secondWatch + 1; i < incidences.length; i++) {
				if(i == firstWatch || incidences[i].getVariable().getAssignmentInt() * incidences[i].getSign() == -1) continue;
				secondWatch = i;
				return;
			}
		}
	}

	public boolean isUnsat() { 
		//System.out.println("clause " + index + " unsat " + incidences[firstWatch].getVariable().toString() + ", " + incidences[secondWatch].getVariable().toString());
		return incidences[firstWatch].getVariable().getAssignmentInt() * incidences[firstWatch].getSign() == -1 
		&& incidences[secondWatch].getVariable().getAssignmentInt() * incidences[secondWatch].getSign() == -1;
		//return assignedVariables == incidences.length && satisfiedVariables == 0;	
	}

	public boolean isUnsatDPLL() {
		return satisfiedVariables == 0 && assignedVariables == incidences.length;
		/*for ( Edge e : incidences ) {
			if(e.getVariable().getAssignmentInt() * e.getSign() != -1) return false; 
		}
		return true;*/
	}

	public LinkedList<Variable> getAssignmentInts() { 
		LinkedList<Variable> assignments = new LinkedList<Variable>();
		for(Edge e : incidences) {
			if(e.getVariable().getAssignmentInt() != 0) assignments.add(e.getVariable());
		}
		return assignments; 
	}	

	public void setAssignmentCounters() {
		for(Edge e : incidences) {
			if(e.getVariable().getAssignmentInt() == 0) continue;
			if(e.getVariable().getAssignmentInt() == e.getSign()) satisfiedVariables++;
			assignedVariables++;
		}
	}

	public boolean setAssignedVariables(int value) { 
		if(value > incidences.length) {
			System.out.println("assigned variables value too large");
			return false;
		}
		assignedVariables = value; 
		return true;
	}

	public boolean increaseAssignedVariables() { 
		if(assignedVariables == incidences.length) { 
			System.out.println("shouldn't have happened, too many assignments at clause " + index); 
			return false; 
		} 
		assignedVariables++; 
		return true; 
	}

	public boolean decreaseAssignedVariables() { 
		if(assignedVariables == 0) { 
			System.out.println("shouldn't have happened, too few assignments at clause " + index); 
			return false; 
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
		return incidences[firstWatch].getVariable().getAssignmentInt() == incidences[firstWatch].getSign() 
		|| incidences[secondWatch].getVariable().getAssignmentInt() == incidences[secondWatch].getSign();
		//return satisfiedVariables != 0; 
	}

	public boolean isUnitClauseGRASP() { 
		//if(index == 107) System.out.println(toString() + " is unit " + (satisfiedVariables == 0) + ", " +  (incidences.length - assignedVariables == 1));
		return satisfiedVariables == 0 && incidences.length - assignedVariables == 1; 
	}

	public boolean isUnitClause() {
		if(!isSatisfied() && !isUnsat() && incidences.length == 1) return true;
		if(!isSatisfied() && !isUnsat() && (incidences[firstWatch].getVariable().getAssignmentInt() != 0 || incidences[secondWatch].getVariable().getAssignmentInt() != 0 )) return true;
		return false;
	}

	public Edge getUnitEdgeGRASP() {
		for(int i = 0; i < incidences.length; i++) if(incidences[i].getVariable().getAssignmentInt() == 0) return incidences[i];
		return null;
	}

	public Edge getUnitEdge() {
		if(incidences[firstWatch].getVariable().getAssignmentInt() == 0) return incidences[firstWatch];
		return incidences[secondWatch];
	}

	public void fillIncidences() {
		incidences = new Edge[incidencesList.size()];
		for(int i = 0; i < incidences.length; i++) incidences[i] = incidencesList.get(i);
		incidencesList = null;
	}

	public int getIndex() { return index; }

	public Edge[] getIncidences() { return incidences; }

	public LinkedList<Edge> getIncidencesList() { return incidencesList; }

	public boolean equals(Clause clause) { return index == clause.getIndex(); }

	public boolean setSatisfiedVariables(int value) { 
		if(value > incidences.length) {
			System.out.println("satisfied variables value too large");
			return false;
		}
		satisfiedVariables = value; 
		return true;
	}

	public void increaseSatisfiedVariables() { satisfiedVariables++; }

	public void decreaseSatisfiedVariables() { satisfiedVariables--; }

	public int getSatisfiedVariables() { return satisfiedVariables; }

	public String toString() { 
		String string = "";
		for (int i = 0; i < incidences.length - 1; i++) {
			string += (incidences[i].getSign() == 1 ? "" : "-") + incidences[i].getVariable().toString() + ", ";
		}
		string += (incidences[incidences.length - 1].getSign() == 1 ? "" : "-") + incidences[incidences.length - 1].getVariable().toString();
		return "Clause " + index + " " + string; 
	}
}