package msgrsc;

import java.io.File;
import java.io.IOException;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class MsgRscExcelReader {

	private String importFile;
	
	public MsgRscExcelReader(String importFile) {
		this.importFile = importFile;
	}
	
	public boolean buildFromDir(String importDir) {
		
		return true;
	}
	
	public boolean buildFromFile(String importFile) {
		try {
			Workbook workbook = WorkbookFactory.create(new File(importFile));
			// Get the sheet with the message resources and read it.
			Sheet msgRscSheet = workbook.getSheetAt(0);
			
			// Get the sheet with the database translations and read it.
			
			Row topRow = msgRscSheet.getRow(0);
			
			// Determine which column is relevant for the current language.
			DataFormatter formatter = new DataFormatter();
			// Lambda!!
			topRow.forEach(cell -> {
				String cellContent = formatter.formatCellValue(cell);
			});
			
			for (Cell cell : topRow) {
				// Get the content of the cell as a String.
				String cellContent = formatter.formatCellValue(cell);
				
			}
			
			for (int i=1; i<msgRscSheet.getPhysicalNumberOfRows(); i++) {
				Row currentRow = msgRscSheet.getRow(i);
				// Check whether this message resource is relevant for the bug number.
				
				
					// This message resource is relevant. Read in the key and the value, and
					// add it to the bundle.
				
			}
			
			
		} catch (EncryptedDocumentException | IOException e) {
			e.printStackTrace();
			return false;
		}
		
		
		return true;
	}
	
	private boolean readMessageResources(Sheet msgRscSheet) {
		
		return true;
	}
	
	private boolean readDbTranslations(Sheet dbTranslations) {
		
		
		return true;
	}

	
}
