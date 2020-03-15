package symphony.bm.bm_logic_devices.engines;

import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.DBEngine.DBEngineRequest;
import bm.main.engines.requests.EngineRequest;
import bm.tools.SystemTimer;

import java.sql.*;
import java.util.TimerTask;

//FIXME Fix unlimited loop when DB is not on
public class DBEngine extends AbstEngine {
	private String dbURL;
    private Connection conn;
    private String dbusr;
    private String dbpwd;
    private boolean connected = false;
    
    /**
     * Primitive instantiation of DBEngine. Automatically connects itself to DB.
     * @param name
     * @param dbURL
     * @param dbusr
     * @param dbpwd
     */
    public DBEngine(String name, String logDomain, String errorLogDomain, String dbURL, String dbusr, String dbpwd, 
    		SystemTimer sysTimer, int reconnectPeriod, int timeout) {
    	super(logDomain, errorLogDomain, name, DBEngine.class.toString());
    	this.dbURL = dbURL;
    	this.dbusr = dbusr;
    	this.dbpwd = dbpwd;
		createConnection(dbURL, dbusr, dbpwd);
		DBEngineConnectionReconnector reconnector = new DBEngineConnectionReconnector(timeout, reconnectPeriod);
		sysTimer.schedule(reconnector, 0, reconnectPeriod);
		LOG.info(name + " started @ URL: " + dbURL);
    }
    
    public void createConnection(String dbURL, String dbusr, String dbpwd) {
    	this.dbURL = dbURL;
    	this.dbusr = dbusr;
    	this.dbpwd = dbpwd;
    	try {
    			LOG.trace("Connecting to DB @ URL: " + dbURL);
			conn = (DriverManager.getConnection(dbURL, dbusr, dbpwd));
			LOG.trace("Connected to DB!");
			connected = true;
		} catch (SQLException e) {
			LOG.warn("Cannot connect to DB! Wait for reconnection...");
			connected = false;
		}
    }
    
    public void closeConnection() throws SQLException {
    	conn.close();
    	LOG.info("Disconnected from Deby DB!");
    }
    

	@Override
	protected Object processRequest(EngineRequest er) throws EngineException {
		DBEngineRequest dber = (DBEngineRequest) er;
		try {
			Object o = executeQuery(dber.getQuery());
			return o;
		} catch(SQLNonTransientConnectionException e) {
			connected = false;
			return (new EngineException(this, "Connection lost with DB! Cannot execute query! Wait for "
					+ "reconnection..."));
		} catch (SQLException e) {
			return (new EngineException(this, "Cannot execute query [" + dber.getQuery() + "]! Check causes for "
					+ "more details.", e));
		} catch(NullPointerException e) {
    			return new EngineException(this, "Connection not yet established!");
    		}
	}
    
    private Object executeQuery(String query) throws SQLException, NullPointerException {
        Statement stmt = null;
        stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY);
        LOG.trace("Executing " + query + " ...");
        stmt.execute(query);
        LOG.trace("Query executed successfully!");
        if(stmt.getResultSet() == null) { //usually the case for non-select queries
            return true;
        } else {
            return stmt.getResultSet();
        }
    }

	public String getDbURL() {
		return dbURL;
	}
	
	private class DBEngineConnectionReconnector extends TimerTask {
    	private int timeout;
    	private int reconnectPeriod;
    	private int attempts = 0;

		public DBEngineConnectionReconnector(int timeout, int reconnectPeriod) {
			this.timeout = timeout;
			this.reconnectPeriod = reconnectPeriod;
		}

		@Override
		public void run() {
			if(!connected) {
				attempts++;
				LOG.info("Reconnecting to DB...");
				createConnection(dbURL, dbusr, dbpwd);
				if(attempts * reconnectPeriod >= timeout) {
					LOG.error(getName() + " Engine couldn't connect to " + dbURL + "! Some processes " +
							"may not work!");
				}
			}
		}
	}
}
