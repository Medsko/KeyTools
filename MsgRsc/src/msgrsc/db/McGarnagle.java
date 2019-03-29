package msgrsc.db;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 'cause he gets results. From the database. In sets. 
 * When you feed him queries to execute.
 */
public class McGarnagle {

	private Connection connection;
	
	private ResultSet resultSet;
	
	public McGarnagle(String localDatabaseUrl) throws SQLException {
		DbConnector connector = new DbConnector();
		connection = connector.getConnection(localDatabaseUrl);
	}
	
	public boolean executeQuery(String sql) {
		try {
			Statement statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			return resultSet.next();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean execute(String dml) {
		try {
			Statement statement = connection.createStatement();
			statement.execute(dml);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public Date getDateResult(String columnName) throws SQLException {
		if (resultSet != null)
			return resultSet.getDate(columnName);
		else
			return null;
	}
	
	public String getString(String columnName) throws SQLException {
		return resultSet.getString(columnName);
	}
	
	public Clob createClob() throws SQLException {
		return connection.createClob();
	}
	
	public Clob getClob(String columnName) throws SQLException {
		return resultSet.getClob(columnName);
	}
	
	public boolean next() throws SQLException {
		return resultSet.next();
	}
	
	public void closeConnection() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
