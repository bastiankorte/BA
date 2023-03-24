import java.io.*;
import java.util.*;

public class GRASP {
	static Graph graph;
	static int variables, clauses, depth, restartTicker;
	static boolean info = false;
	static boolean chaff = false;
	static Stack<Variable> variableStack = new Stack<Variable>();
	static Variable[] randomArray;
	static Random random;
	static long start, stop;

	public static void main(String[] args) {
		try {
			Scanner scanner = new Scanner(new File(args[0]));

			if(args.length == 1 && args[0].equals("-help")) { System.out.println("java SAT FILE BACKTRACKINGSCHEME [-info]"); System.exit(0); }
			//ignores the first two words
			scanner.next(); 
			scanner.next(); 
			//third and fourth word are the amount of variables and clauses
			variables = Integer.parseInt(scanner.next()); 
			clauses   = Integer.parseInt(scanner.next());
			random = new Random();
			randomArray = new Variable[variables + 1];

			depth = 1;

			graph = new Graph(variables, clauses, info);

			if(info) System.out.println("creating graph with " + variables + " variables and " + clauses + " clauses");

			//reads every line until "0" and adds edges between the read variables and the clause determined by the current line, if clause is unit clause it adds an edge to the start node
			for (int i = 1; i <= clauses; i++) {
				Clause temp = graph.getClause(i);
				String next = scanner.next();
				while (!next.equals("0")) {
					if (next.contains("-")) graph.addEdge( new Edge( temp, graph.getVariable(Integer.parseInt(next.substring(1))), -1));
					else graph.addEdge( new Edge( temp, graph.getVariable(Integer.parseInt(next)), 1));
					next = scanner.next();
				}
				temp.setWatches();
			}

			for ( Variable variable : graph.getVariables() ) {
				variable.setLiteralCounters();
			}

			if(info) System.out.println("graph created");
			start = System.currentTimeMillis();
			//for(int i = 1; i <= variables; i++) System.out.println(randomArray[i].toString());
			if(grasp()) {
				stop = System.currentTimeMillis();
				if(!graph.isSAT()) System.out.println("Fehler");
				System.out.println(args[0] + " is satisfiable in " + (stop - start) + " ms");
				//System.out.println(args[0].substring(17,20) + " " + (stop - start));
				if(info) graph.printAssignments();
				//if(info) graph.printSatisfyingVariables();
				/*label: for (Clause clause : graph.getClauses()) {
					for (Edge e : clause.getIncidences() ) {
						if(e.getSign() == e.getVariable().getAssignment().getAssignment()) {
							System.out.println("for clause " + clause.getIndex() + " variable " + e.getVariable().getIndex() + " fits " + e.getSign() + " with assignment " + e.getVariable().getAssignment().getAssignment());
							continue label;
						}
					}
				}*/
			} else {
				stop = System.currentTimeMillis();
				if(graph.isSAT()) System.out.println("Fehler");
				System.out.println(args[0] + " is unsatisfiable in " + (stop - start) + " ms");
				//System.out.println(args[0].substring(17,20) + " " + (stop - start));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean grasp() {
		LinkedList<Clause> queue = new LinkedList<Clause>();
		for (Clause clause : graph.getClauses()) {
			if(clause.isUnitClause()) {
				queue.add(clause);
				if(info) System.out.println("added clause " + clause.getIndex() + " to queue on first ucp detection");
				continue;
			}
		}
		if(unitClausePropagation(queue) != null) return false;

		int depth = 0;

		//purityTest(depth);

		Variable nextUnassigned = null;

		restartTicker = 0;
		int restartCount = 0;
		long start = System.currentTimeMillis();
		while(!graph.allAssigned()) {
			if( restartTicker> (restartCount+1)*clauses*0.55) {
				if(info) System.out.println("restarted");
				while(!variableStack.empty()) graph.unassign(variableStack.pop());
				queue.clear();
				restartTicker = 0;
				start = System.currentTimeMillis();
				restartCount++;
				depth = 0;
				continue;
			}
			if(queue.size() > 0) {
				Clause conflictClause = unitClausePropagation(queue);
				if(conflictClause == null) continue;
				Variable flipVariable = learnGraspClauses(conflictClause);
				if(flipVariable == null) return false;
				while(!flipVariable.equals(variableStack.peek())) {
					graph.unassign(variableStack.pop());
					restartTicker++;
				}
				variableStack.pop();
				depth = flipVariable.getLevel();
				int flipValue = flipVariable.getAssignmentInt();
				if(info) System.out.println("flipping " + flipVariable.toString() + " to -1 via UCP " + graph.getClause(clauses).toString());
				graph.unassign(flipVariable);
				graph.assign(flipVariable, flipValue*(-1), depth, graph.getClause(clauses));
				variableStack.push(flipVariable);
				restartTicker++;

				for(Edge e : flipVariable.getIncidences()) {
					if(e.getClause().isUnitClause()) {
						queue.add(e.getClause());
						if(info) System.out.println("added clause " + e.getClause().getIndex() + " to queue as ucp");
					}
				}
			} else {
				nextUnassigned = graph.getNextUnassigned();
				depth++;
				restartTicker++;
				if(nextUnassigned.getPositiveLiterals() > nextUnassigned.getNegativeLiterals()) graph.assign(nextUnassigned, 1, depth, null);
				else graph.assign(nextUnassigned, -1, depth, null);

				if(info) System.out.println("assigning " + nextUnassigned.toString() + " with " + 1 + " at depth " + depth + ", " + (variables - graph.getAssignedAmount() + " still unassigned"));
				variableStack.push(nextUnassigned);

				for(Edge e : nextUnassigned.getIncidences()) {
					if(graph.isUnitClause(e.getClause().getIndex())) {
						queue.add(e.getClause());
						if(info) System.out.println("added clause " + e.getClause().getIndex() + " to queue as ucp");
					}
				}
			}
		}
		return true;
	}

	//returns the a conflict clause, or null if no conflict is found
	public static Clause unitClausePropagation(LinkedList<Clause> queue) {
		Clause conflictClause = null;
		while (queue.size() > 0) {
			if(queue.getFirst().isUnitClause()) {
				Edge unitEdge = graph.getUnitEdge(queue.getFirst());
				restartTicker++;
				int localDepth = queue.getFirst().getMaxDepth();

				if (!graph.assign(unitEdge.getVariable(), unitEdge.getSign(), localDepth, queue.getFirst()))  { System.out.println("error detected"); return conflictClause; }

				if(!variableStack.empty() && variableStack.peek().getLevel() > unitEdge.getVariable().getLevel()) System.out.println("error");

				if(info) System.out.println("assigning " + unitEdge.getVariable() + " with " + unitEdge.getSign() + " via UCP-clause " + queue.getFirst().toString() + " at depth " + localDepth + " and UCP-Depth " + queue.getFirst().getMaxUCPLevel() + ", " + (variables - graph.getAssignedAmount() + " still unassigned"));
				if(localDepth != 0) variableStack.push(unitEdge.getVariable());
				for(Edge e : unitEdge.getVariable().getIncidences()) {
					if(e.getClause().isUnitClause()) {
				    	queue.addLast(e.getClause());
				    	if(info) System.out.println("added clause " + e.getClause().getIndex() + " to queue" );
					}
				}
				for (Edge e : unitEdge.getVariable().getIncidences()) {
					if(!e.equals(unitEdge) && e.getClause().isUnsat()) {
						if(info) System.out.println("conflict in " + e.getClause().toString());
						queue.clear();
						return e.getClause();
					}
				}
				//queue.addLast(unitEdge.getVariable());

			}
			queue.removeFirst();
		}	
		return conflictClause;
	}

	

	public static Variable learnGraspClauses(Clause conflictClause) {
		LinkedList<Variable> variableList = new LinkedList<Variable>();
		int clauseLevel;
		loop : while(true) {
			clauseLevel = conflictClause.getMaxDepth();
			for ( Edge e : conflictClause.getIncidences() ) {
				variableList.add(e.getVariable());
			}
			int pointer = 0;
			while(pointer < variableList.size()) {
				if(variableList.get(pointer).getLevel() < clauseLevel || variableList.get(pointer).getAntecedent() == null) { pointer++; continue; }
				for(Edge e : variableList.get(pointer).getAntecedent().getIncidences()) if(!variableList.contains(e.getVariable())) variableList.add(e.getVariable());
				variableList.remove(pointer);
			}
			if(variableList.size() == 0) return null;
			graph.addClause(new Clause(++clauses));
			for (Variable variable : variableList ) {
				graph.addEdge( new Edge( graph.getClause(clauses), variable, variable.getAssignmentInt()*(-1)));
				graph.getClause(clauses).increaseAssignedVariables();
				variable.addToLiteralCounters(variable.getAssignmentInt()*(-1));
			}
			graph.getClause(clauses).setWatches();
			if(info) System.out.println("learned clause " + graph.getClause(clauses).toString());
			for(Variable variable : variableList) {
				if(variable.getLevel() == clauseLevel && variable.getAntecedent() == null) return variable;
				if(variable.getLevel() == 0 && variable.getAntecedent() != null) break loop;
			}
			variableList.clear();
			conflictClause = graph.getClause(clauses);
		}
		return null;
	}
}
	