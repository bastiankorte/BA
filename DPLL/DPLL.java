import java.io.*;
import java.util.*;

public class DPLL {
	static Graph graph;
	static int variables, clauses, depth;
	static boolean info = false;
	static Stack<Variable> variableStack = new Stack<Variable>();
	static Random random;
	static long start, stop;

	public static void main(String[] args) {
		try {
			Scanner scanner = new Scanner(new File(args[0]));

			if(args.length == 1 && args[0].equals("-help")) { System.out.println("java SAT FILE BACKTRACKINGSCHEME [-info]"); System.exit(0); }
			info = args.length == 2 && args[1].equals("-info");
			//ignores the first two words
			scanner.next(); 
			scanner.next(); 
			//third and fourth word are the amount of variables and clauses
			variables = Integer.parseInt(scanner.next()); 
			clauses   = Integer.parseInt(scanner.next());
			random = new Random();

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
			}

			if(info) System.out.println("graph created");
			start = System.currentTimeMillis();
			if(dpll()) {
				stop = System.currentTimeMillis();
				if(!graph.isSAT()) System.out.println("Fehler");
				System.out.println(args[0] + " is satisfiable in " + (stop - start) + " ms");
				//System.out.println(args[0].substring(17,20) + " " + (stop - start));
				if(info) graph.printAssignments();
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

	public static boolean dpll() {
		LinkedList<Clause> queue = new LinkedList<Clause>();
		int depth = 0;
		for(Clause clause : graph.getClauses()) if(clause.isUnitClauseGRASP()) queue.add(clause);
		while(!graph.isSATDPLL()) {
			if(!unitClausePropagation(queue)) {
				if(variableStack.empty()) return false;
				while(variableStack.peek().getAssignmentInt() == -1 || variableStack.peek().getAntecedent() != null) { graph.unassignDPLL(variableStack.pop()); if(variableStack.empty()) return false; }
				depth = variableStack.peek().getLevel();
				Variable flipVariable = graph.unassignDPLL(variableStack.peek());
				graph.assignDPLL(flipVariable, -1, depth, null);
				if(info) System.out.println("flipped " + flipVariable.toString());
				for(Edge e : flipVariable.getIncidences()) {
					if(!e.getClause().isUnitClauseGRASP()) continue;
					queue.add(e.getClause());
					if(info) System.out.println("added " + e.getClause().toString() + " to queue");
				}
				continue;
			}
			purityTest(depth);
			if(graph.getAssignedAmount() == variables) continue;
			Variable randomAssign = graph.getVariableList().get(random.nextInt(graph.getVariableList().size()));
			if(randomAssign.getAssignmentInt() != 0) { System.out.println("Fehler " + randomAssign.toString()); System.exit(0); }
			graph.assignDPLL(randomAssign, 1, ++depth, null);
			variableStack.push(randomAssign);
			if(info) System.out.println("assigning " + randomAssign.toString());
			for(Edge e : randomAssign.getIncidences()) {
				if(!e.getClause().isUnitClauseGRASP()) continue;
				queue.add(e.getClause());
				if(info) System.out.println("added " + e.getClause().toString() + " to queue");
			}
		}
		return true;
	}

	public static void purityTest(int level) {
		for(Variable variable : graph.getVariables()) {
			if(variable.isPure() != 0 && variable.getAssignmentInt() == 0) {
				if(info) System.out.println("assigning pure " + variable.toString() + " with " + variable.isPure()); 
				graph.assignDPLL(variable, variable.isPure(), level, variable.getIncidences().getFirst().getClause()); 
				if(level > 0)variableStack.push(variable);
			}
		}
	}

	//returns the a conflict clause, or null if no conflict is found
	public static boolean unitClausePropagation(LinkedList<Clause> queue) {
		while (queue.size() > 0) {
			if(queue.getFirst().isUnitClauseGRASP()) {
				Edge unitEdge = queue.getFirst().getUnitEdgeGRASP();

				int localDepth = queue.getFirst().getMaxDepth();

				if (!graph.assignDPLL(unitEdge.getVariable(), unitEdge.getSign(), localDepth, queue.getFirst()))  { System.out.println(unitEdge.getVariable() + ", " + queue.getFirst().toString()); System.exit(0); }

				if(!variableStack.empty() && variableStack.peek().getLevel() > unitEdge.getVariable().getLevel()) System.out.println("error");//variableStack.peek().toString() + ", " + unitEdge.getVariable().toString());

				if(info) System.out.println("assigning " + unitEdge.getVariable() + " with " + unitEdge.getSign() + " via UCP-clause " + queue.getFirst().toString() + " at depth " + localDepth + " and UCP-Depth " + queue.getFirst().getMaxUCPLevel() + ", " + (variables - graph.getAssignedAmount() + " still unassigned"));
				if(localDepth != 0) variableStack.push(unitEdge.getVariable());
				for (Edge e : unitEdge.getVariable().getIncidences()) {
					if(!e.equals(unitEdge) && e.getClause().isUnsatDPLL()) {
						if(info) System.out.println("conflict in " + e.getClause().toString());
						//System.out.println("watches are " + e.getClause().getFirstWatch().toString() + ", " + e.getClause().getSecondWatch().toString());
						queue.clear();
						return false;
					}
				}
				for(Edge e : unitEdge.getVariable().getIncidences()) {
					if(e.getClause().isUnitClauseGRASP()) {
				    	queue.addLast(e.getClause());
				    	if(info) System.out.println("added clause " + e.getClause().getIndex() + " to queue" );
					}
				}
			}
			queue.removeFirst();
		}	
		return true;
	}	
}