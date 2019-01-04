package msgrsc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class LanguageBundleBuilder {

	private List<LanguageBundle> languageBundles;
	
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
		
		// List the files in the given directory, and build a bundle from each one.

		return true;
	}
	
	public boolean buildFromExcel(String importFile) {
		try {
			Workbook workbook = WorkbookFactory.create(new File(importFile));
			Sheet msgRscSheet = workbook.getSheetAt(0);
			
			Row topRow = msgRscSheet.getRow(0);
			
			
			
		} catch (EncryptedDocumentException | IOException e) {
			e.printStackTrace();
			return false;
		}
		
		
		return true;
	}

	private boolean readFromCsv(String importFileString, String msgRscDirString) {
		
		LanguageBundle german = new LanguageBundle("de", msgRscDirString);
		LanguageBundle french = new LanguageBundle("fr", msgRscDirString);
		LanguageBundle flemish = new LanguageBundle("vls", msgRscDirString);
		LanguageBundle norwegian = new LanguageBundle("nb", msgRscDirString);

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
