package msgrsc.io;

import msgrsc.imp.LanguageBundle;
import msgrsc.utils.StringUtil;

/**
 * Top-heavy Strategy pattern implementation that gives a lowly {@link LineRewriter}
 * the power to couple new message resources to their keys. 
 */
public class MrCoupler implements StringModifier {

	private LanguageBundle languageBundle;
	
	private String bareBugNumber;
		
	public MrCoupler(LanguageBundle languageBundle) {
		this.languageBundle = languageBundle;
		bareBugNumber = languageBundle.getBareBugNumber();
	}
	
	@Override
	public String modify(String text) {
		
		if (text == null || text.length() == 0) {
			return text;
		}
		
		if (text.contains(bareBugNumber)) {
			// This line holds a message for which the translation was requested
			// for this bug number. We should attempt a rewrite.
			String messageKey = text.substring(0, text.indexOf("="));
			String message = languageBundle.getMessage(messageKey);
			
			if (!StringUtil.isEmpty(message)) {
				// A translation was actually provided! Compose the message resource.
				text = messageKey + "=" + languageBundle.getMessage(messageKey);
			} else {
				// This translation was missing from the import file. Add it to the 
				// list of missing translations.
				languageBundle.addMissingTranslation(messageKey);
			}
		}		
		return text;
	}
}
