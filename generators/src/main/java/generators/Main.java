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
	    	   //liczba typów generatorow
	    	   int T = 3;
	    	   //liczba por dnia
	    	   int d = 5;
	    	   //zapotrzebowanie na prad
	    	   double[] zp = {15000.0, 35000.0, 20000.0, 45000.0, 20000.0};
	    	   //obciazenie minimalne generatorów
	    	   double[] omin = {1000.0, 1300.0, 1500.0};
	    	   //obciazenie maksymalne generatorow
	    	   double[] omax = {2000.0, 1800.0, 3000.0};
	    	   //koszt uruchomienia generatora
	    	   int[] ku = {2000, 1500, 1000};
	    	   //koszt godziny pracy generatora przy minimalnym obciazeniu
	    	   int[] km = {1000, 2500, 3200};
	    	   //Wektory reprezentujace generatory pradu typu T pracujace o porze d (GdT)
	    	   //jako zmienna binarna, pomocnicza (flaga dodatniosci)
	    	   IloNumVar[][][] G = new IloNumVar[d][T][];
	    	   for(int i = 0; i<d; i++) {
	    		   for(int j = 0; j<T; j++) {
	    			   G[i][j] = cplex.boolVarArray(dg[j]);
	    		   }
	    	   }
	    	   
	    	   //Wektory reprezentujace obciazenie generatorow pradu typu T o porze d (OdT)
	    	   IloNumVar[][][] O = new IloNumVar[d][T][];
	    	   for(int i = 0; i<d; i++) {
	    		   for(int j = 0; j<T; j++) {
	    			   G[i][j] = cplex.numVarArray(dg[j], 0, Double.MAX_VALUE);
	    		   }
	    	   }
	    	   
	    	   //zamodelowanie zmiennej O jako semicontinous - (g*omin<=O<=g*omax) || O=0
	    	   for(int i = 0; i<d; i++) {
	    		   for(int j = 0; j<T; j++) {
	    			   for(int x = 0; x<dg[j]; x++) {
	    				   //zamodelowanie g*omin <= o
		    			   IloLinearNumExpr lb = cplex.linearNumExpr();
		    			   lb.addTerm(omin[j], G[i][j][x]);
		    	    	   cplex.addGe(O[i][j][x], lb);
		    	    	   //zamodelowanie g*omax >= o
		    	    	   IloLinearNumExpr ub = cplex.linearNumExpr();
		    	    	   ub.addTerm(omax[j], G[i][j][x]);
		    	    	   cplex.addLe(O[i][j][x], ub);
	    			   }
	    		   }
	    	   }
	    	   
	    	   //ograniczenie na zaspokojenie zapotrzebowania na prad w ciagu doby
	    	   
	    	   //obciazenie pracujacych generatorow o roznych porach dnia
    		   IloLinearNumExpr[] opg = new IloLinearNumExpr[d];
	    	   for(int i = 0; i<d; i++) {
	    		   opg[i] = cplex.linearNumExpr();
	    		   for(int j = 0; j<T; j++) {
	    			   for(int x = 0; x<dg[j]; x++) {
	    				   opg[i].addTerm(1.0, O[i][j][x]);	    				   
	    			   }
	    		   }
	    		   //obciazenie musi byc >= zapotrzebowaniu na prad
	    		   cplex.addGe(opg[i], zp[i]);
	    	   }
	    	   
	    	   //pracujace generatory musza miec mozliwosc zaspokojenia wzrostu
	    	   //zapotrzebowania na prad o 10%
	    	   
	    	   //obciazenie maksymalne pracujacych generatorow * 0.9
	    	   for(int i = 0; i<d; i++) {
	    		   IloLinearNumExpr omg = cplex.linearNumExpr();
	    		   for(int j = 0; j<T; j++) {
	    			   for(int x = 0; x<dg[j]; x++) {
	    				   omg.addTerm(0.9*omax[j], G[i][j][x]);	    				   
	    			   }
	    		   }
	    		   cplex.addLe(opg[i], omg);
	    	   }
	    	   
	    	   //jesli jest uzyty T1 to musi byc rowniez uzyty generator T2 lub T3
	    	   for(int i = 0; i<d; i++) {
	    		   IloLinearNumExpr w = cplex.linearNumExpr();
	    		   for(int j = 0; j<T; j++) {
	    			   for(int x = 0; x<dg[j]; x++) {
	    				   if(j == 0) {
	    					   w.addTerm(1.0, G[i][j][x]);
	    				   } else {
	    					   w.addTerm(-1.0, G[i][j][x]);
	    				   }				   
	    			   }
	    		   }
	    		   cplex.addEq(w, 0);
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
	    	   
	    	   IloNumVar[] u = cplex.boolVarArray(3);
	    	   IloNumVar[] f = cplex.numVarArray(3, 0.0, Double.MAX_VALUE);
	    	   double a = 10;
	    	   double b = 20;
	    	   IloLinearNumExpr aexpr1 = cplex.linearNumExpr();
	    	   aexpr1.addTerm(a, u[0]);
	    	   cplex.addGe(f[0], aexpr1);
	    	   IloLinearNumExpr bexpr1 = cplex.linearNumExpr();
	    	   bexpr1.addTerm(b, u[0]);
	    	   cplex.addLe(f[0], bexpr1);
	    	   
	    	   IloLinearNumExpr expr2 = cplex.linearNumExpr();
	    	   expr2.addTerm(a, u[1]);
	    	   cplex.addGe(f[1], expr2);
	    	   IloLinearNumExpr bexpr2 = cplex.linearNumExpr();
	    	   bexpr2.addTerm(b, u[1]);
	    	   cplex.addLe(f[1], bexpr2);
	    	   
	    	   IloLinearNumExpr expr3 = cplex.linearNumExpr();
	    	   expr3.addTerm(a, u[2]);
	    	   cplex.addGe(f[2], expr3);
	    	   IloLinearNumExpr bexpr3 = cplex.linearNumExpr();
	    	   bexpr3.addTerm(b, u[2]);
	    	   cplex.addLe(f[2], bexpr3);
	    	   
	    	   //IloLinearNumExpr main = cplex.linearNumExpr();
	    	   cplex.addGe(cplex.sum(f[0],f[1],f[2]), 29);
	    	   
	    	   
	    	   cplex.addMinimize(cplex.sum(f[0],f[1],f[2]));
	    	   
	    	   //cplex.addLe(cplex.sum(cplex.negative(x[0]),x[1],x[2]),20.0);
	    	   cplex.solve();
	    	   CplexStatus status = cplex.getCplexStatus();
	    	   //double objval = cplex.getObjValue();
	    	   //System.out.println(objval);
	    	   //System.out.println(status.toString());
	    	   double[]	xval = cplex.getValues(f);
	    	   for(double xv : xval) {
	    		   System.out.println(xv);
	    	   }
	    	   double[] yval = cplex.getValues(u);
	    	   for(double xv : yval) {
	    		   System.out.println(xv);
	    	   }
	       } catch (IloException e) {
	    	   System.err.println("Concert exception caught: " + e); 
	       }


	}

}
