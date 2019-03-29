package msgrsc.imp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import msgrsc.dao.MsgRscDir;
import msgrsc.dao.MsgRscFile;
import msgrsc.filewalk.MsgRscFileFinder;
import msgrsc.io.LineRewriter;
import msgrsc.io.MrCoupler;
import msgrsc.io.MrFileReader;
import msgrsc.io.MrKeyCoupler;
import msgrsc.io.StringModifier;
import msgrsc.utils.Fallible;

/**
 * Imports the translations provided by the translation bureaus or Keylane colleagues.
 * These are typically provided in Excel sheet format, so a {@link LanguageBundleBuilder}
 * is used to read in the translations and...build a {@link LanguageBundle} from them (wow).
 */
public class MrImporter implements Fallible {
	
	public final static String IMPORT_DIRECTORY = "C:/MsgRscImport";
	
	private LanguageBundle languageBundle;
	
	private List<String> successFullyUpdatedFiles;
	
	private List<String> failedFiles;
	
	private boolean isDebugMode;
	
	/** 
	 * If {@code true}, the provided translations are matched based on their message key.
	 * Otherwise, the bug number is used for this, i.e. only messages that have a placeholder
	 * are filled. 
	 */
	private boolean findByMessageKey;
	
	private String bareBugNumber;
	
	public MrImporter() {
		this(false);
	}
	
	public MrImporter(boolean isDebugMode) {
		this.isDebugMode = isDebugMode;
		findByMessageKey = false;
	}
	
	// TODO: big one, actually...link provided translations to a particular MR file and directory, 
	// TODO: resulting in a smaller 'LanguageBundle' (new class) per MR file/directory? probably file.
	
	/**
	 * Imports the message resources for the given bug number into the project located at
	 * the given aggregate directory.
	 * <p>
	 * NB: the bugNumber parameter will also be used to determine the directory from 
	 * which the Excel files will be read: e.g. for bug QSD-12345, this 
	 * {@link MrImporter} will try to read all Excel files present in the 
	 * C:\MsgRscImport\QSD-12345 directory. If this directory does not exist, processing
	 * will fail. If no Excel files have been put into this directory,  processing will 
	 * be done really quick...
	 * </p>
	 * 
	 * @param bugNumber - the <strong>full</strong> bug number (so including 'QSD-') for 
	 * which the translations have been requested.
	 * @param aggregateDir - the aggregate directory of the Axon workspace that the 
	 * translations should be transported into. 
	 */
	public boolean importMR(String bugNumber, String aggregateDir) {
		
		if (!isInputValid(bugNumber, aggregateDir)) {
			return false;
		}
		bareBugNumber = bugNumber.replaceAll("QqSsDd-", "");

		LanguageBundleBuilder builder = new LanguageBundleBuilder(bugNumber);
		String importDir = IMPORT_DIRECTORY + "/" + bugNumber;
		if (!builder.buildFromExcelDirectory(importDir)) {
			logger.log("MsgRscImporter - importMR - Failed to construct the language"
					+ " bundles from the Excel files in the import directory.");
			return false;
		}
		
		languageBundle = builder.getLanguageBundle();
		if (languageBundle.isEmpty()) {
			logger.log("MsgRscImporter-importMR - no translations found in directory " 
					+ importDir + " for bug number " + bugNumber);
			return true;
		}
		
		try {
			// Determine the directories and files that contain references to the
			// bug number for which translations have been read from the import file.
			// TODO: first, try to determine the directories and files that should be modified
			// from the TranslationRequest for the bug.
			MsgRscFileFinder finder = determineFinder();
			Files.walkFileTree(Paths.get(aggregateDir), finder);
			List<MsgRscDir> mrDirectories = finder.getMsgRscDirectories();
			
			// Prepare some useful variables.
			LineRewriter rewriter = new LineRewriter();
			rewriter.setModifier(determineStringModifier());
			
			successFullyUpdatedFiles = new ArrayList<>();
			failedFiles = new ArrayList<>();
			
			// Rewrite the files, filling all provided translations.
			for (MsgRscDir mrDir : mrDirectories) {
				for (MsgRscFile mrFile : mrDir.getMsgRscFiles()) {
					// Determine which language's translations we're interested in for
					// this file and set it as active it on the language bundle.
					languageBundle.setActiveLanguage(mrFile.getLanguage());
					
					// Do not let the rewriter replace the original file, as we want to
					// do that in batch (or not) in case of success (or failure).
					if (!rewriter.rewrite(mrFile.getFullPath(), false)) {
						logger.log("importMR - failed while importing to file " 
								+ mrFile.getFullPath());
						failedFiles.add(mrFile.getFullPath());
					} else {
						successFullyUpdatedFiles.add(mrFile.getFullPath());
					}
				}
			}
			
			if (isDebugMode) {
				// This is a test run. Do not overwrite the actual files yet, as the results of the
				// rewrite might yet prove horrifying.
				return true;
			}
			
			// All message resource files were processed. Overwrite the original files
			// with the rewritten files.
			for (String updatedFile : successFullyUpdatedFiles) {
				Path target = Paths.get(updatedFile);
				Path source = target.getParent().resolve(Paths.get(determineTempFileName(updatedFile)));
				Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
			}
			
			// Delete any temporary files for which the rewrite failed.
			for (String failedFile : failedFiles) {
				Path failedFilePath = Paths.get(failedFile);
				Path failedTempFilePath = failedFilePath.resolve(determineTempFileName(failedFile));
				if (!Files.deleteIfExists(failedTempFilePath)) {
					logger.log("importMR - failed to delete temporary file " + failedTempFilePath);
					return false;
				}
			}
			
		} catch (IOException ioex) {
			ioex.printStackTrace();
			return false;
		}
		return true;
	}
	
