package msgrsc.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import msgrsc.dao.LanguageTable;

/**
 * Indexes the QIS language tables, i.e. all tables that have 'LANG' in their name and that 
 * have one or more of the specified typical language table fields.
 */
public class LanguageFieldFinder {

	private List<LanguageTable> languageTables;
	
	private PkRetriever retriever;
	
	public LanguageFieldFinder() {
		languageTables = new ArrayList<>();
		retriever = new PkRetriever();
	}
	
	/**
	 * Finds all language tables that have one or more columns that match the given
	 * field name(s). 
	 */
	public boolean findLanguageTablesWithFields(String... fields) {
	
		StringBuilder query = new StringBuilder("select tabName, colName");
		query.append(" from Syscat.columns");
		// No SYSIBM tables please.
		
		// TODO: allow for search of DataMart tables - make schema a parameter for creating 
		// a LanguageFieldFinder and the underlying McGarnagle. 
		
		query.append(" where tabSchema = 'QIS'");
		// ...because Customers have names too...
		query.append(" and tabName like '%LANG%'");
		// Exclude the Language table.
		query.append(" and tabName <> 'LANGUAGE'");
		query.append(" and typeName in ('VARGRAPHIC', 'VARCHAR')");
		
		query.append(" and colName in (");
		for (int i=0; i<fields.length; i++) {
			if (i > 0) {
				query.append(", ");
			}
			String field = "'" + fields[i].toUpperCase() + "'";
			query.append(field);
		}
		query.append(")");
		
		query.append(" order by tabName");
		
		return findAndFillLanguageTables(query.toString());
	}
	
	private boolean findAndFillLanguageTables(String query) {
		
		McGarnagle playground = null;
		
		try {
			
			LanguageTable currentLanguageTable = null;
			playground = new McGarnagle(null);
			
			playground.executeQuery(query);
			int nrFieldsCurrentTable = 0;
			
			while (playground.next()) {
				
				String tableName = playground.getString("tabName");
				String fieldName = playground.getString("colName");
				
				// I've experienced some problems when automatically constructing queries for tables
				// with a lot of language fields, so enforce a maximum of 5 fields per table.
				if (currentLanguageTable == null 
						|| nrFieldsCurrentTable >= 5
						|| !currentLanguageTable.getName().equals(tableName)) {
					currentLanguageTable = new LanguageTable(tableName);
					languageTables.add(currentLanguageTable);
					nrFieldsCurrentTable = 0;
				}
				
				if (nrFieldsCurrentTable >= 5) {
					currentLanguageTable = new LanguageTable(tableName);
					languageTables.add(currentLanguageTable);
				}
				currentLanguageTable.addField(fieldName);
				// Don't include the languageId in the primary key, as we want to find matches for the
				// same translation but in a different language later on.
				retriever.setExcludedFields(new String[] { "languageId" });
				retriever.retrievePk(currentLanguageTable);
				
				nrFieldsCurrentTable++;
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (playground != null)
				playground.closeConnection();
		}
		
		return true;
	}
	
	/**
	 * Finds all columns in language tables that might very well be language fields and passes them to
	 * {@link #findLanguageTablesWithFields(String...)}, so all language fields in all language tables will
	 * be returned. 
	 */
	public boolean findLanguageTablesAllFields() {
		
		HashSet<String> columns = new HashSet<>(); 		
		McGarnagle playground = null;
		
		try {
			
			playground = new McGarnagle(null);
			
			StringBuilder query = new StringBuilder("select tabName, colName");
			query.append(" from Syscat.columns");
			query.append(" where tabSchema = 'QIS'");
			query.append(" and tabName like '%LANG%'");
			// Exclude the Language table.
			query.append(" and tabName <> 'LANGUAGE'");
			query.append(" and colName not in ('TSCREATED', 'TSCHANGED', 'USERIDCREATED', 'USERIDCHANGED', 'LANGUAGEID')");
			query.append(" and typeName in ('VARGRAPHIC', 'VARCHAR')");
			
			playground.executeQuery(query.toString());
			
			while (playground.next()) {
				columns.add(playground.getString("colName"));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (playground != null)
				playground.closeConnection();
		}
		
		String[] fields = columns.toArray(new String[0]);
		
		return findLanguageTablesWithFields(fields);
	}

	public List<LanguageTable> getLanguageTables() {
		return languageTables;
	}	
}
