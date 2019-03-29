package msgrsc.request;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import msgrsc.dao.MsgRscDir;
import msgrsc.dao.MsgRscFile;
import msgrsc.filewalk.MsgRscFileFinder;
import msgrsc.io.MrFileReader;
import msgrsc.utils.Fallible;
import msgrsc.utils.Language;
import msgrsc.utils.TranslationToolHelper;

/**
 * This somewhat overweight builder can create a {@link TranslationRequest} from either
 * a given bug number or by performing a full scan of the project, depending on whether
 * {@code true} was passed to {@link #setShouldPerformFullScan(boolean)}.
 * <p>
 * A scan by bug number:
 * <ul>
 * 	<li>Determines all message resource files and scans them for occurrences of the given 
 * 		bug number.
 * 	</li>
 * 	<li>Keeps track of all such occurrences and eventually writes them to a CSV file.</li>
 * </ul>
 * <p>
 * A full scan: 
 * <ul>
 * 	<li>Determines all directories with message resource files.</li>
 * 	<li>Per directory, determines all message keys present in the English file.</li>
 * 	<li>Compares the thus determined set of keys to the keys present in each of the files 
 * 		for the supported foreign {@link Language}s.
 * 	</li>
 * 	<li>Keeps track of the keys that are present in English files, but not in their foreign 
 * 		counterparts and eventually writes them to file.
 * 	</li>
 * </ul>
 */
public class TranslationRequestBuilder implements Fallible {

	private Language activeLanguage;
	
	private List<Path> dutchAndEnglishMsgRscFiles;
	
	private TranslationToolHelper specialHelper;
	
	private boolean shouldPerformFullScan;
	
	private final static String PATTERN_BUG_NUMBER = "QSD-\\d{5}";
	
	private String bugNumber;
	
	private int total;
	
	// OUTPUT
	/** The request for translations for the message resources tagged with the given bug number. */
	private TranslationRequest translationRequest;
	
	public TranslationRequestBuilder() {
		dutchAndEnglishMsgRscFiles = new ArrayList<>();
		specialHelper = new TranslationToolHelper();
	}
	
	public boolean buildRequest(String bugNumber, String aggregateDir) {
		if (!bugNumber.matches(PATTERN_BUG_NUMBER)) {
			System.out.println("Please enter a valid bug number!");
			return false;
		}

		this.bugNumber = bugNumber;
		// Find all directories containing message resource files.
		List<MsgRscDir> mrDirectories = findMrDirs(aggregateDir);

		if (mrDirectories == null) {
			// Exception occurred while trying to determine all MR directories.
			return false;
		}
		
		// Now scour all message resource files in the found directories for mention of the
		// given bug number.
		for (MsgRscDir dir : mrDirectories) {
			
			if (shouldPerformFullScan) {
				
				if (!buildFromFullScan(bugNumber, dir)) {
					return false;
				}
				
			} else {
				
				if (!buildByBugNumber(bugNumber, dir.getPath())) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Finds all directories containing message and/or information resources. 
	 */
	private List<MsgRscDir> findMrDirs(String aggregateDir) {
		// Find all directories containing message resource files.
		Path start = Paths.get(aggregateDir);
		MsgRscFileFinder finder = new MsgRscFileFinder();
		try {
			Files.walkFileTree(start, finder);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return finder.getMsgRscDirectories();
	}
	
	/**
	 * Finds all message resources that are present in the Dutch OR English files in the given
	 * directory and checks whether all of these are also present in the files for the other 
	 * supported languages. Files with missing translations are rewritten with placeholders
	 * for the missing messages.
	 */
	public boolean buildFromFullScan(String bugNumber, MsgRscDir mrDir) {
		if (translationRequest == null) {
			translationRequest = new TranslationRequest(bugNumber);
		}
		
		// Find all missing resources in the directory.
		MrDirectoryScanner scanner = new MrDirectoryScanner(mrDir);
		if (!scanner.scan()) {
			logger.log("buildFromFullScan - scanner failed.");
			return false;
		}
		
		boolean dirContainsMissingMessages = false;
		dutchAndEnglishMsgRscFiles = new ArrayList<>();
		// Add the missing resources to the translation request.
		for (MsgRscFile.Type type : MsgRscFile.Type.values()) {
			for (MsgRscFile file : mrDir.getFiles(type)) {
				
				if (file.getLanguage() == Language.DUTCH 
						|| file.getLanguage() == Language.ENGLISH) {
					// Add Dutch and English files to the list, so we can collect the
					// present messages from them later. 
					dutchAndEnglishMsgRscFiles.add(Paths.get(file.getFullPath()));
					continue;
				}
				
				if (!file.containsMissingMessages()) {
					// This file contains no missing messages.
					continue;
				}
				// This file contains missing messages, so the same is true of the directory.
				dirContainsMissingMessages = true;
				
				translationRequest.setActiveLanguage(file.getLanguage());
				translationRequest.addMessageRequests(file.getMissingMessageKeys());
				
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
		
		if (dirContainsMissingMessages) {
			// Now that we have created a complete set of the message keys that we are going to request, find the
			// Dutch and English translations for them in the corresponding message resource files.
			for (Path nlEnMsgRscFile : dutchAndEnglishMsgRscFiles) {
				if (!findPresentMessages(nlEnMsgRscFile)) {
					logger.log("Failed to read the existing messages from file: " + nlEnMsgRscFile);
					return false;
				}
			}
		}
		
		return true;
	}
	
	private boolean appendPlaceHolders(MsgRscFile file) {
			
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.getFullPath()), StandardOpenOption.APPEND)) {
			for (String missingKey : file.getMissingMessageKeys()) {
				// Compose the place holder.
				String placeHolderLine = missingKey; 
				placeHolderLine += "=toBeTranslatedFor" + file.getLanguage().getPrettyCode();
				placeHolderLine += "In" + bugNumber;
				
				logger.log("At this point, placeholder " + placeHolderLine + System.lineSeparator() 
					+ " would be appended to file " + file.getFullPath());
				total++;
				
				// Append the place holder to the line.
				// TODO: uncomment after test.
//				writer.write(placeHolderLine);
			}
			logger.log("Total number of missing translations found so far: " + total);
		} catch (IOException ioex) {
			ioex.printStackTrace();
			return false;
		}
		
		return true;
	}
		
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
		
		String nextMessageResource;
		while ((nextMessageResource = reader.getKey()) != null) {
			
			String messageKey = nextMessageResource.substring(0, 
					nextMessageResource.indexOf("=")).trim();
			
			if (translationRequest.containsKey(messageKey)) {
				// Found one. Add this NL/EN message to the request.
				String messageResource = nextMessageResource.substring(
						nextMessageResource.indexOf("=") + 1).trim();

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

		try (BufferedReader reader = new BufferedReader(new FileReader(msgRscFile.toFile()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains(bareBugNumber)) {
					// Found one. Extract the message key from this line.
					String messageKey = line.split("=")[0].trim();
					translationRequest.addMessageRequest(messageKey);
				}
			}
		} catch (IOException ioex) {
			ioex.printStackTrace();
			return false;
		}
		
		return true;
	}

	/**
	 * Returns the result from calling {@link #buildByBugNumber(String, String)}. 
	 */
	public TranslationRequest getTranslationRequest() {
		return translationRequest;
	}

	public void setShouldPerformFullScan(boolean shouldPerformFullScan) {
		this.shouldPerformFullScan = shouldPerformFullScan;
	}
}
