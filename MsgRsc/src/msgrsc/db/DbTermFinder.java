package msgrsc.db;

import java.sql.SQLException;
import java.util.List;

import msgrsc.dao.LanguageTable;

public class DbTermFinder {

	public boolean find(String term) {
		
		LanguageFieldFinder finder = new LanguageFieldFinder();
		finder.findLanguageTablesAllFields();
		
		return findTermInTables(term, finder.getLanguageTables());
	}
	
	public boolean findTermInFields(String term, String... fields) {
		
		LanguageFieldFinder finder = new LanguageFieldFinder();
		finder.findLanguageTablesWithFields(fields);
		
		return findTermInTables(term, finder.getLanguageTables());
	}
	
	/**
	 * Executes the queries for determining whether the given term is present in them.
	 */
	private boolean findTermInTables(String term, List<LanguageTable> tablesToSearch) {
		
		McGarnagle playground = null;
		
		try {
		
			for (LanguageTable table : tablesToSearch) {
				
				// TODO: in geval van een hit -> dynamisch 'sleutelObject' aanmaken voor de tabel en daarin alle hits zetten,
				// met primary key veldnaam-PK value paren + veld waarin de term gevonden is (dus: primary key-kolommen voor die tabel ophalen uit Syscat).
			
				playground = new McGarnagle(null);
				playground.executeQuery(constructQueryForTable(term, table));
				
				
				
			}
			
		} catch (SQLException sqlex) {
			sqlex.printStackTrace();
			return false;
		} finally {
			// TODO: make it so that this finally-block is not necessary at places where McGarnagle is called, because this shit is unacceptable.
			if (playground != null)
				playground.closeConnection();
		}
		// If everything went smoothly:		
		return true;
	}
	
	private String constructQueryForTable(String term, LanguageTable table) {
		StringBuilder query = new StringBuilder("select ");
		
		for (int i=0; i<table.getFields().size(); i++) {
			if (i > 0) {
				query.append(", ");
			}
			query.append(table.getFields().get(i));
		}
		
		query.append(" from ");
		query.append(table.getName());
		
		query.append(" where ");

		// Use '[spatie][term][spatie]', '[term][endOfString]' and '[endOfString][term]' for the search, 
		// so only exact matches result in a hit (e.g. search for 'cover' != hit in case of 'coverage')
		String termSpaced = " " + term + " ";
		String termLeftSpaced = "%" + term;
		String termRightSpaced = term + "%";
		
		for (int i=0; i<table.getFields().size(); i++) {
			
			if (i > 0) {
				query.append(" or ");
			}
			String field = table.getFields().get(i);
			String likeClause = field + " like " + termSpaced
					+ " or " + field + " like " + termLeftSpaced
					+ " or " + field + " like " + termRightSpaced;
			
			query.append(likeClause);
		}
		
		return query.toString();
	}
}
