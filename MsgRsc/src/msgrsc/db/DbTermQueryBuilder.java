package msgrsc.db;

import java.util.List;

import msgrsc.dao.DbTermHit;
import msgrsc.dao.LanguageTable;
import msgrsc.utils.Language;

public class DbTermQueryBuilder {

	private Language languageToSearch;
	
	private boolean findExactHits;
	
	public DbTermQueryBuilder() {}
	
	public DbTermQueryBuilder(Language languageToSearch) {
		this.languageToSearch = languageToSearch;
	}

	protected void appendLanguageCondition(StringBuilder query) {
		if (languageToSearch != null) {
			query.append(" and languageId = ");
			query.append("(select languageId from Language where code = '");
			query.append(languageToSearch.code);
			query.append("') ");
		}
	}
	
	/**
	 * If the given table has {@link DbTermHit}s, only these rows are queried.
	 * 
	 * @param term - the term to search for, in lower case.
	 * @param table - the language table to construct the query for. 
	 */
	public String constructQueryForTable(String term, LanguageTable table) {
		
		if (table == null || table.getPkFields() == null) {
			System.out.println("Wut");
			return "";
		}
		
		StringBuilder query = new StringBuilder("select ");
		// Include all language fields in the select.
		for (int i=0; i<table.getFields().size(); i++) {
			if (i > 0) {
				query.append(", ");
			}
			query.append(table.getFields().get(i));
		}
		
		// Include all primary key fields.
		for (int i=0; i<table.getPkFields().length; i++) {
			query.append(", ");
			query.append(table.getPkFields()[i]);
		}
		
		query.append(" from ");
		query.append(table.getName());
		
		query.append(" where ");

		if (findExactHits) {
			appendFreeStandingTermCondition(query, term, table);
		} else {
			appendConcatTermCondition(query, term, table);
		}
		
		appendHitMatchingCondition(query, table);
		
		appendLanguageCondition(query);
		
		return query.toString();
	}
	
	private void appendFreeStandingTermCondition(StringBuilder query, String term, LanguageTable table) {
		// Use '[spatie][term][spatie]', '[term][endOfString]' and '[endOfString][term]' for the search, 
		// so only exact matches result in a hit (e.g. search for 'cover' != hit in case of 'coverage')
		String termSpaced = "'% " + term + " %'";
		String termLeftSpaced = "'%" + term + "'";
		String termRightSpaced = "'" + term + "%'";
		
		if (table.getFields().size() > 1)
			query.append("(");
		
		for (int i=0; i<table.getFields().size(); i++) {
			
			if (i > 0) {
				query.append(" or ");
			}
			String field = table.getFields().get(i);
			// Ignore case.
			String fieldLowerCase = "lower(" + field + ")";
			String likeClause = fieldLowerCase + " like " + termSpaced
					+ " or " + fieldLowerCase + " like " + termLeftSpaced
					+ " or " + fieldLowerCase + " like " + termRightSpaced;
			
			query.append(likeClause);
		}
		
		if (table.getFields().size() > 1)
			query.append(")");
	}
	

	/**
	 * Appends a condition that tests for occurrence of the target term. 
	 */
	private void appendConcatTermCondition(StringBuilder query, String term, LanguageTable table) {
		if (table.getFields().size() > 1)
			query.append("(");

		for (int i=0; i<table.getFields().size(); i++) {
			if (i > 0) {
				query.append(" or ");
			}
			String field = table.getFields().get(i);
			// Ignore case.
			String fieldLowerCase = "lower(" + field + ")";
			String likeClause = fieldLowerCase + " like '%" + term + "%'";			
			query.append(likeClause);
		}
		
		if (table.getFields().size() > 1)
			query.append(")");
	}
	
	/**
	 * If the given table has one or more 'Hit' entries, appends a condition that ensures
	 * that only records matching the primary key (excluding languageId) of the hit are selected. 
	 */
	private void appendHitMatchingCondition(StringBuilder query, LanguageTable table) {
		List<DbTermHit> hits = table.getHits();
		if (hits.size() == 0)
			return;
		
		query.append(" and (");
		for (int i=0; i<hits.size(); i++) {
			if (i > 0)
				query.append(" or ");
			
			DbTermHit hit = hits.get(i);
			
			if (table.getPkFields().length > 1)
				query.append("(");
			
			for (int j=0; j<table.getPkFields().length; j++) {
				if (j > 0)
					query.append(" and ");
				query.append(table.getPkFields()[j]);
				query.append(" = ");
				query.append(hit.getPkValues()[j]);
			}
			if (table.getPkFields().length > 1)
				query.append(")");
		}
		query.append(")");
	}
	
	public void setFindExactHits(boolean findExactHits) {
		this.findExactHits = findExactHits;
	}
	
	public Language getLanguageToSearch() {
		return languageToSearch;
	}

	public void setLanguageToSearch(Language languageToSearch) {
		this.languageToSearch = languageToSearch;
	}
}
