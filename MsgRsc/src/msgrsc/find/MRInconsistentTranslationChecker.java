package msgrsc.find;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import msgrsc.craplog.Fallible;
import msgrsc.dao.MsgRscDir;
import msgrsc.dao.MsgRscFile;
import msgrsc.utils.Language;

/**
 * Given a list of terms in a certain language, an instance of this class can 
 * detect inconsistencies in the translations of that term in other languages.
 */
public class MRInconsistentTranslationChecker implements Fallible {

	private Language referenceLanguage;

	private TermFinder finder;

	private Language languageToCheck;

	private int wrongTranslationCount;
	
	private int totalCount;
	
	private List<String> findings;
	
	/**
	 * Contains pairs of term - target translation.
	 */
	private Map<String, String> termTargetTranslation;
	
	/**
	 * Determines whether the the target translations specified in the 
	 * {@code termTargetTranslation} map SHOULD or SHOULD NOT be present 
	 * in the messages in the target language. Default = {@code true}.
	 */
	private boolean shouldContainTargetTranslation;
	
	public MRInconsistentTranslationChecker(Language referenceLanguage) {
		this.referenceLanguage = referenceLanguage;
		finder = new TermFinder();
		findings = new ArrayList<>();
		shouldContainTargetTranslation = true;
	}
	
	public MRInconsistentTranslationChecker(Language referenceLanguage, 
			boolean shouldContainTargetTranslation) {
		this(referenceLanguage);
		this.shouldContainTargetTranslation = shouldContainTargetTranslation;
	}

	/**
	 * Main method for this class. Prior to calling this, call
	 * {@link #setTerms(List)} with the list of terms to search for.
	 */
	public boolean find(String aggregateDir) {

		for (String term : termTargetTranslation.keySet()) {
			
			totalCount = 0;
			wrongTranslationCount = 0;

			List<String> currentSearchTerm = new ArrayList<>();
			currentSearchTerm.add(term);
			finder.setTerms(currentSearchTerm);
			List<MsgRscDir> directoriesContainingTerm = findFilesContainingTerm(aggregateDir);

			for (MsgRscDir dir : directoriesContainingTerm) {
				scanFiles(dir);
				filterByTargetTranslation(dir, termTargetTranslation.get(term));
			}

			try {
				// Write the occurrences of the wrong term to file.
				Path resultFile = Paths.get("C:/", "MsgRsc", term + "IncorrectTranslations.txt"); 
				Files.write(resultFile, findings, StandardOpenOption.CREATE);
				// Clear the list of findings.
				findings = new ArrayList<>();
				informer.informUser("The results of the search have been logged to " 
						+ resultFile.toString());
			} catch (IOException e) {
				e.printStackTrace();
				log.log("find - failed to write findings to file!");
				return false;
			}
		}

		return true;
	}

	private boolean filterByTargetTranslation(MsgRscDir dir, String targetTranslation) {

		for (MsgRscFile.Type type : MsgRscFile.Type.values()) {
			for (Language language : Language.values()) {
				
				MsgRscFile file = dir.getFile(type, language);
				// Also get the reference file, so we can add the reference message in the report.
				MsgRscFile referenceFile = dir.getFile(type, referenceLanguage);

				// No file of this type/language combination in directory.
				if (file == null) {
					continue;
				}

				boolean firstHit = true;
				for (String key : file.getMessagesOfInterest().keySet()) {
					
					totalCount++;
					
					informer.informUser("Now processing file: " + file.getFullPath());
					
					String message = file.getMessagesOfInterest().get(key);
					// If 1) the message should NOT contain the target translation, but DOES...
					// OR 2) the message SHOULD contain the target translation, but does NOT...
					if ((shouldContainTargetTranslation 
								&& !message.toLowerCase().contains(targetTranslation.toLowerCase()))
							|| (!shouldContainTargetTranslation 
								&& message.toLowerCase().contains(targetTranslation.toLowerCase()))) {
						// ...we have a hit.
						wrongTranslationCount++;
						if (firstHit) {
							String leadingMessage = System.lineSeparator() + 
									"Wrong term found in file " + file.getFullPath() + ": ";
							informer.informUser(leadingMessage);
							findings.add(leadingMessage);
							firstHit = false;
						}
						// This is a message that does not contain the correct/target translation.
						String hitMessage = "Total number of unacceptable translations so far: " 
								+ wrongTranslationCount + ". Key: " + key;
						hitMessage += System.lineSeparator() + language.getPrettyCode() + " message: " + message;
						hitMessage += System.lineSeparator() + referenceLanguage.getPrettyCode() + 
								" message: " + referenceFile.getMessagesOfInterest().get(key);
						informer.informUser(hitMessage);
						findings.add(hitMessage);
					} else {
						informer.informUser("Total so far: " + totalCount + 
								". Correct term used for key: " + key + ", message: " + message);
					}
				}
			}
		}

		return false;
	}
	
