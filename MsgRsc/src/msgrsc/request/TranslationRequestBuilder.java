package msgrsc.request;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import msgrsc.craplog.Fallible;
import msgrsc.dao.DbDir;
import msgrsc.dao.DbFile;
import msgrsc.dao.DbTranslation;
import msgrsc.dao.LiquibaseElement;
import msgrsc.dao.MsgRscDir;
import msgrsc.find.MsgRscFileFinder;
import msgrsc.io.LiquibaseFileReader;
import msgrsc.io.MrFileReader;
import msgrsc.utils.IOUtils;
import msgrsc.utils.Language;
import msgrsc.utils.TranslationToolHelper;

/**
 * This somewhat overweight builder can create a {@link TranslationRequest} from a given bug number.
 * <p>
 * A scan by bug number:
 * <ul>
 * 	<li>Determines all message resource files and scans them for occurrences of the given 
 * 		bug number.
 * 	</li>
 * 	<li>Keeps track of all such occurrences and eventually writes them to a CSV file.</li>
 * </ul>
 */
public class TranslationRequestBuilder implements Fallible {

	private Language activeLanguage;
	
	private List<Path> dutchAndEnglishMsgRscFiles;
	
	private TranslationToolHelper specialHelper;
		
	private final static String PATTERN_BUG_NUMBER = "QSD-\\d{5}";
	
	private String bugNumber;
	
	// OUTPUT
	/** The request for translations for the message resources tagged with the given bug number. */
	private TranslationRequest translationRequest;
	
	public TranslationRequestBuilder() {
		dutchAndEnglishMsgRscFiles = new ArrayList<>();
		specialHelper = new TranslationToolHelper();
	}
	
