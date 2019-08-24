package bm.main.engines.requests.DBEngine;

import java.util.HashMap;

import org.apache.log4j.Logger;

import bm.main.engines.DBEngine;

public class DeleteDBEReq extends DBEngineRequest {
	private static final Logger LOG = Logger.getLogger("ENGINES_LOG.DeleteDBEReq");
	private HashMap<String, Object> where = new HashMap<String, Object>(1,1);

	/**
	 * Creates a DBEngineRequest which the DBEngine uses to create a DELETE SQL statement to the DB.
	 * 
	 * @param id The ID of this engine request
	 * @param table The table name for this DELETE statement
	 * @param args HashMap containing the column name and the required column value for each. This is used
	 * 		in the where statement. <b>Must not be null!</b>
	 */
	public DeleteDBEReq(String id, DBEngine engine, String table, HashMap<String, Object> args) {
		super(id, engine, QueryType.DELETE, table);
    	
		if(args.isEmpty()) {
			LOG.warn("Empty where statement! Aborting DeleteDBEReq construction!");
		} else {
	    	String w = "";
	    	String[] colParams = args.keySet().toArray(new String[0]);
	    	Object[] vals = args.values().toArray(new String[0]);
	    	for(int i = 0; i < vals.length; i++) {
	    		String val = "";
	        	if(vals[i].getClass().equals(String.class)) { //if true, encloses the value in single quotes
	        		vals[i] = transformToQueryFriendlyString(vals[i].toString());
	        		val = "'" + vals[i] + "'";
	        	} else {
	        		val = vals[i].toString();
	        	}
	        	
	    		w = w +  colParams[i] + "=" + val + " AND ";
	    	}
	    	w = w.substring(0, w.length() - 4);
			setQuery("delete from " + table + " where " + w);
		}
	}
}