	/**
	 * 
	 * Given a set of keys that were extracted from the file for the reference language,
	 * scans the file for the {@code languageToCheck} in this directory for occurrences 
	 * of those keys.
	 * <p>
	 * The file of the reference language as well as the files that are of no interest
	 * for the search are removed from the {@link MsgRscDir} object.
	 */
	public boolean scanFiles(MsgRscDir dir) {
		
		for (MsgRscFile.Type type : MsgRscFile.Type.values()) {
			MsgRscFile referenceFile = dir.getFile(type, referenceLanguage);
			
			if (referenceFile == null) {
				// No files of this type in this directory.
				continue;
			}
			
			Set<String> keySet = referenceFile.getMessagesOfInterest().keySet();
			List<String> messageKeysOfInterest = new ArrayList<>();
			messageKeysOfInterest.addAll(keySet);
			// Remove the reference file, as it has now served its purpose.
//			dir.removeFile(referenceFile);

			finder.setKeysToSearchFor(messageKeysOfInterest);

			for (Language language : Language.values()) {
				MsgRscFile file = dir.getFile(type, language);

				// No file of this type/language combination in directory.
				if (file == null) {
					continue;
				}

				if (languageToCheck != null && languageToCheck != file.getLanguage()) {
					// This file is not for the language to check.
					continue;
				}

				if (!finder.scan(file)) {
					return false;
				}
				
				if (!file.containsMessagesOfInterest()) {
					// Remove the uninteresting file from the directory.
					dir.removeFile(file);
				}
			}
		}
		return true;
	}

	/**
	 * Scans through all sub-directories of the given directory and collects all
	 * files that contain the chosen search term into {@link MsgRscDir}s. A list
	 * of these directories is returned.
	 * 
	 * @param aggregateDir - the top directory were the search should start.
	 * @return a list of {@link MsgRscDir}s containing files in which the term occurs. 
	 */
	public List<MsgRscDir> findFilesContainingTerm(String aggregateDir) {
		MsgRscFileFinder finder = new MsgRscFileFinder();
		try {
			Files.walkFileTree(Paths.get(aggregateDir), finder);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		List<MsgRscDir> directoriesContainingTerm = new ArrayList<>();
		for (MsgRscDir dir : finder.getMsgRscDirectories()) {
			if (containsTerms(dir)) {
				directoriesContainingTerm.add(dir);
			}
		}
		return directoriesContainingTerm;
	}

	private boolean containsTerms(MsgRscDir dir) {
		boolean dirContainsTerm = false;
		for (MsgRscFile.Type type : MsgRscFile.Type.values()) {
			MsgRscFile file = dir.getFile(type, referenceLanguage);
			// No file of this type/language combination in directory.
			if (file == null) {
				continue;
			}

			finder.scan(file);
			if (file.containsMessagesOfInterest()) {
				dirContainsTerm = true;
			} else {
				// Remove the file from the directory.
				dir.removeFile(file);
			}
		}
		return dirContainsTerm;
	}

	public void setTermTargetTranslation(Map<String, String> termTargetTranslation) {
		this.termTargetTranslation = termTargetTranslation;
	}

	public void setLanguageToCheck(Language languageToCheck) {
		this.languageToCheck = languageToCheck;
	}
}
