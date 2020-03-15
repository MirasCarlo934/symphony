package symphony.bm.bm_logic_devices.engines.requests.DBEngine;

import bm.main.engines.DBEngine;

import java.util.HashMap;

public class SelectDBEReq extends DBEngineRequest {
	private String[] columns;
	/**
	 * Used to construct the WHERE section
	 * key: column name ;; value: column value
	 */
	private HashMap<String, Object> where = new HashMap<String, Object>(1,1);

	/**
	 * Creates a DBEngineRequest which the DBEngine uses to create a SELECT statement to the DB. This
	 * constructor creates a <i>complete</i> SELECT statement which includes the columns that will be retrieved and the 
	 * WHERE section.
	 * 
	 * @param id The ID of this EngineRequest
	 * @param table The table name for this SELECT statement
	 * @param columns The column names to be retrieved from the specified table.
	 * @param where HashMap containing the column name and the required column value for each. This is used
	 * 		in the WHERE section
	 */
	public SelectDBEReq(String id, DBEngine engine,  String table, String[] columns, HashMap<String, Object> where) {
		super(id, engine, QueryType.SELECT, table);
		this.columns = columns;
		
		String cols = "";
	    	for(int i = 0; i < columns.length; i++) {
	    		cols = cols + columns[i] + ",";
	    	}
	    	
	    	String w = "";
	    	String[] colParams = where.keySet().toArray(new String[0]);
	    	Object[] vals = where.values().toArray(new String[0]);
	    	for(int i = 0; i < vals.length; i++) {
	    		String val = "";
	        	if(vals[i].getClass().equals(String.class)) { //if true, encloses the value in single quotes
	        		String s = transformToQueryFriendlyString(vals[i].toString());
	        		val = "'" + s + "'";
	        	} else {
	        		val = vals[i].toString();
	        	}
	        	
	    		w = w +  colParams[i] + "=" + val + " AND ";
	    	}
	    	cols = cols.substring(0, cols.length() - 1); //cuts off last comma
	    	setQuery("select " + cols + " from " + table + " where " + w);
	}
	
	/**
	 * Creates a SelectDBEReq which the DBEngine uses to create a Select statement to the DB. This
	 * constructor retrieves all columns with a WHERE section.
	 * 
	 * @param id The ID of this EngineRequest
	 * @param table The table name for this SELECT statement
	 * @param where HashMap containing the column name and the required column value for each. This is used
	 * 		in the WHERE section
	 */
	public SelectDBEReq(String id, DBEngine engine, String table, HashMap<String, Object> where) {
		super(id, engine, QueryType.SELECT, table);
	    	
	    	String w = "";
	    	String[] colParams = where.keySet().toArray(new String[0]);
	    	String[] vals = where.values().toArray(new String[0]);
	    	for(int i = 0; i < vals.length; i++) {
	    		String val = "";
	        	if(vals[i].getClass().equals(String.class)) { //if true, encloses the value in single quotes
	        		vals[i] = transformToQueryFriendlyString(vals[i]);
	        		val = "'" + vals[i] + "'";
	        	} else {
	        		val = vals[i].toString();
	        	}
	        	
	    		w = w +  colParams[i] + "=" + val + " AND ";
	    	}
	    	setQuery("select * from " + table + " where " + w);
	}
	
	/**
	 * Creates a SelectDBEReq which the DBEngine uses to create a Select statement to the DB. This constructor specifies
	 * which columns to retrieve but excludes the WHERE section.
	 * 
	 * @param id The ID of this EngineRequest
	 * @param table The table name for this SELECT statement
	 * @param columns The column names to be retrieved from the specified table.
	 */
	public SelectDBEReq(String id, DBEngine engine, String table, String[] columns) {
		super(id, engine, QueryType.SELECT, table);
		this.columns = columns;
		
		String cols = "";
	    	for(int i = 0; i < columns.length; i++) {
	    		cols = cols + columns[i] + ",";
	    	}
	    	cols = cols.substring(0, cols.length() - 1); //cuts off last comma
		setQuery("select " + cols + " from " + table);
	}
	
	/**
	 * Creates a SelectDBEReq which the DBEngine uses to create a Select statement to the DB. This
	 * constructor specifies ALL columns and rows to be retrieved.
	 * 
	 * @param id The ID of this EngineRequest
	 * @param table The table name for this SELECT statement
	 */
	public SelectDBEReq(String id, DBEngine engine, String table) {
		super(id, engine, QueryType.SELECT, table);
		this.columns = new String[]{"*"};
		
		setQuery("select * from " + table);
	}

	/**
	 * Returns all columns specified in this SelectDBEReq. In the query, these columns are the ones to be
	 * retrieved from the DB.
	 * @return the column names in String array
	 */
	public String[] getColumns() {
		return columns;
	}
}
