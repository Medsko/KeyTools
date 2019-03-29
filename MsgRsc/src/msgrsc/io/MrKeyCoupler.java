package msgrsc.io;

import msgrsc.imp.LanguageBundle;
import msgrsc.utils.StringUtil;

public class MrKeyCoupler implements StringModifier {

	private LanguageBundle languageBundle;
	
	public MrKeyCoupler(LanguageBundle languageBundle) {
		this.languageBundle = languageBundle;
	}
	
	@Override
	public String modify(String text) {
		if (text == null || text.length() == 0) {
			return text;
		}

		String key = text.substring(0, text.indexOf("="));
		if (languageBundle.containsKey(key)) {
			
			String message = languageBundle.getMessage(key);
			if (!StringUtil.isEmpty(message)) {
				// A translation was actually provided! Compose the message resource.
				text = key + "=" + message;
			} else {
				// This translation was missing from the import file. Add it to the 
				// list of missing translations.
				languageBundle.addMissingTranslation(key);
			}
		}
		return text;
	}	
}
