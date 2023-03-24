import java.io.*;
import java.util.*;

public class Chaff {
	static Graph graph;
	static int variables, clauses, depth, trueclauses, restartTicker, divideTicker;
	static boolean info = false;
	static Stack<Variable> variableStack = new Stack<Variable>();
	static Random random;
	static long start, stop;
	static double restartModifier, divideModifier, alpha;
	static String fileName;

	public static void main(String[] args) {
		try {
			if(args.length == 1 && args[0].equals("-help")) { System.out.println("java Chaff FILE [-info]"); System.exit(0); }
			fileName = args[0];
			Scanner scanner = new Scanner(new File(fileName));

			info = args.length == 2 && args[1].equals("-info");
			//ignores the first two words
			scanner.next(); 
			scanner.next(); 
			//third and fourth word are the amount of variables and clauses
			variables = Integer.parseInt(scanner.next()); 
			clauses   = Integer.parseInt(scanner.next());
			trueclauses = clauses;
			random = new Random();
			restartTicker = 0;
			divideTicker = 0;
			alpha = clauses/variables;
			//0.2??? 2.5
			//divideModifier = Double.parseDouble(args[2]);
			//2 1.2
			//restartModifier = Double.parseDouble(args[1]);

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
				temp.fillIncidences();
				temp.setWatches();
			}

			for ( Variable variable : graph.getVariables() ) {
				variable.setLiteralCounters();
			}

			if(info) System.out.println("graph created");
			start = System.currentTimeMillis();
			if(chaff()) {
				stop = System.currentTimeMillis();
				if(!graph.isSAT()) System.out.println("Fehler");
				System.out.println(fileName + " is satisfiable in " + (stop - start) + " ms");
				//System.out.println(fileName.substring(17,20) + " " + (stop - start));
				if(info) graph.printAssignments();
			} else {
				stop = System.currentTimeMillis();
				if(graph.isSAT()) System.out.println("Fehler");
				System.out.println(fileName + " is unsatisfiable in " + (stop - start) + " ms");
				//System.out.println(fileName.substring(17,20) + " " + (stop - start));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean chaff() {
		LinkedList<Clause> queue = new LinkedList<Clause>();
		for (Clause clause : graph.getClauses()) {
			if(clause.isUnitClause()) {
				queue.add(clause);
				if(info) System.out.println("added clause " + clause.getIndex() + " to queue on first ucp detection");
			}
		}
		if(unitClausePropagation(queue) != null) return false;

		int depth = 0;

		//purityTest(depth);

		while(!variableStack.empty()) variableStack.pop();
		Variable nextUnassigned = null;

		int restartCount = 0;
		while(!graph.allAssigned()) {
			if(System.currentTimeMillis() - start > 100000) { System.out.println(fileName + " has timed out"); System.exit(0); }
			if(restartTicker > (restartCount+1)*trueclauses*0.55) {
				restartTicker = 0;
				//System.out.println("set restartModifier to " + restartModifier);
				if(info) System.out.println("restarted");
				while(!variableStack.empty()) graph.unassign(variableStack.pop());
				queue.clear();
				restartCount++;
				for(int i = trueclauses + 1; i <= clauses; i++) {
					if(graph.getClause(i).getIncidences().length > variables/10 && graph.getClause(i).getCreationTime() + clauses*10 > System.currentTimeMillis()) { 
						if(info) System.out.println("removed " + graph.getClause(i).toString());
						graph.getClauses().remove(--i); 
						clauses--; 
					}
				}
				depth = 0;
				continue; 
			}
			if(divideTicker > variables*restartModifier) {
				divideTicker = 0;
				//System.out.println("set divideModifier to " + divideModifier);
				if(info) System.out.println("halved literal counters");
				for (Variable variable : graph.getVariables()) {
					//1.2
					variable.divideLiteralCounters(alpha*divideModifier);
				}
			}
			if(queue.size() > 0) {
				Clause conflictClause = unitClausePropagation(queue);
				if(conflictClause == null) continue;
				int conflictLevel = conflictClause.getMaxDepth();
				Variable level = learnChaffClause(conflictClause);
				if(level == null || level.getLevel() == 0) return false;
				if(level.getLevel() == conflictLevel) {
					while(!variableStack.empty()) {
						graph.unassign(variableStack.pop());
						restartTicker++;
						divideTicker++;
					}
					depth = 0;
					graph.getClause(clauses).setLearntClauseWatches();
					for(int i = trueclauses + 1; i <= clauses; i++) {
						if(graph.getClause(i).getIncidences().length > variables/10 && graph.getClause(i).getCreationTime() + clauses*10 > System.currentTimeMillis()) { 
							if(info) System.out.println("removed " + graph.getClause(i).toString());
							graph.getClauses().remove(--i); 
							clauses--; 
						}
					}
					for(Clause clause : graph.getClauses()) if(clause.isUnitClause()) {
						queue.add(clause);
						if(info) System.out.println("added " + clause.toString() + " to queue as ucp");
					}
					continue;
				}
				depth = level.getLevel();
				while(variableStack.peek().getLevel() > depth /*|| variableStack.peek().getUCPLevel() > 0*/) {
					graph.unassign(variableStack.pop());
					restartTicker++;
					divideTicker++;
				}
				graph.getClause(clauses).setLearntClauseWatches();
				
				for(Clause clause : graph.getClauses()) {
					if(clause.isUnitClause()) queue.add(clause);
				}

			} else {
				nextUnassigned = graph.getNextUnassigned();
				depth++;
				restartTicker++;
				divideTicker++;
				if(nextUnassigned.getPositiveLiterals() > nextUnassigned.getNegativeLiterals()) {
					graph.assign(nextUnassigned, 1, depth, null);
					if(info) System.out.println("assigning " + nextUnassigned.toString() + " with " + 1 + " at depth " + depth + ", " + (variables - graph.getAssignedAmount() + " still unassigned"));
				}
				else {
					graph.assign(nextUnassigned, -1, depth, null);
					if(info) System.out.println("assigning " + nextUnassigned.toString() + " with " + -1 + " at depth " + depth + ", " + (variables - graph.getAssignedAmount() + " still unassigned"));
				}

				variableStack.push(nextUnassigned);

				for(Edge e : nextUnassigned.getIncidences()) {
					if(e.getClause().isUnitClause()) {	
						queue.add(e.getClause());
						if(info) System.out.println("added " + e.getClause().toString() + " to queue as ucp");
					}
				}
			}
		}
		return true;
	}

	public static boolean purityTest(int level) {
		boolean unpure = false;
		for(Variable variable : graph.getVariables()) {
			if(variable.isPure() != 0 && variable.getAssignmentInt() == 0) {
				unpure = true;
				graph.assign(variable, variable.isPure(), level, null); 
				variableStack.push(variable);
				if(info) System.out.println("assigned pure " + variable.toString() + " with " + variable.isPure()); 
			}
		}
		return unpure;
	}

	//returns the a conflict clause, or null if no conflict is found
	public static Clause unitClausePropagation(LinkedList<Clause> queue) {
		Clause conflictClause = null;
		while (queue.size() > 0) {
			if(queue.getFirst().isUnitClause()) {
				Edge unitEdge = queue.getFirst().getUnitEdge();
				restartTicker++;
				divideTicker++;
				int localDepth = queue.getFirst().getMaxDepth();

				if (!graph.assign(unitEdge.getVariable(), unitEdge.getSign(), localDepth, queue.getFirst()))  { System.out.println(unitEdge.getVariable() + ", " + queue.getFirst().toString()); System.exit(0); }

				//if(!variableStack.empty() && variableStack.peek().getLevel() > unitEdge.getVariable().getLevel()) System.out.println(variableStack.peek().toString() + ", " + unitEdge.getVariable().toString());

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
						//System.out.println("watches are " + e.getClause().getFirstWatch().toString() + ", " + e.getClause().getSecondWatch().toString());
						queue.clear();
						return e.getClause();
					}
				}

			}
			queue.removeFirst();
		}	
		return conflictClause;
	}

	public static Variable learnChaffClause(Clause conflictClause) {
		LinkedList<Variable> variableList = new LinkedList<Variable>();
		int clauseLevel = conflictClause.getMaxDepth();
		int ucpLevel = conflictClause.getMaxUCPLevel();
		int readypointer = 0;
		
		//fill the resolution set with the literals from the conflict clause, ignore literals with level = 0, literals with decision level < clauseLevel put at the end and not to be touched again
		for(Edge e : conflictClause.getIncidences()) {
			if(e.getVariable().getLevel() == 0) continue;
			if(e.getVariable().getLevel() < clauseLevel) { 
				variableList.addLast(e.getVariable());
				continue;
			}
			if(e.getVariable().getUCPLevel() < ucpLevel) {
				variableList.add(readypointer, e.getVariable());
				readypointer++;
				continue;
			}
			variableList.addFirst(e.getVariable());
			readypointer++;
		}
		if(variableList.size() == 0) return null;
		while( readypointer > 1) {
			if(variableList.getFirst().getUCPLevel() < ucpLevel) {
				//if first element in list is lower than ucpLevel, move all element with ucplevel = ucpLevel to front of list
				int stop = 0;
				for(int i = readypointer - 1; i > stop; i--) {
					if(variableList.get(i).getUCPLevel() < ucpLevel) continue;
					variableList.add(stop++, variableList.remove(i));
					i++;
				}
			}
			//if list contains no element at current ucpLevel decrease ucpLevel and continue while loop
			if(variableList.getFirst().getUCPLevel() < ucpLevel) { ucpLevel--; continue; }
			//add all literals from antecedent of first element in list apart from element itself and assignments set at level 0
			for(Edge e : variableList.getFirst().getAntecedent().getIncidences()) {
				if(variableList.contains(e.getVariable()) || e.getVariable().getLevel() == 0) continue;
				if(e.getVariable().getLevel() < clauseLevel) { 
					variableList.addLast(e.getVariable());
					continue;
				}
				variableList.add(readypointer, e.getVariable());
				readypointer++;
			}
			variableList.removeFirst();
			readypointer--;
		}
		//if(variableList.size() == 0) return null;

		graph.addClause(new Clause(++clauses));
		for (Variable variable : variableList ) {
			graph.addEdge( new Edge( graph.getClause(clauses), variable, variable.getAssignmentInt()*(-1)));
			variable.addToLiteralCounters(variable.getAssignmentInt()*(-1));
		}
		graph.getClause(clauses).fillIncidences();
		if(info) System.out.println("learned clause " + graph.getClause(clauses).toString());

		if(variableList.size() == 1) return variableList.getFirst();

		Variable max = variableList.get(1);
		for(int i = 1; i < variableList.size(); i++){
			if(max.getLevel() < variableList.get(i).getLevel()) max = variableList.get(i);
		}

		return max;
	}
}