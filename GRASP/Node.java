import java.util.*;

public class Node {
	int index;
	boolean clause;
	LinkedList<Edge> incidences = new LinkedList<Edge>();

	public Node(int index) {
		this.index = index;
	}

	public LinkedList<Edge> getIncidences() {
		return incidences;
	}

	public int getIndex() {
		return index;
	}

	public boolean isClause() { return clause; }
}