package msgrsc.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import msgrsc.craplog.Fallible;
import msgrsc.dao.LanguageTable;

/**
 * Finds the primary key fields for a table and sets them on the provided
 * {@link LanguageTable} object. 
 */
public class PkRetriever implements Fallible {
	
	private String[] excludedFields;
	
	public boolean retrievePk(LanguageTable table) {
		
		try {
			List<String> upperExcludedFields = new ArrayList<>();
			if (excludedFields != null) {
				for (int i=0; i<excludedFields.length; i++) {
					upperExcludedFields.add(excludedFields[i].toUpperCase());
				}
			}
			
			McGarnagle agent = new McGarnagle(null);
			String query = "select PK_colNames from Syscat.references"
					+ " where tabName = '" + table.getName() + "'";
			if (!agent.executeQuery(query)) {
				log.log("Query yielded no results or exception: " + query);
				return true;
			}
			
			String rawResult = "";
			do {
				rawResult += agent.getString("PK_colNames");
			} while (agent.next());
			
			Scanner scanner = new Scanner(rawResult);
			List<String> pks = new ArrayList<>();
			while (scanner.hasNext()) {
				
				String fieldName = scanner.next();
				
				if (upperExcludedFields.contains(fieldName)) {
					continue;
				}
				pks.add(fieldName);
			}
			if (pks.size() < 1)
				log.log("aaaarg");
			
			table.setPkFields(pks.toArray(new String[0]));
			scanner.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return true;
	}

	public void setExcludedFields(String[] excludedFields) {
		this.excludedFields = excludedFields;
	}
}
