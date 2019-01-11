package msgrsc;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts a {@link List} containing {@link LanguageBundle}s to a table
 * structure containing strings. The first row holds the language codes. 
 * The first column holds the key.  
 */
public class LanguageBundleTable {

	private String[][] table;
	
	public LanguageBundleTable() {}
	
	public void convert(List<LanguageBundle> languageBundles) {
		
		int nrOfBundles = languageBundles.size();
		int maxNumberOfMessages = 0;
		LanguageBundle biggestBundle = null;
		List<LanguageBundle> localList = new ArrayList<>();
		
		for (int i=0; i<nrOfBundles; i++) {
			if (languageBundles.get(i).size() > maxNumberOfMessages) {
				biggestBundle = languageBundles.get(i);
				maxNumberOfMessages = biggestBundle.size();
			}
			localList.add(languageBundles.get(i));
		}
		localList.remove(biggestBundle);
		
		// Include a descriptive row and column (for language codes and keys).
		table = new String[maxNumberOfMessages + 1][languageBundles.size() + 1];
		
		// Fill the first row.
		table[0][0] = "key";
		// First fill the code for the language with the most messages. 
		table[0][1] = biggestBundle.getLanguage().code;
		// Now the other language codes.
		for (int i=0; i<localList.size(); i++) {
			table[0][i + 2] = localList.get(i).getLanguage().code;
		}
		
		int rowIndex = 1;
		for (String key : biggestBundle.getKeySet()) {
			// Fill the first two columns (the key, and the translation that 
			// is definitely there) with the bundle with the most translations.
			table[rowIndex][0] = key;
			table[rowIndex][1] = biggestBundle.getMessage(key);
			
			// Now fill the other columns with the other translations.
			for (int j=0; j<localList.size(); j++) {
				String message = localList.get(j).getMessage(key);
				table[rowIndex][j + 2] = message == null ? "" : message; 
			}
		}
	}
	
	public String getCell(int row, int column) {
		return table[row][column];
	}
	
	public int getNrOfColumns() {
		return table[0].length;
	}
	
	public int getNrOfRows() {
		return table.length;
	}
}
