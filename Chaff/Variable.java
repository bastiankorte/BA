import java.util.*;

public class Variable {
	Clause antecedent;
	int level, assignmentInt, ucplevel, index;
	double positiveLiterals, negativeLiterals;
	LinkedList<Edge> incidences= new LinkedList<Edge>();
	
	public Variable(int index) {
		this.index = index;
		level = -1;
		assignmentInt = 0;
		positiveLiterals = 0;
		negativeLiterals = 0;
		ucplevel = -1;
		antecedent = null;
	}

	public void divideLiteralCounters(double divisor) {
		positiveLiterals = positiveLiterals / divisor;
		negativeLiterals = negativeLiterals / divisor;
	}

	public void setLiteralCounters() {
		int count = 0;
		for (Edge e : incidences) {
			if(e.getSign() == 1) count++;
		}
		positiveLiterals = count;
		count = 0;
		for (Edge e : incidences) {
			if(e.getSign() == -1) count++;
		}
		negativeLiterals = count;
	}

	public void addToLiteralCounters(int added) {
		if(added == -1) negativeLiterals = negativeLiterals + 1;
		else positiveLiterals = positiveLiterals + 1;
	}

	public double getPositiveLiterals() { return positiveLiterals;	}

	public double getNegativeLiterals() { return negativeLiterals; }

	//returns 0 if not pure, 1 if all signs are 1, -1 if all signs are -1
	public int isPure() {
		/*if(positiveLiterals == 0) return -1;
		if(negativeLiterals == 0) return 1;
		return 0;*/
		int sign = 0;
		for (Edge e : incidences) {
			if(e.getSign() != e.getVariable().getAssignmentInt()) {sign = e.getSign(); break;}
		}
		for (Edge e : incidences ) {
			if(e.getSign() != e.getVariable().getAssignmentInt() && e.getSign() != sign) return 0;
		}
		return sign;
	}

	public boolean assign(int setAssignmentInt, int setLevel, Clause setAntecedent) { 
		if(setAssignmentInt*(-1) == assignmentInt) return false; 
		assignmentInt = setAssignmentInt; 
		level = setLevel;
		antecedent = setAntecedent; 
		if(antecedent != null) ucplevel = antecedent.getMaxUCPLevel() + 1;
		else ucplevel = 0;
		return true;
	}

	public int getLowestAntecedentLevel() {
		int level = Integer.MAX_VALUE;
		for (Edge edge : antecedent.getIncidences()) {
			if(edge.getVariable().getIndex() == index) continue;
			if(edge.getVariable().getAntecedent() == null && edge.getVariable().getLevel() < level) level = edge.getVariable().getLevel();
			else if (edge.getVariable().getAntecedent() != null) {
				int temp = edge.getVariable().getLowestAntecedentLevel();
				if(temp < level) level = temp;
			}
		}
		return level;
	}

	public int getIndex() { return index; }

	public LinkedList<Edge> getIncidences() { return incidences; }

	public void unassign() { assignmentInt = 0; level = -1; antecedent = null; ucplevel = -1; }

	public int getAssignmentInt() { return assignmentInt; }

	public int getLevel() { return level; }

	public int getUCPLevel() { return ucplevel; }

	public Clause getAntecedent() { return antecedent; }

	public boolean equals(Variable variable) { return index == variable.getIndex(); }

	public String toString() { return "(x" + index + " = " + assignmentInt + "@" + level + "^" + ucplevel + ")"; }
}