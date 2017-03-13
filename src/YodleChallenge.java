// Author: Nicholas Wein

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

public class YodleChallenge {
	
	static HashMap<Circuit,ArrayList<Juggler>> circuitsToJugglers = 
			new HashMap<Circuit,ArrayList<Juggler>>();
	
	static HashMap<Juggler,ArrayList<String>> jugglersToPrefs = 
			new HashMap<Juggler,ArrayList<String>>();
	
	static CopyOnWriteArrayList<Juggler> unassigned = 
			new CopyOnWriteArrayList<Juggler>();
	
	static int numJugglers = 0;
	
	
	static class Circuit {
		
		String name;
		int H;
		int E;
		int P;
		
		public Circuit(String name, int H, int E, int P) {
			this.name = name;
			this.H = H;
			this.E = E;
			this.P = P;
		}
	}
	
	static class Juggler {
		
		String name;
		int H;
		int E;
		int P;
		ArrayList<String> circuitPrefs = new ArrayList<String>();
		
		public Juggler(String name, int H, int E, int P, String[] prefs) {
			this.name = name;
			this.H = H;
			this.E = E;
			this.P = P;
			
			for (int i = 0; i < prefs.length; i++) {
				circuitPrefs.add(prefs[i]);
			}
		}
		
	}
	
	// returns a Circuit object based on the name identifier
	public static Circuit getCircuitByName(String name) {
		for (Circuit c : circuitsToJugglers.keySet()) {
			if (c.name.equals(name)) {
				return c;
			}
		}
		
		return null;
	}
	
	public static int getDotProduct(Juggler j, Circuit c) {
		return (j.H*c.H + j.E*c.E + j.P*c.P);
	}
	
	// assign each juggler his/her first preference to start
	public static void initCircuitAssignments() {
		for (Juggler j : unassigned) {
			Circuit firstPref = getCircuitByName(j.circuitPrefs.remove(0));
			circuitsToJugglers.get(firstPref).add(j);
		}
		
		unassigned.clear();
	}
	
	// sorts jugglers in a list by their dot product with the circuit
	public static void sortJugglersByDotProduct(ArrayList<Juggler> list, final Circuit c) {
		Collections.sort(list, new Comparator<Juggler>() {
			public int compare(Juggler j1, Juggler j2) {
				int dotProd1 = getDotProduct(j1, c);
				int dotProd2 = getDotProduct(j2, c);
				return (dotProd2 - dotProd1);
			}
		});
	}
	
	// trim circuit size to ensure no circuit has more jugglers than capacity
	public static void trimCircuitsToCapacity() {
		int circuitCap = getCircuitCapacity();
		
		for (Circuit c : circuitsToJugglers.keySet()) {
			ArrayList<Juggler> jugglers = circuitsToJugglers.get(c);
			int size = jugglers.size();
			
			if (size > circuitCap) {
				sortJugglersByDotProduct(jugglers, c);
				
				for (int i = size - 1; i >= circuitCap; i--) {
					Juggler j = jugglers.remove(i);
					unassigned.add(j);
				}
			}
		}
	}
	
	// replaces j with lowest-ranked juggler if j's dot product is larger
	public static void sortJugglerIntoCircuit(Juggler j, Circuit c) {
		ArrayList<Juggler> jugglers = circuitsToJugglers.get(c);
		
		// circuit is not yet full
		if (jugglers.size() < getCircuitCapacity()) {
			j.circuitPrefs.remove(0);
			unassigned.remove(j);
			jugglers.add(j);
			sortJugglersByDotProduct(jugglers, c);
			return;
		}
		
		sortJugglersByDotProduct(jugglers, c);
		Juggler currSmallest = jugglers.get(jugglers.size() - 1);
		
		if (getDotProduct(j, c) > getDotProduct(currSmallest, c)) {
			Juggler kickedOut = jugglers.remove(jugglers.size() - 1);
			unassigned.add(kickedOut);
			j.circuitPrefs.remove(0);
			unassigned.remove(j);
			jugglers.add(j);
			sortJugglersByDotProduct(jugglers, c);
		}
	}
	
	// randomly assign remaining jugglers who were not assigned a preference
	public static void assignRemainingJugglers() {
		int circuitCap = getCircuitCapacity();
		
		for (Circuit c : circuitsToJugglers.keySet()) {
			ArrayList<Juggler> jugglers = circuitsToJugglers.get(c);
			int size = jugglers.size();
			
			if (size < circuitCap) {
				int numToAdd = circuitCap - size;
				for (int i = 0; i < numToAdd; i++) {
					Juggler j = unassigned.get(0);
					unassigned.remove(j);
					jugglers.add(j);
				}
				
				sortJugglersByDotProduct(jugglers, c);
			}
		}
	}
	
