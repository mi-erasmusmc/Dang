/**
 * 
 */
package nl.erasmusmc.biosemantics.eudra.drugs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author dangvh
 *
 */
public class RxNormServer {
	
	private Connection connection;
	private String server;
	private String database;
	private String user;
	private String password;
	
	
	/**
	 * @param server
	 * @param database
	 * @param user
	 * @param password
	 */
	public RxNormServer(String server, String database, String user, String password){
		this.server = server;
		this.database = database;
		this.user = user;
		this. password = password;
		this.connection = null;
		connect();
	}

	public void connect(){
		
		try {
			connection = DriverManager.getConnection( "jdbc:mysql://" + server  + "/" + database + "?" + "user=" + user + "&password=" + password + 
					  "&connectTimeout=0&socketTimeout=0&autoReconnect=true&useUnicode=yes&characterEncoding=UTF-8" );
			//Connection connection = DriverManager.getConnection( "jdbc:mariadb://" + server  + "/" + database, user, password );
			 
		} catch (SQLException e) {			
			e.printStackTrace();
		}

	}
	
	public String getRxCui(String cui){
		String rxCui = null;
		
		if (this.connection == null){
			connect();
		}
		
		
		String sql = "SELECT CUI, `CODE` FROM MRCONSO WHERE CUI=\"" + cui + "\" AND SAB=\"RXNORM\"";
		
		
		try {
			
			PreparedStatement statement = connection.prepareStatement(sql);
			
			ResultSet rs = statement.executeQuery();
			
			if (rs.first()){
				rxCui = rs.getString("CODE");
			}
			
			
		} catch (SQLException e) {
			
			e.printStackTrace();
		}
		
		return rxCui;
	}
}
