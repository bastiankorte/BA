import java.io.*;
import java.util.*;

public class ReadTimeResults {
	public static double[] unsatCount = new double[600];
	public static void main(String[] args) {
		try {
			Scanner scanner = new Scanner(new File(args[0]));
			int tempIndex;
			while(scanner.hasNext()) {
				tempIndex = Integer.parseInt(scanner.next());
				unsatCount[tempIndex-200] += Integer.parseInt(scanner.next());
			}
			//for(int i = 0; i < 600; i++) {
			//	unsatCount[i] = unsatCount[i]/50;
			//}
			String position = "";
			for (int i = 0; i < 600; i = i+5) {
				position = "" + (200+i);
				System.out.print("(" + position.substring(0,1) + "." + position.substring(1,3) + "," + unsatCount[i] + ")");
			}
			System.out.println();
			for(int i = 10; i < 589; i = i+5) {
				position = "" + (200+i);
				System.out.print("(" + position.substring(0,1) + "." + position.substring(1,3) + "," + (unsatCount[i-10] + unsatCount[i-5] + unsatCount[i] + unsatCount[i+5] + unsatCount[i+10])/5 + ")");
			}
		} catch(Exception e) { e.printStackTrace(); }
	}
}
