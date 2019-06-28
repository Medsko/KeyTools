package msgrsc.imp;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

import msgrsc.utils.Language;
import msgrsc.utils.TranslationToolHelper;

public class AbstractExcelReader {

	protected String bareBugNumber;
	
	protected Language fileLanguage;
	protected Integer columnContainingBugNumber;
	protected Integer columnContainingKey;
	protected Integer columnContainingTranslations;
	protected Integer columnContainingEnglishText;
	protected DataFormatter formatter;
	protected TranslationToolHelper translationHelper;

	public AbstractExcelReader(String bareBugNumber) {
		this.bareBugNumber = bareBugNumber;
		formatter = new DataFormatter();
		translationHelper = new TranslationToolHelper();
	}
	
	protected boolean readFirstRow(Row topRow) {
		// Reset the column variables.
		columnContainingBugNumber = null;
		columnContainingKey = null;
		columnContainingTranslations = null;
		columnContainingEnglishText = null;

		for (int i=0; i<topRow.getLastCellNum(); i++) {
			// Get the content of the cell as a String.
			String cellContent = getCellContent(topRow, i);
			
			if (cellContent.contains("QSD")) {
				// This column contains the bug numbers for which the translations 
				// were requested. Save the column number in the appropriate variable.
				columnContainingBugNumber = i;
			}
			// Determine which column contains the message keys.
			if (cellContent.contains("key")) {
				// This is the column with message keys. Save the column number.
				columnContainingKey = i;
			}
			// Check if this column holds the English text.
			if (cellContent.contains(Language.ENGLISH.code)) {
				columnContainingEnglishText = i;
			}
			
			// Determine which column is relevant for the current language.
			if (cellContent.contains(fileLanguage.code)) {
				// This column contains the new translations. Save the column number and
				// start processing the rows.
				columnContainingTranslations = i;
				break;
			}
		}
		// If all three necessary column numbers could be determined, read was successful.
		return columnContainingBugNumber != null 
				&& columnContainingKey != null 
				&& columnContainingTranslations != null
				&& columnContainingEnglishText != null;
	}
	
	protected String getCellContent(Row row, int columnNumber) {
		return formatter.formatCellValue(row.getCell(columnNumber));
	}
	
}
