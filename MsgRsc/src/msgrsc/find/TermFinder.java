package msgrsc.find;

import java.util.List;

import msgrsc.craplog.Fallible;
import msgrsc.dao.MsgRscFile;
import msgrsc.io.MrFileReader;

/**
 * Scans the given file for usage of the given term in messages. 
 * Composes a list of all occurrences including message key.
 * <p>
 * Use the setters to specify what the {@link TermFinder} should search
 * for. By setting a target this way, the finder internally determines 
 * the appropriate search mode.
 */
public class TermFinder implements Fallible {
	
	private List<String> terms;
	
	private MrFileReader reader;
	
	private List<String> keysToSearchFor;
	
	private Mode currentMode;
	
	private enum Mode {
		MR_MESSAGES,
		MR_KEYS
	}
	
	// TODO: also check language tables for the terms set using setTerms().
	
	
	public TermFinder() {
		reader = new MrFileReader();
	}
	
	/**
	 * Core method. Scans the given file for occurrences of the term(s) 
	 * that have been specified. The hits are stored on the 
	 * {@link MsgRscFile}, and can be accessed afterwards by calling
	 * {@link MsgRscFile#getMessagesOfInterest()}.
	 * 
	 * @param file - the message resource file to scan.
	 * @return {@code boolean} indicating success.
	 */
	public boolean scan(MsgRscFile file) {
		
		if (!reader.readFile(file.getFullPath())) {
			logger.log("TermFinder.scan - failed to read the file!");
			return false;
		}
		
		while (reader.next()) {			
			if (isHit()) {
				// Add the current message resource to the list of messages
				// of interest for the file.
				String key = reader.getKey();
				String message = reader.getMessage();
				file.putMessageOfInterest(key, message);
			}			
		}
		return true;
	}
		
	private boolean isHit() {
		// Determine whether the current message resource can be considered
		// a 'hit' according to the current mode.
		switch (currentMode) {
			case MR_MESSAGES:
				return containsTerm(reader.getMessage());
			case MR_KEYS:
				return keysToSearchFor.contains(reader.getKey());
		}
		return false;
	}

	private boolean containsTerm(String message) {
		for (String term : terms) {
			if (message.toLowerCase().contains(term.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public void setTerms(List<String> terms) {
		// Set the mode.
		currentMode = Mode.MR_MESSAGES;
		this.terms = terms;
	}

	public void setKeysToSearchFor(List<String> keysToSearchFor) {
		// Set the mode.
		currentMode = Mode.MR_KEYS;
		this.keysToSearchFor = keysToSearchFor;
	}
}