	// maximum number of jugglers assigned to a given circuit
	public static int getCircuitCapacity() {
		return (numJugglers / circuitsToJugglers.keySet().size());
	}
	
	// for testing purposes
	public static void printMap() {
		for (Circuit c : circuitsToJugglers.keySet()) {
			System.out.println("============");
			System.out.println(c.name);
			ArrayList<Juggler> list = circuitsToJugglers.get(c);
			sortJugglersByDotProduct(list, c);
			for (int i = 0; i < list.size(); i++) {
				System.out.println("----" + list.get(i).name + ": " + getDotProduct(list.get(i), c));
			}
			System.out.println("============");
		}
	}
	
	// for testing purposes
	public static void printUnassigned() {
		System.out.println("UNASSIGNED");
		for (Juggler j : unassigned) {
			System.out.println(j.name);
		}
	}
	
	// returns the final answer
	public static int getSum() {
		int sum = 0;
		Circuit c = getCircuitByName("C1970");
		ArrayList<Juggler> jugglers = circuitsToJugglers.get(c);
		for (Juggler j : jugglers) {
			sum += Integer.parseInt(j.name.substring(1));
		}
		
		return sum;
	}
	
	// writes circuit assignments to file
	public static void writeOutputFile() {
		try {
			
			PrintWriter pw = new PrintWriter("output.txt", "UTF-8");
			
			for (Circuit c : circuitsToJugglers.keySet()) {
				pw.print(c.name);
				
				ArrayList<Juggler> jugglers = circuitsToJugglers.get(c);
				int numLeft = jugglers.size();
				for (Juggler j : jugglers) {
					numLeft--;
					pw.print(" ");
					pw.print(j.name);
					
					ArrayList<String> prefs = jugglersToPrefs.get(j);
					for (int i = 0; i < prefs.size(); i++) {
						String name = prefs.get(i);
						Circuit currPref = getCircuitByName(name);
						pw.print(" " + name + ":" + getDotProduct(j, currPref));
					}
					
					if (numLeft > 0) {
						pw.print(",");
					}
				}
				pw.print("\r\n");
			}
	        
			pw.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		
		if (args.length < 1) {
			System.out.println("Please provide an input file.");
			System.exit(0);
		}
		
		String filename = args[0];
		
		try {
			
			InputStream stream = new FileInputStream(filename);
	        Scanner sc = new Scanner(stream);
	        sc.useDelimiter("\\n");
	        
	        while (sc.hasNext()) {
	        	String currLine = sc.next();
	        	String[] lineElements = currLine.trim().split("\\s+");
	        	
	        	if (lineElements[0].equals("C")) {
	        		String circuitName = lineElements[1];
	        		int H = Integer.parseInt(lineElements[2].split(":")[1]);
	        		int E = Integer.parseInt(lineElements[3].split(":")[1]);
	        		int P = Integer.parseInt(lineElements[4].split(":")[1]);
	        		
	        		Circuit c = new Circuit(circuitName, H, E, P);
	        		circuitsToJugglers.put(c, new ArrayList<Juggler>());
	        	} 
	        	else if (lineElements[0].equals("J")) {
	        		String jugglerName = lineElements[1];
	        		int H = Integer.parseInt(lineElements[2].split(":")[1]);
	        		int E = Integer.parseInt(lineElements[3].split(":")[1]);
	        		int P = Integer.parseInt(lineElements[4].split(":")[1]);
	        		String[] circuitPrefs = lineElements[5].split(",");
	        		
	        		Juggler j = new Juggler(jugglerName, H, E, P, circuitPrefs);
	        		jugglersToPrefs.put(j, new ArrayList<String>(j.circuitPrefs));
	        		unassigned.add(j);
	        		numJugglers++;
	        	}
	        }
	        
			for (Juggler j : unassigned) {
	        	Circuit c = getCircuitByName(j.circuitPrefs.get(0));
	        	sortJugglerIntoCircuit(j, c);
	        }
			
			assignRemainingJugglers();
			
			writeOutputFile();
			
			System.out.println("FINISHED.");
	        
	        stream.close();
	        sc.close();
	        
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}