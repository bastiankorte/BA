public class Variable extends Node {
	//Assignment assignment;
	Clause antecedent;
	int level, assignmentInt, positiveLiterals, negativeLiterals, ucplevel;
	
	public Variable(int index) {
		super(index);
		level = -1;
		assignmentInt = 0;
		positiveLiterals = 0;
		negativeLiterals = 0;
		ucplevel = -1;
		antecedent = null;
	}

	public boolean isContradiction() {
		for (int i = 0; i < incidences.size(); i++) {
			if(!incidences.get(i).getClause().isUnitClause() && incidences.get(i).getClause().getIncidences().size() > 1) continue;
			for (int j = i + 1; j < incidences.size(); j++) {
				if(!incidences.get(j).getClause().isUnitClause() && incidences.get(j).getClause().getIncidences().size() > 1) continue;
				if(incidences.get(i).getSign() != incidences.get(j).getSign()) return true;
			}
		}
		return false;
	}

	public void divideLiteralCounters() {
		positiveLiterals = positiveLiterals / 2;
		negativeLiterals = negativeLiterals / 2;
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
		if(added == -1) negativeLiterals++;
		else positiveLiterals++;
	}

	public int getPositiveLiterals() { return positiveLiterals;	}

	public int getNegativeLiterals() { return negativeLiterals; }

	//returns 0 if not pure, 1 if all signs are 1, -1 if all signs are -1
	public int isPure() {
		if(positiveLiterals == 0) return -1;
		if(negativeLiterals == 0) return 1;
		return 0;
		/*for (Edge e : incidences) {
			if(e.isEnabled()) {sign = e.getSign(); break;}
		}
		for (Edge e : incidences ) {
			if(e.isEnabled() && e.getSign() != sign) return 0;
		}*/
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

	public void unassign() { assignmentInt = 0; level = -1; antecedent = null; ucplevel = -1; }

	public int getAssignmentInt() { return assignmentInt; }

	public int getLevel() { return level; }

	public int getUCPLevel() { return ucplevel; }

	public Clause getAntecedent() { return antecedent; }

	//public void setAssignment(Assignment setAssignment) { assignment = setAssignment; }

	//public Assignment getAssignment() { return assignment; }

	public boolean equals(Variable variable) { return index == variable.getIndex(); }

	public boolean equals(Clause clause) { return false; }

	public boolean isClause() { return false; }

	//public String toString() { return "Variable " + index; }

	public String toString() { return "(x" + index + " = " + assignmentInt + "@" + level + "^" + ucplevel + ")"; }
}