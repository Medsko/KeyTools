package msgrsc.fitnesse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import msgrsc.craplog.Fallible;
import msgrsc.dao.MessageResource;
import msgrsc.dao.MsgRscDir;
import msgrsc.dao.MsgRscFile;
import msgrsc.io.MrFileReader;
import msgrsc.utils.IOUtils;
import msgrsc.utils.Language;

/**
 * Finds the translations for the keys that were determined by a 
 * {@link MissingDefinitionFinder}. 
 */
public class VariableDefinitionsFinder implements Fallible {
	
	private String aggregateDir;
	
	private List<MsgRscDir> mesResDirs;
	
	private Language currentLanguage;
	
	private Map<Language, List<MessageResource>> variablesPerLanguage;
	
	public VariableDefinitionsFinder(String aggregateDir) {
		this.aggregateDir = aggregateDir;
	}

	/**
	 * Scans Axon message resource files for the given list of message resources and collects the
	 * translations. These can then be accessed via {@link #getVariablesPerLanguage()}.
	 * 
	 * @param missingMessageResources - a Map of missing message resources per language.
	 * @return {@code true} if successful. 
	 */
	public boolean findMessageResourceDefinitions(Map<Language, List<MessageResource>> missingMessageResources) {
		// Find all directories and files containing message resources.
		mesResDirs = IOUtils.findMesResDirs(aggregateDir);

		// For each language, find the translations for the missing keys.
		variablesPerLanguage = missingMessageResources;
		
		if (mesResDirs == null) {
			log.error("Failed to collect all directories containing message resources!");
			return false;
		}
		
		for (Language language : variablesPerLanguage.keySet()) {
			currentLanguage = language;
			if (!findMissingMesRes(variablesPerLanguage.get(language))) {
				log.error("Failed to find the required message resources!");
				return false;
			}
		}
		
		return true;
	}
	
	private boolean findMissingMesRes(List<MessageResource> missingVariables) {
		// Create a shallow copy of the list to avoid ConcurrentModificationExceptions.
		List<MessageResource> stillMissing = new ArrayList<>(missingVariables);
		
		outer:
		for (MsgRscDir dir : mesResDirs) {
			for (MsgRscFile.Type type : MsgRscFile.Type.values()) {
				MsgRscFile file = dir.getFile(type, currentLanguage);
				if (file == null) {
					continue;
				}
				
				if (!scanMesResFile(file.getFullPath(), stillMissing)) {
					log.error("Failed to scan file: " + file.getFullPath());
					return false;
				}
				
				if (stillMissing.size() == 0) {
					// All missing resources have been found for this language.
					log.debug("All missing message resources for " 
							+ currentLanguage.getPrettyName() + " have been found.");
					break outer;
				}
			}
		}
		
		return true;
	}
	
	private boolean scanMesResFile(String mrFile, List<MessageResource> stillMissing) {
		MrFileReader reader = new MrFileReader();	
		if (!reader.readFile(mrFile)) {
			return false;
		}
		List<MessageResource> noLongerMissing = new ArrayList<>();
		
		while (reader.next()) {
			for (MessageResource missing : stillMissing) {
				if (missing.getKey().equals(reader.getKey())) {
					// Found a translation. Add it to the message resource.
					missing.addTranslation(currentLanguage, reader.getMessage());
					// Remove it from the list of missing resources.
					noLongerMissing.add(missing);
				}
			}
		}
		stillMissing.removeAll(noLongerMissing);
		
		return true;
	}

	public Map<Language, List<MessageResource>> getVariablesPerLanguage() {
		return variablesPerLanguage;
	}
}
