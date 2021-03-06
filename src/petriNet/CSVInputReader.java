package petriNet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class CSVInputReader {

	public HashMap<String, Vector<Double>> readResult(String file, ArrayList<String> columns, boolean omc)
			throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(file));
		ArrayList<String> text = new ArrayList<String>();
		HashMap<String, Integer> columnName = new HashMap<String, Integer>();
		String line = null;
		while ((line = in.readLine()) != null) {
			text.add(line);
		}
		in.close();
		String head = text.get(0);

		// "\" added, because names / labels may contain ","
		String[] headNames = head.split("\"[,;]\"");

		int lines = text.size();
		int cols = headNames.length;
		String name;
		for (int i = 0; i < cols; i++) {
			name = headNames[i];
			if (name.charAt(0) != '"') {
				name = "\"" + name;
			}
			if (name.charAt(name.length() - 1) != '"') {
				name = name + "\"";
			}
			columnName.put(name, i);
		}
		// System.out.println("cols:");
		// System.out.println(columnName);

		String[][] content = new String[lines][cols];

		for (int i = 0; i < lines; i++) {
			content[i] = text.get(i).split("[,;]");
		}

		HashMap<String, Vector<Double>> result = new HashMap<String, Vector<Double>>();
		// System.out.println(lines);
		Vector<Double> v = new Vector<Double>();
		String k = "";
		for (int i = 0; i < columns.size(); i++) {
			if (omc) {
				k = "\"" + columns.get(i) + "\"";
				// System.out.println(k);
			} else {
				k = columns.get(i);
			}
			if (columnName.containsKey(k)) {
				// System.out.println(k+" enthalten");
				v.clear();
				for (int j = 1; j < lines; j++) {
					v.add(Double.parseDouble(content[j][columnName.get(k)]));
				}
				result.put(columns.get(i), (Vector<Double>) v.clone());
				// System.out.println(columns.get(i) + " " +v.size());
			}
			// System.out.println(k+" nicht enthalten");
		}
		// System.out.println(result);
		return result;
	}
}
