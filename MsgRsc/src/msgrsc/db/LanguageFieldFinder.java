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
	
	public LanguageFieldFinder() {
		languageTables = new ArrayList<>();
	}
	
	public boolean findLanguageTablesWithFields(String... fields) {
	
		StringBuilder query = new StringBuilder("select tabName, colName");
		query.append(" from Syscat.columns");
		// No SYSIBM tables please.
		
		// TODO: allow for search of DataMart tables - make schema a parameter for creating 
		// a LanguageFieldFinder and the underlying McGarnagle. 
		
		query.append(" where tabSchema = 'QIS'");
		// ...because Customers have names too...
		query.append(" and tabName like '%LANG%'");
		query.append(" and typeName in ('VARGRAPHIC', 'VARCHAR')");
		
		query.append(" and colName in (");
		for (int i=0; i<fields.length; i++) {
			if (i > 0) {
				query.append(", ");
			}
			String field = "'" + fields[i] + "'";
			query.append(field);			
		}
		query.append(")");
		
		query.append(" order by tabName");
		
		return findAndFillLanguageTables(query.toString());
	}
	
	private boolean findAndFillLanguageTables(String query) {
		
		McGarnagle playground = null;
		
		try {
			
			playground = new McGarnagle(null);
			
			playground.executeQuery(query);
			
			LanguageTable currentLanguageTable = null;
			
			while (playground.next()) {
				
				String tableName = playground.getString("tabName");
				if (currentLanguageTable == null || !currentLanguageTable.getName().equals(tableName)) {
					currentLanguageTable = new LanguageTable(tableName);
					languageTables.add(currentLanguageTable);
				}
				
				String fieldName = playground.getString("colName");
				currentLanguageTable.addField(fieldName);
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
