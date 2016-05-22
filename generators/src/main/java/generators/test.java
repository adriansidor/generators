package generators;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class test {

	public static void main(String[] args) throws IOException {
		int n = 789;
		File all = new File("cost\\all.txt");
		FileWriter fw = new FileWriter(all);
		BufferedWriter bw = new BufferedWriter(fw);
		for(int i = 0; i<n; i++) {
			File file = new File("cost\\cost-risk" + i + ".txt");
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			bw.write(line);
			bw.newLine();
			br.close();
			fr.close();
		}
		bw.close();
		fw.close();

	}

}