	/**
	 * Builds a complete {@link TranslationRequest} for the given bug number and the given
	 * aggregate directory. 
	 */
	public boolean buildRequest(String bugNumber, String aggregateDir) {
		if (!bugNumber.matches(PATTERN_BUG_NUMBER)) {
			informer.informUser("Please enter a valid bug number!");
			return false;
		}

		this.bugNumber = bugNumber;
		// Find all directories containing message resource files.
		List<MsgRscDir> mrDirectories = IOUtils.findMesResDirs(aggregateDir);

		if (mrDirectories == null) {
			// Exception occurred while trying to determine all MR directories.
			logger.error("I/O error while trying to determine all directories containing "
					+ "message resource files under aggregate directory: " + aggregateDir);
			return false;
		}
		
		// Now scour all message resource files in the found directories for mention of the
		// given bug number.
		for (MsgRscDir dir : mrDirectories) {			
			if (!buildByBugNumber(bugNumber, dir.getPath())) {
				return false;
			}
		}
		// Scan Liquibase scripts for occurrence of bug number. Add these to request.
		DbScanner scanner = new DbScanner(bugNumber);
		scanner.scan(aggregateDir);
		List<DbDir> directoriesToScan = scanner.getDirectoriesToScan();
		
		for (DbDir dir : directoriesToScan) {
			if (!scanDbDirectory(dir)) {
				logger.log("buildRequest - failed to scan directory!");
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Scans the files that contain the bug number for translation requests, determines
	 * the Dutch and English text for these and adds new {@link DbTranslation}s to the
	 * request for each. 
	 */
	private boolean scanDbDirectory(DbDir dir) {
		LiquibaseFileReader reader = new LiquibaseFileReader();
		for (DbFile file : dir.getFiles()) {
			if (!reader.readFile(file.getFullPath())) {
				logger.log("scanDbDirectory - failed to read file: " 
						+ file.getFullPath());
				return false;
			}
			// Get a list of the insert-/updateTranslations elements.
			List<LiquibaseElement> translations = new ArrayList<>();
			reader.getDatabaseChangeLog().getChildrenBy(translations, 
					(element)-> 
						element.getTag().equals("ext:insertTranslations")
						|| element.getTag().equals("ext:updateTranslations"));
						
			// For each insertTranslations, create a DbTranslation and add it to the list.
			DbTranslationBuilder builder = new DbTranslationBuilder(bugNumber);
			for (LiquibaseElement insTrans : translations) {
				List<DbTranslation> dbTranslations = builder.build(insTrans);
				if (dbTranslations.size() == 0) {
					logger.log("scanDbDirectory - failed to build translation from "
							+ "insertTranslations element!");
					return false;
				}
				translationRequest.addDbTranslations(dbTranslations);
			}
		}
		
		return true;
	}
	
	/**
	 * Scans the given directory for message resources with a placeholder for 
	 * the given bug number. Missing message keys are collected and Dutch and 
	 * English translations are gathered. The results are set on the
	 * {@link TranslationRequest}.
	 * 
	 * @param bugNumber - the bug number to scan for. Hopefully for a 'request 
	 * translations for ...'-sub-task.
	 * @param directory - the full path to the directory to scan. 
	 */
	public boolean buildByBugNumber(String bugNumber, String directory) {
		if (translationRequest == null) {
			translationRequest = new TranslationRequest(bugNumber);
		}

		Path directoryPath = Paths.get(directory);
		
		if (!Files.isDirectory(directoryPath)) {
			informer.informUser("Please enter a valid directory path!");
			return false;
		}
		
		// Initial checks OK. Reset the list of Dutch and English MR files.
		dutchAndEnglishMsgRscFiles = new ArrayList<>();
		
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(directoryPath, "MessageResources*.properties");
				DirectoryStream<Path> infoDs = Files.newDirectoryStream(directoryPath, "InformationMessageResources*.properties")) {
			
			// Bring the bug number back to its bare essentials (so we can scan the msgRsc file for it).
			String bareBugNumber = bugNumber.replaceAll("[QqSsDd-]", "");
			
			for (Path msgRscFile : ds) {
				// Use the MsgRscExcelReader to create a language bundle from the import file.
				if (!findByBugNumber(msgRscFile, bareBugNumber)) {
					System.out.println("Failed to read the missing messages from file: " + msgRscFile);
					return false;
				}
			}
			
			// Now that we have created a complete set of the message keys that we are going to request, find the
			// Dutch and English translations for them in the corresponding message resource files.
			for (Path nlEnMsgRscFile : dutchAndEnglishMsgRscFiles) {
				if (!findPresentMessages(nlEnMsgRscFile)) {
					logger.log("Failed to read the existing messages from file: " + nlEnMsgRscFile);
					return false;
				}
			}
			
			// Do the same for InformationMessageResources files.
			dutchAndEnglishMsgRscFiles = new ArrayList<>();
			for (Path msgRscFile : infoDs) {
				// Use the MsgRscExcelReader to create a language bundle from the import file.
				if (!findByBugNumber(msgRscFile, bareBugNumber)) {
					System.out.println("Failed to read the missing messages from file: " + msgRscFile);
					return false;
				}
			}
			
			// Now that we have created a complete set of the message keys that we are going to request, find the
			// Dutch and English translations for them in the corresponding message resource files.
			for (Path nlEnMsgRscFile : dutchAndEnglishMsgRscFiles) {
				if (!findPresentMessages(nlEnMsgRscFile)) {
					logger.log("Failed to read the existing messages from file: " + nlEnMsgRscFile);
					return false;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Scans the given Dutch or English MR file for keys that are present in the TranslationRequest
	 * under construction. If a match is found, the Dutch/English translation is added to the
	 * request - so the translators have something more than just a message key to work with.
	 * 
	 * @param msgRscFile - the file to scan. Must be either Dutch or English.
	 */
	private boolean findPresentMessages(Path msgRscFile) {
		// Determine the language of this file.
		String fileName = msgRscFile.getFileName().toString();
		String languageCode = fileName.substring(fileName.indexOf("_") + 1, fileName.indexOf("."));
		activeLanguage = Language.fromCode(languageCode);
		
		if (!Language.DUTCH.equals(activeLanguage) && !Language.ENGLISH.equals(activeLanguage)) {
			// We are only looking for messages in the languages that we do not request from the translator.
			logger.log("Translations for language " + activeLanguage + " have to be"
					+ " requested from the translator!");
		}
		// Set it as active language on the TranslationRequest.
		translationRequest.setActiveLanguage(activeLanguage);

		MrFileReader reader = new MrFileReader();
		if (!reader.readFile(msgRscFile.toString())) {
			logger.log("findPresentMessages - the file reader failed!");
			return false;
		}
		
		while (reader.next()) {
			
			String messageKey = reader.getKey();
			
			if (translationRequest.containsKey(messageKey)) {
				// Found one. Add this NL/EN message to the request.
				String messageResource = reader.getMessage();

				// Un-escape the special characters, so the translators aren't scared off.
				messageResource = specialHelper.replaceCodeBySpecialCharacters(messageResource);
				translationRequest.addMessage(messageKey, messageResource);
			}
		}
		return true;
	}

	/**
	 * Finds the message resources for which translations should be requested for the given bug number.
	 * @param bareBugNumber - the bug number to scan for. 
	 */
	private boolean findByBugNumber(Path msgRscFile, String bareBugNumber) {
		// Determine the language of this file.
		String fileName = msgRscFile.getFileName().toString();
		String languageCode = fileName.substring(fileName.indexOf("_") + 1, fileName.indexOf("."));
		activeLanguage = Language.fromCode(languageCode);
		
		if (activeLanguage == null) {
			// This language is not yet supported, or language could not be determined. Skip the file.
			System.out.println("TranslationRequestBuilder.findMissingMessages -"
					+ " Language code could not be determined from file: " + fileName);
			return true;
		} else if (activeLanguage == Language.DUTCH || activeLanguage == Language.ENGLISH) {
			// The Dutch and English message resources are already provided. Add them to the list however,
			// so the paths are available when we are going to search for the existing translations.
			dutchAndEnglishMsgRscFiles.add(msgRscFile);
			return true;
		}
		
		// Set it as active language on the TranslationRequest.
		translationRequest.setActiveLanguage(activeLanguage);
		MrFileReader reader = new MrFileReader();
		if (!reader.readFile(msgRscFile.toString())) {
			logger.log("findByBugNumber - failed to read the message resource file!");
			return false;
		}
		
		while (reader.next()) {
			// Check all message resources for the bug number.
			if (reader.getMessage().contains(bareBugNumber)) {
				// Found one. Add the message key to the request.
				translationRequest.addMessageRequest(reader.getKey());
			}
		}
		
		return true;
	}

	/**
	 * Returns the result from calling {@link #buildByBugNumber(String, String)}. 
	 */
	public TranslationRequest getTranslationRequest() {
		return translationRequest;
	}
}
