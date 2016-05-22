package generators;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.math3.distribution.MultivariateNormalDistribution;

public class Main2 {

	public static void main(String[] args) throws IOException {
		double[] means = {2.5, 1.5, 3.5};
		double[][] covariances = { {1.0, -2.0, -1.0}, {-2.0, 25.0, -8.0}, {-1.0, -8.0, 9.0} };
		MultivariateNormalDistribution mnd = new MultivariateNormalDistribution(means, covariances);
		int s = 10;

		double[][] R = new double[s][3];
		double[] p = new double[s];
		int x = 0;
		File r = new File("cost\\r.txt");
		
/*		FileWriter fwr = new FileWriter(r);
		BufferedWriter bwr = new BufferedWriter(fwr);
		while(x < s) {
			double[] sample = mnd.sample();
			boolean flag = true;
			for(int j = 0; j<sample.length; j++) {
				if(sample[j] < 1.0) {
					flag = false;
				} else if (sample[j] > 5.0) {
					flag = false;
				}
			}
			if(flag){
				R[x] = sample;
				System.out.println("R[" + x + "]=" + R[x][0] + "," + R[x][1] + "," + R[x][2]);
				bwr.write("R[" + x + "]=" + R[x][0] + "," + R[x][1] + "," + R[x][2]);
				bwr.newLine();
				p[x] = 1d/s;
				x++;
			}
		}
		bwr.close();
		fwr.close();*/
		
		//load r
		FileReader frw = new FileReader(r);
		BufferedReader brr = new BufferedReader(frw);
		for(int i = 0; i<s; i++) {
			String line = brr.readLine();
			int index = line.indexOf("=");
			line = line.substring(index+1);
			String[] cost = line.split(",");
			R[i][0] = Double.valueOf(cost[0]);
			R[i][1] = Double.valueOf(cost[1]);
			R[i][2] = Double.valueOf(cost[2]);
			p[i] = 1d/s;
		}
			
		Model3 model1 = new Model3(s, R, p, 2, "MinC");
		double minc = model1.getResult()[0];
		System.out.println("Minimum cost = " + minc);
		Model3 model2 = new Model3(s, R, p, 2, "MaxC");
		double maxc = model2.getResult()[0];
		System.out.println("Maximum cost = " + maxc);
		//File file = new File("cost-risk.txt");
		/*FileWriter fw = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fw);*/
		int n = (int)((maxc - minc)/1000);
		System.out.println("N = " + n);
		double delta = (maxc - minc)/n;
		System.out.println("Delta=" + delta);
		//Model3[] modela = new Model3[n];
		int offset = 2475;
		for(int i = (0+offset); i<n; i++) {
			File file = new File("cost\\cost-risk" + i + ".txt");
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			double cost = minc+delta*i;
			System.out.println("MinC=" + minc);
			System.out.println("MaxC=" + maxc);
			System.out.println("Cost=" + cost);
			//modela[i] = new Model3(s, R, p, cost, "Le");
			Model3 modela = new Model3(s, R, p, cost, "Le");;
			//double[] result = modela[i].getResult();
			double[] result = modela.getResult();
			bw.write(String.valueOf(result[0]));
			bw.write(";");
			bw.write(String.valueOf(result[1]));
			bw.newLine();
			bw.close();
			fw.close();
		}

		System.out.println("Finished");
		
		
	}

}
