package generators;

import ilog.concert.*; 
import ilog.cplex.*;
import ilog.cplex.IloCplex.CplexStatus;

public class Main2 {

	public static void main(String[] args) {
	       try {
	    	   IloCplex cplex = new IloCplex();
	    	   IloNumVar h = cplex.numVar(0, Double.MAX_VALUE);
	    	   IloNumVar g = cplex.numVar(0, 10);
	    	   IloNumVar hminus = cplex.numVar(10, 20);
	    	   IloNumVar hplus = cplex.numVar(5, 40);
	    	   IloLinearNumExpr su = cplex.linearNumExpr();
	    	   su.addTerm(1.0, hplus);
	    	   su.addTerm(-1, hminus);
	    	   cplex.addLe(h, cplex.negative(su));
	    	   cplex.addLe(h, su);
	    	   IloLinearNumExpr pw = cplex.linearNumExpr();
	    	   pw.addTerm(3, h);
	    	   //pw.addTerm(3, hplus);
	    	   //pw.addTerm(3, hminus);
	    	   pw.addTerm(1, g);
	    	   //cplex.addLe(cplex.negative(je1), j);
	    	   //cplex.addRange(je1, j, je1);
	    	   //System.out.println("j: " + j);
	    	   System.out.println(cplex);
	    	   //cplex.addMinimize(pw);
	    	   //koniec przykladu
	    	   //IloNumExpr total = cplex.numExpr();
	    	   //total = cplex.sum(total, cplex.abs(su));
	    	   //cplex.addMinimize(total);
	    	   //cplex.addLe(cplex.sum(cplex.negative(x[0]),x[1],x[2]),20.0);
	    	   
	    	   IloNumVar[] xy = cplex.boolVarArray(5);
	    	   IloNumExpr total = cplex.numExpr();
	    	   for(int i = 0; i<xy.length-1; i++) {
	    		   IloLinearNumExpr su2 = cplex.linearNumExpr();
		    	   su2.addTerm(0.5, xy[i]);
		    	   su2.addTerm(-0.5, xy[i+1]);
		    	   total = cplex.sum(total, cplex.abs(su2));
		    	   total = cplex.sum(total, cplex.negative(su2));
	    	   }
	    	   
	    	   cplex.addEq(0, xy[0]);
	    	   cplex.addEq(1, xy[1]);
	    	   cplex.addEq(0, xy[2]);
	    	   cplex.addEq(1, xy[3]);
	    	   cplex.addEq(1, xy[4]);
	    	   
	    	   cplex.addMaximize(total);
	    	   
	    	   
	    	   cplex.solve();
	    	   CplexStatus status = cplex.getCplexStatus();
	    	   System.out.println(cplex.getValue(total));
	    	   double[]	xval = cplex.getValues(xy);
	    	   for(double xv : xval) {
	    		   System.out.println(xv);
	    	   }
	    	   //System.out.println(cplex.getValue(h));
	    	   //System.out.println(cplex.getValue(hplus));
	    	   //System.out.println(cplex.getValue(hminus));
	       } catch (IloException e) {
	    	   System.err.println("Concert exception caught: " + e); 
	       }

	}

}