	private StringModifier determineStringModifier() {
		if (findByMessageKey)
			return new MrKeyCoupler(languageBundle);
		else
			return new MrCoupler(languageBundle);
	}
	
	private MsgRscFileFinder determineFinder() {
		if (findByMessageKey) {
			return new MsgRscFileFinder() {
				@Override
				protected boolean processMRFile(MsgRscFile mrFile) {
					MrFileReader reader = new MrFileReader();
					if (!reader.readFile(mrFile.getFullPath())) {
						logger.log("processMRFile - failed to read file! " + mrFile.getFullPath());
						return false;
					}
					
					while (reader.next()) {
						if (languageBundle.containsKey(reader.getKey())) {
							return true;
						}
					}
					return false;
				}
			};
		} else {
			return new MsgRscFileFinder() {
				@Override
				protected boolean processMRFile(MsgRscFile mrFile) {
					MrFileReader reader = new MrFileReader();
					if (!reader.readFile(mrFile.getFullPath())) {
						logger.log("processMRFile - failed to read file! " + mrFile.getFullPath());
						return false;
					}
					
					while (reader.next()) {
						if (reader.getMessage().contains(bareBugNumber)) {
							return true;
						}
					}
					return false;
				}
			};
		}
	}
	
	/**
	 * Performs checks on the input to verify it is valid. 
	 */
	private boolean isInputValid(String bugNumber, String aggregateDir) {
		if (bugNumber == null || aggregateDir == null) {
			throw new IllegalArgumentException("No null values allowed for parameters to MrImporter.importMR()!!");
		}
		
		// Bug number should follow format 'QSD-12345' (hyphen may be omitted).
		if (!bugNumber.matches("QSD-?\\d{5}")) {
			logger.log("MrImporter.isInputValid - input bug number not valid!");
			return false;
		}

		String importDir = IMPORT_DIRECTORY + "/" + bugNumber;
		if (!Files.isDirectory(Paths.get(importDir))) {
			logger.log("MrImporter.isInputValid - import directory for given bug number does not exist!");
			return false;
		}
		
		if (!Files.isDirectory(Paths.get(aggregateDir))) {
			logger.log("MrImporter.isInputValid - input aggregate/project directory does not exist!");
			return false;
		}

		return true;
	}
	
	private String determineTempFileName(String fileName) {
		String tempFileName = Paths.get(fileName).getFileName().toString();
		tempFileName = tempFileName.substring(0, tempFileName.indexOf('.')) + "temp.properties";
		return tempFileName;
	}

	public void setFindByMessageKey(boolean findByMessageKey) {
		this.findByMessageKey = findByMessageKey;
	}
}
