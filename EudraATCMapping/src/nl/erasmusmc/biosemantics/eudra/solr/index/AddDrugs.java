package nl.erasmusmc.biosemantics.eudra.solr.index;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;

import nl.erasmusmc.biosemantics.eudra.Utils.ConceptUtils;

public class AddDrugs {
	
	public static void main(String[] args) throws ParseException, SQLException, SolrServerException, IOException {
		Options options = new Options().addOption( "solr", true, "drug server" ).addOption( "s", true, "mysql server" ).addOption( "d", true, "database" ).
										addOption( "u", true, "user" ).addOption( "p", true, "password" );
		String url = new PosixParser().parse( options, args).getOptionValue( "solr" );
		String server = new PosixParser().parse( options, args).getOptionValue( "s" );
		String database = new PosixParser().parse( options, args).getOptionValue( "d" );
		String user = new PosixParser().parse( options, args).getOptionValue( "u" );
		String password = new PosixParser().parse( options, args).getOptionValue( "p" );
		if ( url == null || server == null || database == null || user == null || password == null ){
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( AddDrugs.class.getCanonicalName(), options );
		}
		else {
			process( new HttpSolrClient( url, HttpClientBuilder.create().setUserAgent( "MyAgent" ).setMaxConnPerRoute(4).build() ), server, database, user, password );
			System.out.println("Done!");
		}
	}
	
	
	private static void process(HttpSolrClient solr, String server, String database, String user, String password) throws SQLException, SolrServerException, IOException {
		
		Connection connection = DriverManager.getConnection( "jdbc:mysql://" + server  + "/" + database + "?" + "user=" + user + "&password=" + password + 
												  "&connectTimeout=0&socketTimeout=0&autoReconnect=true&useUnicode=yes&characterEncoding=UTF-8" );
		
		//Connection connection = DriverManager.getConnection( "jdbc:mariadb://" + server  + "/" + database, user, password );
		
		// SELECT all terms have ATC code from ATC source SAB = "ATC"
		/*String sql = "SELECT DISTINCT c.CUI, c.STR,  c.`CODE`, c.TTY, c.SAB ";
				sql	+= " FROM MRCONSO c  " ;
				sql += " WHERE SAB='ATC' AND c.LAT='ENG' ";
				
				// combine with all terms associated with CUIs from ATC source 
				sql += " UNION ";
				sql += "SELECT DISTINCT c.CUI, c.STR,  v.`CODE`, c.TTY, c.SAB ";
				sql += "  FROM MRCONSO c INNER JOIN  "; 
				sql += " ( select distinct `MRCONSO`.`CUI` AS `CUI`,`MRCONSO`.`CODE` AS `CODE` from `MRCONSO` where (`MRCONSO`.`SAB` = 'ATC') ) as v ON v.CUI= c.CUI ";
				sql += " WHERE c.CUI IN (SELECT CUI FROM MRCONSO WHERE SAB='ATC') ";
				sql += " AND  c.STR NOT IN (select DISTINCT  STR FROM MRCONSO WHERE SAB='ATC') ";
				sql += " AND c.LAT='ENG' ";*/

		String sql = "SELECT DISTINCT CUI, DRUG, INGREDIENT, ING_CUI, `CODE`, RELA, SAB, TTY FROM `DRUGS` ";
				
		PreparedStatement statement = connection.prepareStatement(sql);
		System.out.println("Preparing statement....");
		ResultSet rs = statement.executeQuery();
		System.out.println("Quering database...");
		int cnt = 0;
		if ( rs.first() ){
			do {
				cnt++;
		        SolrInputDocument doc = new SolrInputDocument();

		        String cui = rs.getString("CUI");
		        String drug = rs.getString("STR");
		        String ingredient = rs.getString("STR");
		        String ing_cui = rs.getString("CUI");		       
		        String code = rs.getString("CODE");
		        String tty = rs.getString("TTY");
		        String rela =  rs.getString("RELA");
		        String sab = rs.getString("SAB");
		        
		        doc.addField("uuid", ConceptUtils.getUuid(cui, drug, ing_cui, ingredient, code, sab, rela, tty) );
		        doc.addField("cui", cui );
		        doc.addField("drug", drug);
		        doc.addField("drug_norm", ConceptUtils.normalizeConcept(drug));
		        doc.addField("ingredient", ingredient );
		        doc.addField("ingredient_norm", ConceptUtils.normalizeConcept(ingredient));
		        doc.addField("ing_cui", ing_cui );
		        doc.addField("code", code );
		        doc.addField("rela", rela );
		        doc.addField("sab", sab );
		        doc.addField("tty", tty);
		      
		        
		        solr.add( doc );
		        if ( cnt % 10000 == 0 ) {
		        	solr.commit();
		        	System.out.println("Commiting 10000 documents....");
		        }
			} while ( rs.next() );
        	solr.commit();
		}
	}


	
	
}
