package msgrsc.imp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import msgrsc.dao.DbTranslation;
import msgrsc.utils.Language;
import msgrsc.utils.TranslationToolHelper;

public class MsgRscExcelReader {

	private String bareBugNumber;
	
	private Language fileLanguage;
	
	private Integer columnContainingBugNumber;
	private Integer columnContainingKey;
	private Integer columnContainingTranslations;
	private Integer columnContainingEnglishText;
	
	private DataFormatter formatter;
	private TranslationToolHelper translationHelper;
	
	// OUTPUT
	private LanguageBundle languageBundle;
	// English text as key.
	private Map<String, DbTranslation> dbTranslations;
	
	public MsgRscExcelReader(String bareBugNumber) {
		this.bareBugNumber = bareBugNumber;
		formatter = new DataFormatter();
		translationHelper = new TranslationToolHelper();
		dbTranslations = new HashMap<>();
	}
	
	/**
	 * Reads the translations for the given bug number from the given Excel file and
	 * adds them to the {@link #languageBundle}. 
	 * 
	 * @param importFile - the full path to the file to read from.
	 * @return boolean indicating success. 
	 */
	public boolean buildFromFile(String importFile) {

		if (languageBundle == null) { 
			languageBundle = new LanguageBundle(bareBugNumber);
		}
		// Determine the language of the translations in the file.
		fileLanguage = Language.fromStringContainsFullName(importFile);
		languageBundle.setActiveLanguage(fileLanguage);
		
		try (Workbook workbook = WorkbookFactory.create(new File(importFile))) {
			
			Sheet msgRscSheet;
			if ((msgRscSheet = workbook.getSheet("Message resources")) == null
					&& (msgRscSheet = workbook.getSheet("MessageResources")) == null) {
				msgRscSheet = workbook.getSheetAt(0);
			}
			
			Sheet dbTranslationsSheet;
			if ((dbTranslationsSheet = workbook.getSheet("Database")) == null) {
				dbTranslationsSheet = workbook.getSheetAt(1);
			}
			
			if (!readMessageResources(msgRscSheet)) {
				return false;
			}
			
			if (!readDbTranslations(dbTranslationsSheet)) {
				return false;
			}
			
		} catch (EncryptedDocumentException | IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private boolean readDbTranslations(Sheet dbTranslationSheet) {
		// Parse the first row.
		if (!readFirstRow(dbTranslationSheet.getRow(0))) {
			System.out.println("Could not determine which columns hold the bug number, "
					+ "key and translation for language: " + fileLanguage.toString());
			return false;
		}
		
		// Skip the first row, as we already extracted all useful information from it in 
		// the previous step.
		for (int i=1; i<dbTranslationSheet.getPhysicalNumberOfRows(); i++) {
			Row currentRow = dbTranslationSheet.getRow(i);
			// Check whether this translation is relevant for the bug number.
			if (currentRow == null) {
				System.out.println("Faulty row: " + i);
				continue;
			}
			String rowBugNumber = getCellContent(currentRow, columnContainingBugNumber);

			if (!rowBugNumber.contains(bareBugNumber)) {
				// This translation is for another bug. Skip it.
				continue;
			}
			// This translation is relevant. Get the original text and translation, and
			// add it to the list.
			String englishText = getCellContent(currentRow, columnContainingEnglishText).trim();
			String translation = getCellContent(currentRow, columnContainingTranslations).trim();
			
			if (translation.equalsIgnoreCase("x")) {
				// This translation has already been provided, or was not deemed necessary.
				continue;
			}
			
			// Replace the special characters with their unicode equivalents.
			// Skip for now for DB translations: this does not appear to yield desired results. 
			translation = translationHelper.replaceSpecialCharactersByCodes(translation);
			translation = translationHelper.replaceCodeBySpecialCharacters(translation);
			
			System.out.println("Database translation: " + englishText + ", translation: " + translation);
			// Escape single quotes (by adding a second quote).
			translation = translation.replace("'", "''");

			// Check if a translation object is already present for this text.
			DbTranslation dbTranslation = dbTranslations.get(englishText);
			if (dbTranslation == null) {
				// No translation yet. Create one now.
				dbTranslation = new DbTranslation();
				dbTranslation.setTextEnglish(englishText);
				dbTranslations.put(englishText, dbTranslation);
			}
			dbTranslation.addTranslation(fileLanguage, translation);
		}
		return true;
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
			if (cellContent.toLowerCase().contains("key")) {
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
	
	private boolean readMessageResources(Sheet msgRscSheet) {
		
		Row topRow = msgRscSheet.getRow(0);
		
		if (!readFirstRow(topRow)) {
			System.out.println("Could not determine which columns hold the bug number, "
					+ "key and translation for language: " + fileLanguage.toString());
			return false;
		}
		
		// Skip the first row, as we already extracted all useful information from it in 
		// the previous step.
		for (int i=1; i<msgRscSheet.getPhysicalNumberOfRows(); i++) {
			Row currentRow = msgRscSheet.getRow(i);
			// Check whether this message resource is relevant for the bug number.
			
			if (currentRow == null) {
				System.out.println("Faulty row: " + i);
				continue;
			}
			String rowBugNumber = getCellContent(currentRow, columnContainingBugNumber);

			if (!rowBugNumber.contains(bareBugNumber)) {
				// This translation is for another bug. Skip it.
				continue;
			}
			// This message resource is relevant. Read in the key and the value, and
			// add it to the bundle.
			String key = getCellContent(currentRow, columnContainingKey).trim();
			String translation = getCellContent(currentRow, columnContainingTranslations).trim();
			
			if (translation.equalsIgnoreCase("x")) {
				// This translation has already been provided, or was not deemed necessary.
				continue;
			}
			
			// Replace the special characters with their unicode equivalents.
			translation = translationHelper.replaceSpecialCharactersByCodes(translation);
			System.out.println("Message resource key: " + key + ", translation: " + translation);
			languageBundle.addMessage(key, translation);
		}
		
		return true;
	}
	
	public List<DbTranslation> getDbTranslations() {
		return new ArrayList<DbTranslation>(dbTranslations.values());
	}
	
	protected String getCellContent(Row row, int columnNumber) {
		return formatter.formatCellValue(row.getCell(columnNumber));
	}
		
	public void setLanguageBundle(LanguageBundle languageBundle) {
		this.languageBundle = languageBundle;
	}

	public LanguageBundle getLanguageBundle() {
		return languageBundle;
	}
}
