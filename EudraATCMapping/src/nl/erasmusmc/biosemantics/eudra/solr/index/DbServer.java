/**
 * 
 */
package nl.erasmusmc.biosemantics.eudra.solr.index;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.sql.ResultSet;
/**
 * @author haidangvo
 *
 */
public class DbServer {
	private String server;
	private String user;
	private String password;
	private String database;
	private Connection connection;
	private ResultSet resultset;
	
	public DbServer(){
		this.user = null;
		this.server = null;
		this.password = null;
		this.database = null;
		this.connection = null;
		this.resultset = null;
	}
	
	public DbServer(String server, String user, String password, String database){
		this.server = server;
		this.user = user;
		this.password = password;
		this.database = database;
		this.connection = null;
		this.resultset = null;
	}
	
	public void connect(){		
		try {
			connection = DriverManager.getConnection( "jdbc:mysql://" + server  + "/" + database + "?" + "user=" + user + "&password=" + password + 
					  "&connectTimeout=0&socketTimeout=0&autoReconnect=true&useUnicode=yes&characterEncoding=UTF-8" );
		} catch (SQLException e) {			 
			e.printStackTrace();
		}
	}
	
	public ResultSet query(String sql){
		if (connection == null){
			connect();
		}
		try {
			
			PreparedStatement statement = connection.prepareStatement(sql); 
			
			System.out.println("querying database...");
			resultset = statement.executeQuery();
			
		} catch (SQLException e) {
			 
			e.printStackTrace();
		}
		 
		return resultset;
	}
	
	public void execute(String sql){
		if (connection == null){
			connect();
		}
		PreparedStatement statement;
		try {
			statement = connection.prepareStatement(sql);
			statement.execute();
		} catch (SQLException e) {			 
			e.printStackTrace();
		}
		
		
	}
	
	public ResultSet query(String sql, Object... params){
		if (connection == null){
			connect();
		}
		try {
			
			PreparedStatement statement = connection.prepareStatement(sql);
			int i = 1;
			for(Object p : params){
				if (p instanceof Integer){				 
					statement.setInt(i++, (Integer) p);
				}else if (p instanceof String){
					statement.setString(i++, (String)p);
				}else if (p instanceof Float){
					statement.setFloat(i, (float)p); 
				}else if (p instanceof Double){
					statement.setDouble(i++, (Double)p);
				}else if (p instanceof Date){
					statement.setDate(i++, (java.sql.Date)p);
				}else{
					throw new SQLException();
				}
			}
			
			System.out.println("querying database...");
			resultset = statement.executeQuery();
			
		} catch (SQLException e) {
			 
			e.printStackTrace();
		}
		 
		return resultset;
	}
	
	public void close(){
		try {
			resultset.close();
			connection.close();
		} catch (SQLException e) {		 
			
		}
	}
	
	 
	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public ResultSet getResultset() {
		return resultset;
	}

	public void setResultset(ResultSet resultset) {
		this.resultset = resultset;
	}
	
	
	
}
