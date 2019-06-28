package msgrsc.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import msgrsc.dao.MsgRscDir;
import msgrsc.dao.MsgRscFile;
import msgrsc.io.MrFileReader;
import msgrsc.utils.Language;

/**
 * Scans all message and/or information resource files in a directory and
 * composes a list of resources that are present in NL or EN, but not in one or
 * more other supported languages.
 */
public class MrDirectoryScanner {

	// INPUT
	/** The directory to scan. */
	private MsgRscDir directory;
	
	// PROCESSING
	private MrFileReader reader;
	
	// OUTPUT
	private Map<Language, List<String>> missingResources;
	
	public MrDirectoryScanner(MsgRscDir directory) {
		this.directory = directory;
		reader = new MrFileReader();
	}

	public boolean scan() {

		for (MsgRscFile.Type type : MsgRscFile.Type.values()) {			
			// For now, assume the programmers who use this are good boys, and
			// have added the Dutch AND English translations - only search EN. 
			MsgRscFile englishFile = getByLanguage(Language.ENGLISH, type);
			if (englishFile == null) {
				continue;
			}
			
			List<String> requiredKeys = getAllKeys(englishFile.getFullPath());
			if (requiredKeys == null) {
				// The keys could not be determined.
				return false;
			}
			
			for (Language language : Language.foreignLanguages()) {
				
				MsgRscFile foreignFile = getByLanguage(language, type);
				
				if (foreignFile == null) {
					continue;
				}
				
				List<String> presentKeys = getAllKeys(foreignFile.getFullPath());
				if (presentKeys == null) {
					// The keys could not be determined.
					return false;
				}
				
				List<String> missingKeys = determineMissingKeys(requiredKeys, presentKeys);
				if (missingKeys.size() > 0) {
					// Some missing translations were found.
					foreignFile.setMissingMessageKeys(missingKeys);
				}
			}
		}
		
		return true;
	}
	
	/**
	 * This method is fastest when presented with two alphabetically ordered lists,
	 * but this is not a prerequisite.  
	 */
	private List<String> determineMissingKeys(List<String> required, List<String> present) {
		List<String> missing = new ArrayList<>();
		
		for (String requiredKey : required) {
			String matchedKey = null;
			for (String presentKey : present) {
				if (requiredKey.equals(presentKey)) {
					matchedKey = presentKey;
					break;
				}
			}
			if (matchedKey == null) {
				// No match could be found. This resource is missing.
				missing.add(requiredKey);
			} else {
				// Remove the matched key to reduce processing time for the next search.
				present.remove(matchedKey);
			}
		}
		return missing;
	}
	
	/*
	 * Gets all message keys (duplicates possible) from the given file.
	 */
	private List<String> getAllKeys(String fileToScan) {
		// TODO: use a Set<String> and check for duplicates (so we can fix them later).
		List<String> requiredKeys = new ArrayList<>();
		
		if (!reader.readFile(fileToScan)) {
			return null;
		}
		
		while (reader.next()) {
			requiredKeys.add(reader.getKey());
		}
		
		return requiredKeys;
	}
	
	private MsgRscFile getByLanguage(Language language, MsgRscFile.Type type) {
		for (MsgRscFile file : directory.getFiles(type)) {
			if (file.getLanguage() == language) {
				return file;
			}
		}
		return null;
	}

	public Map<Language, List<String>> getMissingResources() {
		return missingResources;
	}
}
