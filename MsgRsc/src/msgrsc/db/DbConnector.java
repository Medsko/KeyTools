package msgrsc.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Creates and maintains a connection to the database. 
 */
public class DbConnector {

	private final static String LOCAL_USER_NAME_AND_PASSWORD = "db2admin"; 
	
	/** 
	 * Default host and port for a local connection. Append the name of your local database 
	 * and feed the result to {@link #getConnection(String)} and you're good to go. 
	 */
	public final static String URL_LOCAL_HOST_AND_PORT = "jdbc:db2://localhost:50000/";
	
	/** Default (local) URL for the connection. */
	public final static String URL_QISLOCAL = "jdbc:db2://localhost:50000/QISLOCAL";
	
	/**
	 * Creates a connection to the DB2 database at the specified URL, or at the default
	 * {@link #URL_QISLOCAL} if no URL is provided.
	 */
	public Connection getConnection(String localDatabaseUrl) throws SQLException {
		try {
			// Goddamn you Eclipse/DB2 for making me invoke the ancients.
			Class.forName("com.ibm.db2.jcc.DB2Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		Properties props = new Properties();
		props.setProperty("user", LOCAL_USER_NAME_AND_PASSWORD);
		props.setProperty("password", LOCAL_USER_NAME_AND_PASSWORD);
		props.setProperty("currentSchema", "QIS");
		
		if (localDatabaseUrl == null) {
			localDatabaseUrl = URL_QISLOCAL;
		}
		
		Connection connection = DriverManager.getConnection(localDatabaseUrl, props);
		
		return connection;
	}

	
}
