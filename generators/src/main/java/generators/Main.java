package generators;

import ilog.concert.*; 
import ilog.cplex.*;

public class Main {

	public static void main(String[] args) {
	       try {
	    	   IloCplex cplex = new IloCplex();
	       } catch (IloException e) {
	    	   System.err.println("Concert exception caught: " + e); 
	       }

	}

}
