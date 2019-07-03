package msgrsc.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import msgrsc.craplog.Fallible;
import msgrsc.dao.DbTermHit;
import msgrsc.dao.LanguageTable;

public class DbTermFinder implements Fallible {

	private List<LanguageTable> tablesWithHits;
	
	private DbTermQueryBuilder builder;

	public DbTermFinder(DbTermQueryBuilder builder) {
		this.builder = builder;
	}
	
	/**
	 * Executes the queries for determining whether the given term is present in the
	 * given (language) tables.
	 */
	public boolean findTermInTables(String term, List<LanguageTable> tablesToSearch) {
		
		String lowerCaseTerm = term.toLowerCase();
		tablesWithHits = new ArrayList<>();
		
		try (McGarnagle playground = new McGarnagle(null)) {
		
			for (LanguageTable table : tablesToSearch) {
				
				String query = builder.constructQueryForTable(lowerCaseTerm, table);
				// Log the query, when in debug mode.
				log.debug(query);
				
				if (!playground.executeQuery(query)) {
					log.log("Either no results found, or something went wrong.");
					continue;
				}
				
				do {
					
					for (String fieldName : table.getFields()) {
						String entry = playground.getString(fieldName);
						// Check if the field is a legit hit.
						if (entry != null && entry.toLowerCase().contains(lowerCaseTerm)) {
							// Add it to the list of hits for this table.
							Integer[] pk = determinePk(playground, table);
							DbTermHit hit = table.getHit(pk);
							if (hit == null) {
								hit = new DbTermHit();
								hit.setEntry(entry);
								hit.setFieldName(fieldName);
								hit.setPkValues(pk);
								table.addHit(hit);
							}
							hit.addContent(builder.getLanguageToSearch(), entry);
						}
					}
				} while (playground.next());
				
				tablesWithHits.add(table);
			}
		} catch (SQLException sqlex) {
			sqlex.printStackTrace();
			return false;
		} 
		// If everything went smoothly:		
		return true;
	}
	
	private Integer[] determinePk(McGarnagle playground, LanguageTable table) throws SQLException {
		Integer[] pkFields = new Integer[table.getPkFields().length];
		for (int i=0; i<pkFields.length; i++) {
			pkFields[i] = playground.getInt(table.getPkFields()[i]);
		}
		return pkFields;
	}
	
	public List<LanguageTable> getTablesWithHits() {
		return tablesWithHits;
	}
}
