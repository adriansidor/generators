package generators;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.CplexStatus;

public class Main {

	public static void main(String[] args) {
		try {
	    	   IloCplex cplex = new IloCplex();
	    	   
	    	   //dostepne generatory
	    	   int[] dg = {16, 14, 12};
	    	   //liczba typ�w generatorow
	    	   int T = 3;
	    	   //liczba por dnia
	    	   int d = 5;
	    	   //Wektory reprezentujace generatory pradu typu T pracujace o porze d (GdT)
	    	   IloNumVar[][][] GdT = new IloNumVar[d][T][];
	    	   for(int i = 0; i<d; i++) {
	    		   for(int j = 0; j<T; j++) {
	    			   GdT[i][j] = cplex.boolVarArray(dg[T]);
	    		   }
	    	   }
	    	   
	    	   IloNumVar[] x = cplex.numVarArray(3, 0.0, 100.0, IloNumVarType.Int);
	    	   IloNumVar[] y = cplex.numVarArray(3, 2.0, 15.0);
	    	   IloLinearNumExpr lexpr = cplex.linearNumExpr();
	    	   lexpr.addTerm(1.0, x[0]);
	    	   lexpr.addTerm(2.0, x[1]);
	    	   lexpr.addTerm(3.0, x[2]);
	    	   lexpr.addTerm(3.0, y[0]);
	    	   lexpr.addTerm(5.3, y[1]);
	    	   lexpr.addTerm(5.0, y[2]);
	    	   cplex.addMaximize(lexpr);
	    	   cplex.addLe(cplex.sum(cplex.negative(x[0]),x[1],x[2]),20.0);
	    	   cplex.solve();
	    	   CplexStatus status = cplex.getCplexStatus();
	    	   double objval = cplex.getObjValue();
	    	   System.out.println(objval);
	    	   System.out.println(status.toString());
	    	   double[]	xval = cplex.getValues(x);
	    	   for(double xv : xval) {
	    		   System.out.println(xv);
	    	   }
	    	   double[] yval = cplex.getValues(y);
	    	   for(double xv : yval) {
	    		   System.out.println(xv);
	    	   }
	       } catch (IloException e) {
	    	   System.err.println("Concert exception caught: " + e); 
	       }


	}

}
