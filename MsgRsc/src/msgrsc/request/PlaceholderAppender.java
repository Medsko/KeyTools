package msgrsc.request;

import java.io.IOException;
import java.util.List;

import msgrsc.craplog.Fallible;
import msgrsc.dao.MsgRscDir;
import msgrsc.dao.MsgRscFile;
import msgrsc.io.AppendingFileWriter;
import msgrsc.utils.IOUtils;
import msgrsc.utils.Language;

/**
 * Performs a full scan of all MessageResource and InformationMessageResource files,
 * appending placeholders in foreign language files that are missing message keys
 * that are present in the English files.
 */
public class PlaceholderAppender implements Fallible {

	private String bugNumber;
		
	/** Total number of missing message resources found so far. */
	private int total;
	
	public PlaceholderAppender(String bugNumber) {
		this.bugNumber = bugNumber;
		total = 0;
	}

	public boolean appendPlaceHolders(String aggregateDir) {
		// Find all message resource directories for the specified branch.
		List<MsgRscDir> mrDirs = IOUtils.findMesResDirs(aggregateDir);
		
		for (MsgRscDir mrDir : mrDirs) {
			if (!appendPlaceHolders(mrDir)) {
				logger.error("Failed while processing directory: " 
						+ mrDir.getPath());
			}
		}
		
		return true;
	}
	
	public boolean appendPlaceHolders(MsgRscDir mrDir) {
		// Find all missing resources in the directory.
		MrDirectoryScanner scanner = new MrDirectoryScanner(mrDir);
		if (!scanner.scan()) {
			logger.log("buildFromFullScan - scanner failed.");
			return false;
		}
		
		// Add the missing resources to the translation request.
		for (MsgRscFile.Type type : MsgRscFile.Type.values()) {
			for (MsgRscFile file : mrDir.getFiles(type)) {
				
				if (file.getLanguage() == Language.DUTCH 
						|| file.getLanguage() == Language.ENGLISH) {
					// Skip Dutch and English files. 
					continue;
				}
				
				if (!file.containsMissingMessages()) {
					// This file contains no missing messages.
					continue;
				}				
				// Rewrite the files with missing messages, adding place holders for
				// the missing message keys.
				// For now, append all message keys for missing resources to the end
				// of the file, and let IntelliJ sort them out.
				if (!appendPlaceHolders(file)) {
					logger.log("buildFromFullScan - writing the placeholders failed.");
					return false;
				}
			}
		}
		
		return true;
	}

	/**
	 * Appends placeholders for missing translations in the given message resource file.
	 * 
	 * @param file - the {@link MsgRscFile} object specifying which messages are missing
	 * and in which file placeholders should be appended.
	 */
	public boolean appendPlaceHolders(MsgRscFile file) {
		try (AppendingFileWriter writer = new AppendingFileWriter(file.getFullPath())) {
			for (String missingKey : file.getMissingMessageKeys()) {
				// Compose the placeholder.
				String placeHolderLine = missingKey; 
				placeHolderLine += "=toBeTranslatedFor" + file.getLanguage().getPrettyCode();
				placeHolderLine += "In" + bugNumber;
				
				logger.log("Appended placeholder " + placeHolderLine 
					+ " to file " + file.getFullPath());
				total++;
				
				// Append the place holder to the line.
				writer.writeLine(placeHolderLine);
			}
			logger.log("Total number of missing translations found so far: " + total);
		} catch (IOException ioex) {
			ioex.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Resets the counter for the number of missing translations found.  
	 */
	public void reset() {
		total = 0;
	}
	
}
