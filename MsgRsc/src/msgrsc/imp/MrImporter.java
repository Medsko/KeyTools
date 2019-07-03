package msgrsc.imp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import msgrsc.craplog.Fallible;
import msgrsc.dao.MsgRscDir;
import msgrsc.dao.MsgRscFile;
import msgrsc.find.MsgRscFileFinder;
import msgrsc.io.LineRewriter;
import msgrsc.io.MrCoupler;
import msgrsc.io.MrFileReader;
import msgrsc.io.MrKeyCoupler;
import msgrsc.io.StringModifier;
import msgrsc.utils.IOUtils;

/**
 * Imports the translations provided by the translation bureaus or Keylane colleagues.
 * These are typically provided in Excel sheet format, so a {@link LanguageBundleBuilder}
 * is used to read in the translations and...build a {@link LanguageBundle} from them (wow).
 */
public class MrImporter implements Fallible {
	
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
	
	/**
	 * Imports the message resources for the given bug number into the project located at
	 * the given aggregate directory.
	 * <p>
	 * NB: the bugNumber parameter will also be used to determine the directory from 
	 * which the Excel files will be read: e.g. for bug QSD-12345, this 
	 * {@link MrImporter} will try to read all Excel files present in the 
	 * C:\MsgRsc\QSD-12345 directory. If this directory does not exist, processing
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
		bareBugNumber = bugNumber.replaceAll("[QqSsDd-]", "");

		// TODO: this doesn't belong here. LanguageBundle should be constructed elsewhere,
		// then set on the MrImporter.
		LanguageBundleBuilder builder = new LanguageBundleBuilder(bugNumber);
		String importDir = IOUtils.MR_IMPORT_PATH + bugNumber;
		if (!builder.buildFromExcelDirectory(importDir)) {
			log.log("MsgRscImporter - importMR - Failed to construct the language"
					+ " bundles from the Excel files in the import directory.");
			return false;
		}
		
		languageBundle = builder.getLanguageBundle();
		if (languageBundle.isEmpty()) {
			log.log("MsgRscImporter-importMR - no translations found in directory " 
					+ importDir + " for bug number " + bugNumber);
			return true;
		}
		
		try {
			// Determine the directories and files that contain references to the
			// bug number for which translations have been read from the import file.
			// TODO: this should probably also be done somewhere else.
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
				// Combine the information- and messageResources files.
				List<MsgRscFile> infoAndMrFiles = new ArrayList<>();
				infoAndMrFiles.addAll(mrDir.getMsgRscFiles());
				infoAndMrFiles.addAll(mrDir.getInfoFiles());
				
				for (MsgRscFile mrFile : infoAndMrFiles) {
					// Determine which language's translations we're interested in for
					// this file and set it as active it on the language bundle.
					languageBundle.setActiveLanguage(mrFile.getLanguage());
					
					// Do not let the rewriter replace the original file, as we want to
					// do that in batch (or not) in case of success (or failure).
					if (!rewriter.rewrite(mrFile.getFullPath(), false)) {
						log.log("importMR - failed while importing to file " 
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
					log.log("importMR - failed to delete temporary file " + failedTempFilePath);
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
			log.log("MrImporter.isInputValid - input bug number not valid!");
			return false;
		}

		String importDir = IOUtils.MR_IMPORT_PATH + bugNumber;
		if (!Files.isDirectory(Paths.get(importDir))) {
			log.log("MrImporter.isInputValid - import directory for given bug number does not exist!");
			return false;
		}
		
		if (!Files.isDirectory(Paths.get(aggregateDir))) {
			log.log("MrImporter.isInputValid - input aggregate/project directory does not exist!");
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

	public LanguageBundle getLanguageBundle() {
		return languageBundle;
	}
}
