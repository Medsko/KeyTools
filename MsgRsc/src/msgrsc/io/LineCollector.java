package msgrsc.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Collects all lines from a given file that test positive for the specified condition. 
 */
public class LineCollector {

	private List<String> lines;
	
	public boolean collect(String file, Predicate<String> condition) {
		
		lines = new ArrayList<>();
		
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			
			String line;
			while ((line = reader.readLine()) != null) {
				if (condition.test(line)) {
					// This line tests positive for whatever affliction the 
					// caller has specified. Collect it.
					lines.add(line);
				}
			}
		} catch (IOException ioex) {
			ioex.printStackTrace();
			return false;
		}
		
		return true;
	}

	public List<String> getLines() {
		return lines;
	}
}
