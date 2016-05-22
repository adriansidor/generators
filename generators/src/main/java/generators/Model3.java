package generators;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class Model3 {
	
	private IloCplex cplex = null;
	
	
	private double[] result;

	public Model3(int s, double[][] R, double[] p, double cost, String mode) {
		try {
	    	   cplex = new IloCplex();
	    	   //cplex.setOut(null);
	    	   //dostepne generatory
	    	   int[] dg = {16, 14, 12};
	    	   //liczba typów generatorow
	    	   int T = 3;
	    	   //liczba por dnia
	    	   int d = 5;
	    	   //liczba godzin pracy
	    	   double[] lgp = {6.0, 3.0, 6.0, 3.0, 6.0};
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
	    	   
	    	   //koszt uruchomienia generatorow
	    	   IloNumExpr kug = cplex.numExpr();
	    	   for(int j = 0; j<T; j++) {
	    		   for(int x =0; x<dg[j]; x++) {
	    			   //first kost (pierwszy koszt)
	    			   IloLinearNumExpr fk = cplex.linearNumExpr();
	    			   fk.addTerm(ku[j], G[0][j][x]);
	    			   kug = cplex.sum(kug, fk);
	    			   for(int i = 0; i<d-1; i++) {
	    				   //reszta kosztu
	    				   IloLinearNumExpr rk = cplex.linearNumExpr();
	    				   rk.addTerm(0.5*ku[j], G[i][j][x]);
	    				   rk.addTerm(-0.5*ku[j], G[i+1][j][x]);
	    				   //(|a|-a)/2*ku[j] ,gdzie a[i] = x[i]-x[i+1]
	    				   kug = cplex.sum(kug, cplex.abs(rk));
	    				   kug = cplex.sum(kug, cplex.negative(rk));
	    			   }
	    		   }
	    	   }
	    	   
	    	   //koszt pracy generatorow przy minimalnym obciazeniu
	    	   IloNumExpr kmo = cplex.numExpr();
	    	   for(int i = 0; i<d; i++) {
	    		   for(int j = 0; j<T; j++) {
	    			   for(int x = 0; x<dg[j]; x++) {
	    				   IloLinearNumExpr kpm = cplex.linearNumExpr();
	    				   kpm.addTerm(lgp[i]*km[j], G[i][j][x]);
	    				   kmo = cplex.sum(kmo, kpm);
	    			   }
	    		   }
	    	   }
	    	   
	    	   
	    	   //koszt sredni godziny pracy/MW generatora powyzej minimalnego
			   //obciazenia
	    	   double[] ksp = new double[T];
	    	   for(int j = 0; j<T; j++) {
	    		   for(int r = 0; r<s; r++) {
	    			   ksp[j] += p[r]*R[r][j];
	    			   //System.out.println(R[r][j]);
	    			   //System.out.println(p[r]);
	    			   //System.out.println("ksp[" + j + "]=" + ksp[j]);
	    		   }
	    		   System.out.println("ksp[" + j + "]=" + ksp[j]);
	    	   }
	    	   
	    	   //koszt pracy generatorow powyzej minimalnego obciazenia
	    	   IloNumExpr kpp = cplex.numExpr();
	    	   for(int i = 0; i<d; i++) {
	    		   for(int j = 0; j<T; j++) {
	    			   for(int x = 0; x<dg[j]; x++) {
	    				   //6*(og-omin)*ksp
	    				   IloLinearNumExpr r = cplex.linearNumExpr();
	    				   r.addTerm(lgp[i]*ksp[j], O[i][j][x]);
	    				   r.addTerm(-1.0*lgp[i]*omin[j]*ksp[j], G[i][j][x]);
	    				   kpp = cplex.sum(kpp, r);
	    			   }
	    		   }
	    	   }
	    	   
	    	   //koszt dla roznicy gini
	    	   IloNumExpr all = cplex.numExpr();
	    	   //IloLinearNumExpr[][] op = new IloLinearNumExpr[d][T];
	    	   for(int i = 0; i<s; i++) {
	    		   for(int j = 0; j<s; j++) {
	    	    	   IloNumExpr ls = cplex.numExpr();
	    			   //koszt uruchomienia generatorow
	    			   ls = cplex.sum(ls, kug);
	    			   //koszt pracy przy minimalnym obciazeniu
	    			   ls = cplex.sum(ls, kmo);
	    	    	   IloNumExpr rs = cplex.numExpr();
	    			   rs = cplex.sum(rs, kug);
	    			   rs = cplex.sum(rs, kmo);
	    			   
	    			   for(int a = 0; a<d; a++) {
	    				   for(int b = 0; b<T; b++) {
	    					   //obciazenie powyjej minimalnego
	    	    			   //IloLinearNumExpr op = cplex.linearNumExpr();
	    					   IloLinearNumExpr op = cplex.linearNumExpr();
	    					   for(int x = 0; x<dg[b]; x++) {
	    						   op.addTerm(lgp[a], O[a][b][x]);
	    						   op.addTerm(-1.0*lgp[a]*omin[b], G[a][b][x]);
	    					   }
	    					   ls = cplex.sum(ls, cplex.prod(R[i][b], op));
	    					   rs = cplex.sum(rs, cplex.prod(R[j][b], op));
	    				   }
	    			   }
	    			   //abs = |ls - rs|
	    			   IloNumExpr abs = cplex.abs(cplex.sum(ls, cplex.negative(rs)));
	    			   all = cplex.sum(all, cplex.prod(p[i]*p[j], abs));
	    		   }
	    	   }
	    	   IloNumExpr gini = cplex.prod(0.5, all);
	    	   IloNumExpr tk2 = cplex.sum(kug, kmo, kpp);
	    	   //FUNKCJA CELU
	    	   //double lambda = 0.22;
	    	   //IloNumExpr fc = cplex.sum(tk, cplex.prod(-1*lambda, dr));
	    	   
	    	   //cplex.addMinimize(tk);
	    	   //cplex.addMaximize(tk);
	    	   //cplex.addLe(tk, 1880528.0);
	    	   if(mode.equals("MinC")) {
	    		   cplex.addMinimize(tk2);
	    	   }
	    	   if(mode.equals("MaxC")) {
	    		   cplex.addMaximize(tk2);
	    	   }
	    	   if(mode.equals("Le")) {
	    		   cplex.addLe(tk2, cost);
	    		   cplex.addMinimize(gini);
	    	   }
	    	   if(mode.equals("Eq")){
	    		   cplex.addEq(tk2, cost);
	    		   cplex.addMinimize(gini);
	    	   }
	    	   
	    	   cplex.solve();
	    	   
	    	   if(mode.equals("MinC")) {
	    		   result = new double[1];
	    		   result[0] = cplex.getValue(tk2);
	    	   }
	    	   if(mode.equals("MaxC")) {
	    		   result = new double[1];
	    		   result[0] = cplex.getValue(tk2);
	    	   }
	    	   if(mode.equals("Le")) {
	    		   result = new double[2];
	    		   result[0] = cplex.getValue(tk2);
	    		   result[1] = cplex.getValue(gini);
	    	   }
	    	   if(mode.equals("Eq")){
	    		   result = new double[2];
	    		   result[0] = cplex.getValue(tk2);
	    		   result[1] = cplex.getValue(gini);
	    	   }
	    	   
	    	   //CZY WSZYSTKO SIE ZGADZA
	    	   
	    	   //obciazenie pracujacych generatorow o roznych porach dnia
	    	   System.out.println("Obciazenie pracujacych generatorow");
	    	   for(int i = 0; i<d; i++) {
	    		   System.out.println("opg[" + (i+1) + "]=" + cplex.getValue(opg[i]));
	    	   }
	    	   System.out.println("Czy ma 10% rezerwy?");
	    	   for(int i = 0; i<d; i++) {
	    		   double o = 0;
	    		   for(int j = 0; j<T; j++) {
	    			   double[] g= cplex.getValues(G[i][j]);
	    			   for(int x = 0; x<dg[j]; x++) {
	    				   o += g[x]*omax[j];
	    			   }
	    		   }
	    		   double a = (cplex.getValue(opg[i])/o)*100;
	    		   System.out.println("o[" + (i+1) + "]=" + a + "%");
	    	   }
	    	   
	    	   System.out.println("Tyle samo gen. T1 co T2 i T3?");
	    	   double[][] lg = new double[d][T];
	    	   for(int i = 0; i<d; i++) {
	    		   double[] n = new double[T];
	    		   for(int j = 0; j<T; j++) {
	    			   double[] g= cplex.getValues(G[i][j]);
	    			   for(int x = 0; x<dg[j]; x++) {
	    				   n[j] += g[x];
	    				   
	    			   }
	    		   }
	    		   lg[i] = n;
	    		   System.out.print("T1[" + (i+1) + "]=" + n[0]);
	    		   System.out.print(" T2[" + (i+1) + "]=" + n[1]);
	    		   System.out.println(" T3[" + (i+1) + "]=" + n[2]);
	    	   }
	    	   
	    	   System.out.println("czy zgadza sie koszt uruchomienia gen. ?");
	    	   double sumk = 0;
	    	   for(int j = 0; j<T; j++) {
	    		   for(int x =0; x<dg[j]; x++) {
	    			   sumk += cplex.getValue(G[0][j][x])*ku[j];
	    			   for(int i = 0; i<d-1; i++) {
	    				   double a = cplex.getValue(G[i][j][x]) - cplex.getValue(G[i+1][j][x]);
	    				   double k = (Math.abs(a) - a)*0.5;
	    				   sumk += k*ku[j];
	    			   }
	    		   }
	    	   }
	    	   System.out.println("Wyliczony koszt przez solver = " + cplex.getValue(kug));
	    	   System.out.println("Wyliczony przeze mnie = " + sumk);
	    	   
	    	   System.out.println("Wyliczony koszt pracy przy mini. obc. = " + cplex.getValue(kmo));
	    	   double kmo2 = 0;
	    	   for(int i = 0; i<d; i++) {
	    		   for(int j = 0; j<T; j++) {
	    			   kmo2 += lg[i][j]*km[j]*lgp[i];
	    		   }
	    	   }
	    	   System.out.println("Wyliczony przeze mnie = " + kmo2);
	    	   
	    	   //koszt pracy generatorow powyzej minimalnego obciazenia
	    	   double opm = 0;
	    	   for(int i = 0; i<d; i++) {
	    		   for(int j = 0; j<T; j++) {
	    			   double[] g = cplex.getValues(G[i][j]);
	    			   double[] o = cplex.getValues(O[i][j]);
	    			   for(int x = 0; x<dg[j]; x++) {
	    				   if(o[x] != 0) {
	    					   opm += (o[x]-omin[j])*lgp[i]*ksp[j];
	    				   }
	    			   }
	    		   }
	    	   }
	    	   System.out.println("Wyliczony koszt obc. pow. min. = " + cplex.getValue(kpp));
	    	   System.out.println("Wyliczony przeze mnie koszt = " + opm);
	    	   
	    	   System.out.println("Czy zgadza sie indeks gini?");
	    	   double gmd = 0;
	    	   for(int a = 0; a<s; a++) {
	    		   for(int b = 0; b<s; b++) {
	    			   double ls = 0;
	    			   double rs = 0;
	    			   for(int i = 0; i<d; i++) {
	    				   for(int j = 0; j<T; j++) {
	    					   double[] o = cplex.getValues(O[i][j]);
	    					   double[] g = cplex.getValues(G[i][j]);
	    					   for(int x = 0; x<dg[j]; x++) {
	    						   if(o[x] != 0) {
	    							   double c = (o[x]-omin[j])*lgp[i];
	    							   /*System.out.println("c = " + c);
	    							   System.out.println("Rls = " + R[a][j]);
	    							   System.out.println("cls = " + (c*R[a][j]));
	    							   System.out.println("Rrs = " + R[b][j]);
	    							   System.out.println("crs = " + (c*R[b][j]));*/
	    							   ls += c*R[a][j];
	    							   rs += c*R[b][j];
	    							   /*System.out.println("ls = " + ls);
	    							   System.out.println("rs = " + rs);*/ 
	    						   }
	    					   }
	    				   }
	    			   }
	    			  /* System.out.println("Koncowy ls = " + ls);
	    			   System.out.println("Koncowy rs = " + rs);
	    			   System.out.println("R[" + (a+1) + "]=" + R[a][0] + "," + R[a][1] + "," + R[a][2]);
	    			   System.out.println("R[" + (b+1) + "]=" + R[b][0] + "," + R[b][1] + "," + R[b][2]);*/
	    			   //System.out.println(p[a]);
	    			   //System.out.println(p[b]);
	    			   gmd += Math.abs(ls-rs)*p[a]*p[b];
	    		   }
	    	   }
	    	   gmd = 0.5*gmd;
	    	   System.out.println("Wyliczony przeze mnie gmd = " + gmd);
	    	   
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
