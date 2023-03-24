import java.io.*;
import java.util.*;

public class CreateTestFile {
	static int variables, clauses, temp1, temp2, temp3;
	static double alpha;
	static Random random = new Random();
	static LinkedList<Integer> list = new LinkedList<Integer>();
	public static void main(String[] args) {
		try{
			variables = Integer.parseInt(args[0]);
			if(variables < 3) throw new IllegalArgumentException("has to have more than 3 variables");
			for(int i = 1; i <= variables; i++) list.addLast(i); 
			alpha = Double.parseDouble(args[1]);
			clauses = (int)(variables * alpha);
			String file = "p cnf " + variables + " " + clauses + "\n";
			for (int i = 0; i < clauses; i++) {
				if(list.size() > 0) temp1 = list.remove(random.nextInt(list.size())).intValue();
				else temp1 = random.nextInt(variables)+1;
				temp2 = temp1;
				while(temp1 == temp2) temp2 = random.nextInt(variables)+1;
				temp3 = temp1;
				while(temp1 == temp3 || temp2 == temp3) temp3 = random.nextInt(variables)+1;

				if(random.nextBoolean()) file += temp1 + " ";
				else file += "-" + temp1 + " ";
				if(random.nextBoolean()) file += temp2 + " ";
				else file += "-" + temp2 + " ";
				if(random.nextBoolean()) file += temp3 + " ";
				else file += "-" + temp3 + " ";

				file += "0" + "\n";
			}
			System.out.println(file);
		} catch(Exception e) { list.size(); e.printStackTrace(); }
	}
}