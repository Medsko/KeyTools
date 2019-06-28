package msgrsc.db;

import java.io.IOException;
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
public class McGarnagle implements AutoCloseable {

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
			System.out.println("Failed query: " + sql);
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
	
	public Integer getInt(String columnName) throws SQLException {
		return resultSet.getInt(columnName);
	}
	
	public Clob createClob() throws SQLException {
		return connection.createClob();
	}
	
	public Clob getClob(String columnName) throws SQLException {
		return resultSet.getClob(columnName);
	}
	
	public boolean next() throws SQLException {
		if (resultSet.isClosed())
			return false;
		return resultSet.next();
	}
	
	@Deprecated
	// Use close().
	public void closeConnection() {
		try {
			if (connection != null)
				connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() throws SQLException {
		try {
			if (connection != null)
				connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		}		
	}

}
