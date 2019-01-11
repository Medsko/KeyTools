package msgrsc;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class LanguageBundleBuilder {

	private List<LanguageBundle> languageBundles;
	
	private String bugNumber;
	
	public LanguageBundleBuilder(String bugNumber) {
		this.bugNumber = bugNumber;
	}
	
	public boolean buildLanguageBundles(String importFileString, String msgRscDirString) {

		if (importFileString.endsWith(".csv")) {
			readFromCsv(importFileString, msgRscDirString);
		} else if (importFileString.endsWith(".xlsx")) {
			
		} else if (importFileString.endsWith(".xls")) {
			// Old school excel file.
			
		} else {
			throw new UnsupportedOperationException("File extension not supported!");
		}
		return true;
	}
	
	public boolean buildFromExcelDirectory(String importDir) {
		
		languageBundles = new ArrayList<>();
		// List the files in the given directory, and build a bundle from each one.
		try {
			
			DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get(importDir), "*.{xls,xlsx}");
			MsgRscExcelReader reader = new MsgRscExcelReader(bugNumber);
			for (Path importFile : ds) {
				// Use the MsgRscExcelReader to create a language bundle from the import file.
				reader.buildFromFile(importFile.toString());
				languageBundles.add(reader.getLanguageBundle());
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}	

	private boolean readFromCsv(String importFileString, String msgRscDirString) {
		
		LanguageBundle german = new LanguageBundle("de");
		LanguageBundle french = new LanguageBundle("fr");
		LanguageBundle flemish = new LanguageBundle("vls");
		LanguageBundle norwegian = new LanguageBundle("nb");

		languageBundles.add(german);
		languageBundles.add(french);
		languageBundles.add(flemish);
		languageBundles.add(norwegian);
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(importFileString)))) {
			
			String line;
			
			while ((line = reader.readLine()) != null) {
				String[] fields = line.split(";");
				
				if (fields.length != 7) {
					System.out.println("Wrong number of columns!");
					continue;
				}
				
				String key = fields[0];
				String germanMsg = fields[3];
				String frenchMsg = fields[4];
				String flemishMsg = fields[5];
				String norwegianMsg = fields[6];
				
				german.addMessage(key, germanMsg);
				french.addMessage(key, frenchMsg);
				flemish.addMessage(key, flemishMsg);
				norwegian.addMessage(key, norwegianMsg);
			}
		} catch (IOException ioex) {
			ioex.printStackTrace();
			return false;
		}
		return true;
	}

	
	public List<LanguageBundle> getLanguageBundles() {
		return languageBundles;
	}
}
