package symphony.bm.bm_logic_devices.engines.requests.DBEngine;

import bm.main.engines.DBEngine;

import java.util.HashMap;

public class UpdateDBEReq extends DBEngineRequest {

	/**
	 * Creates an DBEngineRequest which the DBEngine uses to create an UPDATE statement to the DB.
	 * 
	 * @param id The ID of this EngineRequest
	 * @param table The table name for this UPDATE statement
	 * @param vals HashMap containing names of the columns to be updated along with the new values 
	 * 		for each.
	 * @param args HashMap containing the column name and the required column value for each. This is used
	 * 		in the WHERE section.
	 */
	public UpdateDBEReq(String id, DBEngine engine,  String table, HashMap<String, Object> vals, HashMap<String, Object> args) {
		super(id, engine, QueryType.UPDATE, table);
		String q = "update " + table;
    	
    	//constructs set clause
		String set = " SET "; // where clause that ignores case
		Object[] valscols = vals.keySet().toArray();
		for(int i = 0; i < valscols.length; i++) {
			String col = (String) valscols[i];
			Object value = vals.get(col);
			
			if(value == null) {
				set = set + col + " = NULL";
			} else if(value.getClass().equals(String.class)) { //if value is a string
				value = transformToQueryFriendlyString(value.toString());
				set = set + col + "='" + value + "'";
			} else {
				set = set + col + " = " + value + "";
			}
			set += ", ";
		}
		set = set.substring(0, set.length() - 2); //cuts last comma and space in set String
		
		//constructs where clause
		String where = " WHERE "; // where clause that ignores case
		Object[] argscols = args.keySet().toArray();
		for(int i = 0; i < argscols.length; i++) {
			String col = (String) argscols[i];
			Object value = args.get(col);
			
			if(value.getClass().equals(String.class)) { //if value is a string
				value = transformToQueryFriendlyString(value.toString());
				where = where + "UPPER(" + col + ") LIKE UPPER('" + value + "')";
			} else {
				where = where + col + " = " + value + "";
			}
			where += " AND ";
		}
		where = where.substring(0, where.length() - 4); //cuts last AND in where String
		
		q += set + where;
		setQuery(q);
	}

}
