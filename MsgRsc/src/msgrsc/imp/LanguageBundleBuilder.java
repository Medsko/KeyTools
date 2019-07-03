package msgrsc.imp;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import msgrsc.craplog.Fallible;

/**
 * This is a bad class. It doesn't really do anything useful, except a for each over the files
 * for the different languages. A {@link MsgRscExcelReader} does all the heavy lifting.
 */
public class LanguageBundleBuilder implements Fallible {

	private LanguageBundle languageBundle;
	
	private String bugNumber;
	
	public LanguageBundleBuilder(String bugNumber) {
		this.bugNumber = bugNumber;
	}
	
	public boolean buildFromExcelDirectory(String importDir) {
		// List the Excel files in the given directory, and build a bundle from each one.
		try {
			// Strip the 'QSD'/'QSD-' from the bug number to make matching easier.
			String bareBugNumber = bugNumber.replaceAll("[QqSsDd-]", "");
			DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get(importDir), "*.{xls,xlsx}");
			MsgRscExcelReader reader = new MsgRscExcelReader(bareBugNumber);
			for (Path importFile : ds) {
				// Use the MsgRscExcelReader to create a language bundle from the import file.
				reader.buildFromFile(importFile.toString());
			}
			languageBundle = reader.getLanguageBundle();
			
			if (languageBundle == null) {
				log.log("buildFromExcelDirectory - no excel files found in import directory!");
				informer.informUser("No excel files were found in directory " + importDir + ". "
						+ "Please put all excel files with translations in that directory, or "
						+ "provide the correct bug number.");
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	public LanguageBundle getLanguageBundle() {
		return languageBundle;
	}	
}
