package msgrsc.fixes;

import java.util.HashMap;
import java.util.Map;

import msgrsc.utils.Language;

public class Testy {

	
	public static void main(String[] args) {
		
		MRInconsistentTranslationChecker checker = new MRInconsistentTranslationChecker(Language.DUTCH);
		checker.setLanguageToCheck(Language.ENGLISH);
		Map<String, String> termTargetTranslation = new HashMap<>();
//		termTargetTranslation.put("aanvraag", "application");
		termTargetTranslation.put("aanvragen", "applications");
		checker.setTermTargetTranslation(termTargetTranslation);
		
		String aggregateDir = "C:\\src\\QSD-36385";
//		String aggregateDir = "C:\\src\\QSD-36385\\QISDamageClaimAdministration\\source\\com"
//				+ "\\quinity\\qfa\\damageclaimadministration\\carcollisionliability";
		
		checker.find(aggregateDir);
		
	}
	
}
