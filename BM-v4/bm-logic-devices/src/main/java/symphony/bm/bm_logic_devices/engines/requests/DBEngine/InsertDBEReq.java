package symphony.bm.bm_logic_devices.engines.requests.DBEngine;

import bm.main.engines.DBEngine;

import java.util.HashMap;

public class InsertDBEReq extends DBEngineRequest {
	private HashMap<String, Object> values = new HashMap<String, Object>(1,1);

	/**
	 * Creates a DBEngineRequest which the DBEngine uses to create an INSERT SQL statement to the DB.
	 * 
	 * @param id The ID of this EngineRequest
	 * @param table The table name for this Insert statement
	 * @param values HashMap containing the column names and the new values of each for the new row. 
	 * 		<b>MUST</b> contain all columns of the table specified, if required. 
	 */
	public InsertDBEReq(String id, DBEngine engine,  String table, HashMap<String, Object> values) {
		super(id, engine, QueryType.INSERT, table);
		this.table = table;
		this.values = values;
		
		String scols = "";
	    	String vals = "";
	    	Object[] cols = values.keySet().toArray();
	    	for(int i = 0; i < values.size(); i++) {
	    		//puts all column names into a string for query construction
	    		String col = (String) cols[i];
	    		scols = scols + col + ",";
	    		
	    		//puts all values into a string for query construction
	    		Object val = values.get(col);
	    		if(val == null) {
	    			vals += "NULL,";
	    		} else {
		    		if(val.getClass().equals(String.class)) { //if true, encloses the value in single quotes
		    			val = transformToQueryFriendlyString(val.toString());
		    			vals = vals + "'" + val + "',";
		    		} else {
		    			vals = vals + val + ",";
		    		}
	    		}
	    	}
	    	scols = scols.substring(0, scols.length() - 1); //cuts off last comma
	    	vals = vals.substring(0, vals.length() - 1); //cuts off last comma
	    	
	    	setQuery("insert into " + table + "(" + scols + ") values(" + vals + ")");
	}
}
