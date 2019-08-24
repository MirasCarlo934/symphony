package bm.main.engines.requests.DBEngine;

import bm.main.engines.AbstEngine;
import bm.main.engines.DBEngine;
import bm.main.engines.requests.EngineRequest;

public abstract class DBEngineRequest extends EngineRequest {
	private QueryType type;
	protected String table;
	private String query;

	public DBEngineRequest(String id, DBEngine engine, QueryType type, String table) {
		super(id, engine);
		this.type = (type);
		this.table = table;
	}

	/**
	 * Returns the QueryType of this DBEngineRequest. Used to determine the kind of query to be used by 
	 * the DBEngine (select, delete, updateRules, insert, raw query string)
	 * @return the QueryType enum
	 */
	public QueryType getQueryType() {
		return type;
	}
	
	public String getQuery() {
		return query;
	}
	
	protected void setQuery(String query) {
		this.query = query;
	}
	
	protected String transformToQueryFriendlyString(String str) {
		char[] string = str.toCharArray();
		String s = "";
		for(int i = 0; i < string.length; i++) {
			char c = string[i];
			if(c == '\'' || c == '"') {
				s += String.valueOf(c) + String.valueOf(c);
			} else {
				s += c;
			}
		}
		return s;
	}
	
	public String getTable() {
		return table;
	}
}
