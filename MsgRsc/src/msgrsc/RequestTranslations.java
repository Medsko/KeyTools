package msgrsc;

import msgrsc.craplog.Fallible;
import msgrsc.request.PlaceholderAppender;
import msgrsc.request.TranslationRequestBuilder;
import msgrsc.request.TranslationRequestFileWriter;

/**
 * Scans the given aggregate directory for occurrences of the given bug number.
 * With this information, two CSV files are created in C:\MsgRsc folder: one for 
 * the message keys and one for the database translations.
 * When opened in Excel, the resulting table can be directly copied and then
 * pasted into the request page in KeylaneBase.
 * <p>
 * For the truly lazy, pass {@code true} as a third parameter. This will make
 * the program find all message keys that are present in the English message
 * resource files, but not in the foreign language files and append standardized
 * placeholders (e.g. 'message.key=toBeTranslatedForVlsInQSD-12345') to the relevant 
 * message resource files. So, no more need to insert these yourself!
 * <p>
 * Arguments for this executable:
 * <ol>
 * 	<li>1. bug number - the QSD of the translation sub-task (e.g. 'QSD-12345').
 * 	</li>
 * 	<li>2. aggregate directory - the aggregate directory of the project that 
 * 		will be scanned for missing message resources (e.g. 'C:\src\BUGFIX').
 * 		Hint: if path contains spaces, wrap this argument in quotes.
 * 	</li>
 * 	<li>3. append placeholders - (optional) boolean indicating whether placeholders
 * 		should be appended to the foreign language files for missing message keys.
 * 	</li>
 * </ol>
 */
public class RequestTranslations implements Fallible {

	public static void main(String[] args) {
		// Check and process input.
		if (args.length < 2) {
			log.error("Please run with arguments 1) bug number and 2) aggregate directory");
			return;
		}
		
		String bugNumber = args[0];
		String aggregateDir = args[1];
		
		if (args.length > 2) {
			// Check whether the user wants to automatically determine what message resources
			// should be requested.
			Boolean shouldAppendPlaceholders = Boolean.parseBoolean(args[2]);
			if (shouldAppendPlaceholders) {				
				PlaceholderAppender appender = new PlaceholderAppender(bugNumber);
				
				if (!appender.appendPlaceHolders(aggregateDir)) {
					log.error("Failed to append placeholders!");
				} else {
					log.error("Placeholders were appended successfully!");
				}
			}
		}
		
		if (args.length > 3 && Boolean.parseBoolean(args[3])) {
			log.debug("Fourth argument was 'true': only appending placeholders.");
			return;
		}
		
		TranslationRequestBuilder builder = new TranslationRequestBuilder();

		if (!builder.buildRequest(bugNumber, aggregateDir)) {
			log.error("Failed to build the request!");
			return;
		}
		
		TranslationRequestFileWriter writer = new TranslationRequestFileWriter(builder.getTranslationRequest());
		writer.setIncludeColumnHeaders(true);
		
		if (!writer.writeToCsvFile()) {
			log.error("Failed to write request to file!");
		} else {
			log.debug("Great success for glorious nation of Kazachstan...I mean Keylane!");
		}
	}
}
