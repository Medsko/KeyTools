package msgrsc.request;

import java.util.ArrayList;
import java.util.List;

import msgrsc.dao.DbTranslation;
import msgrsc.dao.LiquibaseElement;
import msgrsc.utils.Language;

public class DbTranslationBuilder {

	private String bareBugNumber;
		
	public DbTranslationBuilder(String bugNumber) {
		bareBugNumber = bugNumber.replaceAll("[QqSsDd-]", "");
	}

	public List<DbTranslation> build(LiquibaseElement translationElement) {
		List<DbTranslation> results = new ArrayList<>();
		String tableName = translationElement.getTableName();
		
		List<LiquibaseElement> translationColumns = translationElement.getChildrenBy(
				(element)-> element.getTag().equals("ext:translationColumns"));
		
		LiquibaseElement translationColumnsElement = translationColumns.get(0);
		
		List<LiquibaseElement> columns = translationColumnsElement.getChildrenBy(
				(element)-> element.getTag().equals("ext:column"));

		for (LiquibaseElement column : columns) {
			// Create a new translation DAO and set the table name on it.
			DbTranslation translation = new DbTranslation();
			translation.setTable(tableName);
			translation.setColumnName(column.getColumnName());

			// Determine for which languages the translation is required.
			List<LiquibaseElement> reqTranslations = column.getChildrenBy(
					(element)-> element.getTag().equals("ext:translation"));
			boolean[] requestedFor = new boolean[Language.foreignLanguages().length];
			
			for (LiquibaseElement reqTrans : reqTranslations) {
				
				Language reqTransLang = Language.fromCode(reqTrans.getLanguageCode());
				
				switch (reqTransLang) {
					case DUTCH:
						translation.setTextDutch(reqTrans.getValue());
						break;
					case ENGLISH:
						translation.setTextEnglish(reqTrans.getValue());
						break;
					default:
						if (reqTrans.getValue().contains(bareBugNumber))
							requestedFor[reqTransLang.ordinal() - Language.masteredLanguages().length] = true;
				}
			}
			translation.setRequestedFor(requestedFor);
			// Add the result to the list.
			results.add(translation);
		}
		
		return results;
	}	
}
