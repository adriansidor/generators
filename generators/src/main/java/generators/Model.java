package generators;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class Model {
	
	private IloCplex cplex;
	
	private double[] result;

	public Model(int s, double[][] R, double[] p, double cost, String mode) {
		try {
	    	   cplex = new IloCplex();
	    	   
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
	    	   double[] ku = {2000.0, 1500.0, 1000.0};
	    	   //koszt godziny pracy generatora przy minimalnym obciazeniu
	    	   double[] km = {1000.0, 2500.0, 3200.0};
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
	    			   O[i][j] = cplex.numVarArray(dg[j], 0, Double.MAX_VALUE);
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

	    	   //total k (koszt calkowity)
	    	   //TODO: to jest definicja kosztu a dokladniej wartosc oczekiwana
	    	   //i tak to zmienic
	    	   //definicja kosztu
	    	   IloNumExpr tk = cplex.numExpr();
	    	   
	    	   //definicja ryzyka
	    	   IloNumExpr dr = cplex.numExpr();
	    	   
	    	   //koszt uruchomienia generatorow
	    	   for(int j = 0; j<T; j++) {
	    		   for(int x =0; x<dg[j]; x++) {
	    			   //first kost (pierwszy koszt)
	    			   IloLinearNumExpr fk = cplex.linearNumExpr();
	    			   fk.addTerm(ku[j], G[0][j][x]);
	    			   tk = cplex.sum(tk, fk);
	    			   dr = cplex.sum(dr, fk);
	    			   for(int i = 0; i<d-1; i++) {
	    				   //reszta kosztu
	    				   IloLinearNumExpr rk = cplex.linearNumExpr();
	    				   rk.addTerm(0.5, G[i][j][x]);
	    				   rk.addTerm(-0.5, G[i+1][j][x]);
	    				   tk = cplex.sum(tk, cplex.abs(rk));
	    				   tk = cplex.sum(tk, cplex.negative(rk));
	    				   dr = cplex.sum(dr, cplex.abs(rk));
	    				   dr = cplex.sum(dr, cplex.negative(rk));
	    			   }
	    		   }
	    	   }
	    	   
	    	   //koszt pracy generatorow przy minimalnym obciazeniu
	    	   for(int i = 0; i<d; i++) {
	    		   for(int j = 0; j<T; j++) {
	    			   for(int x = 0; x<dg[j]; x++) {
	    				   IloLinearNumExpr kpm = cplex.linearNumExpr();
	    				   kpm.addTerm(6*km[j], G[i][j][x]);
	    				   tk = cplex.sum(tk, kpm);
	    				   dr = cplex.sum(dr, kpm);
	    			   }
	    		   }
	    	   }
	    	   
	    	   //liczba scenariuszy
	    	   //model uproszczony - 1 scenariusz dla kazdego typu generatora
	    	   //int s = 1;
	    	   //wektor kosztu dla roznych typow generatorow dla roznych scenariuszy
	    	   //double[][] R = new double[T][s];
	    	   //na razie model uproszczony
	    	   //TODO: TU TRZEBA WYGENEROWAC SCENARIUSZE
	    	   /*R[0][0] = 2.5;
	    	   R[1][0] = 1.5;
	    	   R[2][0] = 3.5;*/
	    	   //wektor prawdopodobienstwa dla roznych typow generatorow
	    	   //dla roznych scenariuszy
	    	   //double[] p = new double[s];
	    	   //na razie model uproszczony
	    	   //TODO: TU TRZEBA WYGENEROWAC SCENARIUSZE
	    	   //TODO: p powinno byc raczej tablica jednowymiarowa bo
	    	   //prawdopodobienstwo dla wszystkich R przy danym scenariuszu jest jednakowe
	    	   //p[0] = 1;

	    	   
	    	   //koszt sredni godziny pracy/MW generatora powyzej minimalnego
			   //obciazenia
	    	   double[] ksp = new double[T];
	    	   for(int j = 0; j<T; j++) {
	    		   for(int r = 0; r<s; r++) {
	    			   ksp[j] += p[r]*R[r][j];
	    		   }
	    		   System.out.println("ksp[" + j + "]=" + ksp[j]);
	    	   }
	    	   
	    	   //koszt pracy generatorow powyzej minimalnego obciazenia
	    	   for(int i = 0; i<d; i++) {
	    		   for(int j = 0; j<T; j++) {
	    			   for(int x = 0; x<dg[j]; x++) {
	    				   IloLinearNumExpr r = cplex.linearNumExpr();
	    				   r.addTerm(6*ksp[j], O[i][j][x]);
	    				   r.addTerm(-6*omin[j], G[i][j][x]);
	    				   tk = cplex.sum(tk, r);
	    			   }
	    		   }
	    	   }
	    	   
	    	   //koszt dla roznicy gini
	    	   for(int i = 0; i<s; i++) {
	    		   for(int j = 0; j<s; j++) {
	    			   IloLinearNumExpr ls = cplex.linearNumExpr();
	    			   IloLinearNumExpr rs = cplex.linearNumExpr();
	    			   for(int a = 0; a<d; a++) {
	    				   for(int b = 0; b<T; b++) {
	    					   for(int x = 0; x<dg[b]; x++) {
	    						   ls.addTerm(0.5*6*R[i][b], O[a][b][x]);
	    						   ls.addTerm(-0.5*6*omin[b], G[a][b][x]);
	    						   rs.addTerm(0.5*6*R[j][b], O[a][b][x]);
	    						   rs.addTerm(-0.5*6*omin[b], G[a][b][x]);
	    					   }
	    				   }
	    			   }
	    			   IloNumExpr diff = cplex.numExpr();
	    			   diff = cplex.sum(ls, cplex.negative(rs));
	    			   IloNumExpr abs = cplex.linearNumExpr();
	    			   abs = cplex.abs(diff);
	    			   dr = cplex.sum(dr, cplex.prod(p[i]*p[j], abs));
	    		   }
	    	   }
	    	   
	    	   //FUNKCJA CELU
	    	   //double lambda = 0.22;
	    	   //IloNumExpr fc = cplex.sum(tk, cplex.prod(-1*lambda, dr));
	    	   
	    	   //cplex.addMinimize(tk);
	    	   //cplex.addMaximize(tk);
	    	   //cplex.addLe(tk, 1880528.0);
	    	   if(mode.equals("MinC")) {
	    		   cplex.addMinimize(tk);
	    	   }
	    	   if(mode.equals("MaxC")) {
	    		   cplex.addMaximize(tk);
	    	   }
	    	   if(mode.equals("Le")) {
	    		   cplex.addLe(tk, cost);
	    		   cplex.addMinimize(dr);
	    	   }
	    	   if(mode.equals("Eq")){
	    		   cplex.addEq(tk, cost);
	    		   cplex.addMinimize(dr);
	    	   }
	    	   
	    	   cplex.solve();
	    	   
	    	   if(mode.equals("MinC")) {
	    		   result = new double[1];
	    		   result[0] = cplex.getValue(tk);
	    	   }
	    	   if(mode.equals("MaxC")) {
	    		   result = new double[1];
	    		   result[0] = cplex.getValue(tk);
	    	   }
	    	   if(mode.equals("Le")) {
	    		   result = new double[2];
	    		   result[0] = cplex.getValue(tk);
	    		   result[1] = cplex.getValue(dr);
	    	   }
	    	   if(mode.equals("Eq")){
	    		   result = new double[2];
	    		   result[0] = cplex.getValue(tk);
	    		   result[1] = cplex.getValue(dr);
	    	   }
	    	   //cplex.addLe(tk, 6806000.0);
	    	   //cplex.addEq(tk, 6806000.0);
	    	   
	    	   //cplex.addMaximize(dr);
	    	   //cplex.addMinimize(fc);
	    	   //cplex.solve();
	    	   //CplexStatus status = cplex.getCplexStatus();
	    	   
	    	   //System.out.println("Koszt sredni = " + cplex.getValue(tk));
	    	   //System.out.println("Ryzyko = " + cplex.getValue(dr));
	    	   
/*	    	   for(int i = 0; i<d; i++) {
	    		   System.out.println("Dla pory " + i);
	    		   //generowany prad przez generatory
 			   double gp = 0;
	    		   for(int j = 0; j<T; j++) {
	    			   System.out.println("Generator typu " + j);
	    			   double[]	g = cplex.getValues(G[i][j]);
	    			   double[]	o = cplex.getValues(O[i][j]);
	    			   for(int x = 0; x<dg[j]; x++) {
	    				   StringBuilder sb = new StringBuilder();
	    				   sb.append("G[").append(i).append("]");
	    				   sb.append("[").append(j).append("]");
	    				   sb.append("[").append(x).append("]");
	    				   sb.append("=").append(g[x]).append(";");
	    				   sb.append(o[x]);
	    				   //System.out.println(sb.toString());
	    				   
	    				   gp = gp + o[x];
	    			   }
	    		   }
	    		   System.out.println("Generowany prad przez generatory = " + gp);
	    	   }*/
	    	   
	    	   /*double[]	xval = cplex.getValues(xy);
	    	   for(double xv : xval) {
	    		   System.out.println(xv);
	    	   }*/
	    	   
	    	   
	    	  /* IloNumVar[] x = cplex.numVarArray(3, 0.0, 100.0, IloNumVarType.Int);
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
	    	   cplex.addLe(f[2], bexpr3);*/
	    	   
	    	   //IloLinearNumExpr main = cplex.linearNumExpr();
	    	   //cplex.addGe(cplex.sum(f[0],f[1],f[2]), 29);
	    	   
	    	   //poczatek przykladu
	    	   //IloNumVar[] xy = cplex.boolVarArray(5);
	    	   //IloNumVar[] z = cplex.boolVarArray(4);
	    	   
	    	   /*IloNumVar[] z = cplex.numVarArray(4, 0, Double.MAX_VALUE);
	    	   for(int i = 0; i<z.length; i++) {
	    		   IloLinearNumExpr expr1 = cplex.linearNumExpr();
		    	   expr1.addTerm(1.0, xy[i]);
		    	   expr1.addTerm(-1.0, xy[i+1]);
		    	   cplex.addLe(z[i], expr1);
		    	   //cplex.addGe(z[i], expr1);
		    	   IloLinearNumExpr e2 = cplex.linearNumExpr();
		    	   e2.addTerm(-1.0, xy[i]);
		    	   e2.addTerm(1.0, xy[i+1]);
		    	   cplex.addLe(z[i], e2);
		    	   //cplex.addGe(z[i],e2);
	    	   }*/
	    	  /* IloLinearNumExpr s = cplex.linearNumExpr();
	    	   for(int i = 0; i<z.length; i++) {
	    		   s.addTerm(0.5, z[0]);
	    		   //IloLinearNumExpr ai = cplex.linearNumExpr();
	    		   s.addTerm(-0.5, xy[i]);
	    		   s.addTerm(0.5, xy[i+1]);
	    	   }*/
	    	   
	    	   /*IloLinearNumExpr s = cplex.linearNumExpr();
	    	   for(int i = 0; i<z.length; i++) {
	    		   //s.addTerm(0.5, z[0]);
	    		   IloLinearNumExpr ai = cplex.linearNumExpr();
	    		   ai.addTerm(0.5, xy[i]);
	    		   ai.addTerm(-0.5, xy[i+1]);
	    		   //s.add((IloLinearNumExpr) cplex.abs(ai));
	    		   s.addTerm(-0.5, xy[i]);
	    		   s.addTerm(0.5, xy[i+1]);
	    	   }*/

	    	   //cplex.addEq(xy[0], 1);
	    	   //cplex.addEq(xy[1], 1);
	    	   //cplex.addEq(xy[2], 0);
	    	   //cplex.addEq(xy[3], 1);
	    	   //cplex.addEq(xy[4], 1);
	    	   /*IloLinearNumExpr expr = cplex.linearNumExpr();
	    	   expr.addTerm(1.0, xy[0]);
	    	   expr.addTerm(-1.0, xy[1]);
	    	   cplex.addLe(z[0], expr);*/
	    	   //IloLinearNumExpr exp = cplex.linearNumExpr();
	    	   /*IloNumVar h = cplex.numVar(-10, 2);
	    	   IloNumVar g = cplex.numVar(5, 40);
	    	   IloNumVar j = cplex.numVar(0, Double.MAX_VALUE);
	    	   IloLinearNumExpr je1 = cplex.linearNumExpr();
	    	   je1.addTerm(1.0, h);
	    	   je1.addTerm(-1.0, g);
	    	   cplex.addLe(j, je1);
	    	   //cplex.addLe(je1, j);
	    	   IloLinearNumExpr je2 = cplex.linearNumExpr();
	    	   je2.addTerm(-1.0, h);
	    	   je2.addTerm(1.0, g);
	    	   cplex.addLe(j, cplex.negative(je1));
	    	   cplex.addGe(j, 0);
	    	   IloNumVar hminus = cplex.numVar(0, Double.MAX_VALUE);
	    	   IloNumVar hplus = cplex.numVar(0, Double.MAX_VALUE);
	    	   IloLinearNumExpr su = cplex.linearNumExpr();
	    	   su.addTerm(1.0, hplus);
	    	   su.addTerm(-1, hminus);
	    	   cplex.addEq(h, su);
	    	   IloLinearNumExpr pw = cplex.linearNumExpr();
	    	   pw.addTerm(3, h);
	    	   pw.addTerm(1, g);
	    	   //cplex.addLe(cplex.negative(je1), j);
	    	   //cplex.addRange(je1, j, je1);
	    	   System.out.println("j: " + j);*/
	    	   //System.out.println(cplex);
	    	   //cplex.addMaximize(pw);
	    	   //koniec przykladu
	    	   
	    	   //cplex.addLe(cplex.sum(cplex.negative(x[0]),x[1],x[2]),20.0);
	    	   //cplex.solve();
	    	   //CplexStatus status = cplex.getCplexStatus();
	    	   //System.out.println(cplex.getValue(pw));
	    	   //double objval = cplex.getObjValue();
	    	   //System.out.println(objval);
	    	   //System.out.println(status.toString());
	    	   //System.out.println(s);
	    	   //double v = cplex.getValue(s);
	    	   //System.out.println("value: " + v);
	    	   /*double[]	xval = cplex.getValues(xy);
	    	   for(double xv : xval) {
	    		   System.out.println(xv);
	    	   }*/
	    	   /*System.out.println("z");
	    	   double[] yval = cplex.getValues(z);
	    	   for(double xv : yval) {
	    		   System.out.println(xv);
	    	   }*/
	    	   /*double[]	xval = cplex.getValues(f);
	    	   for(double xv : xval) {
	    		   System.out.println(xv);
	    	   }
	    	   double[] yval = cplex.getValues(u);
	    	   for(double xv : yval) {
	    		   System.out.println(xv);
	    	   }*/
	       } catch (IloException e) {
	    	   System.err.println("Concert exception caught: " + e); 
	       }

	}
	
	public double[] solve(int n) throws IloException {
		cplex.solve();
		double[] result = new double[2];
		
		return result;
	}
	
	public double[] getResult() {
		return result;
	}
}
