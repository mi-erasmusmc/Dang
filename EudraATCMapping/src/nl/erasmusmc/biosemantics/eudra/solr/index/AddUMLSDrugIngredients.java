package nl.erasmusmc.biosemantics.eudra.solr.index;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import nl.erasmusmc.biosemantics.eudra.Utils.ConceptUtils;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;

 

public class AddUMLSDrugIngredients {
	
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
			formatter.printHelp( AddUMLSDrugIngredients.class.getCanonicalName(), options );
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
		//-- CUI2 (drug) ---> CUI1 (substance)	
		/*
		String sql = " SELECT DISTINCT  r.CUI, r.DRUG, r.INGREDIENT, r.ING_CUI,  r.`CODE`, r.RELA, r.SAB ";
				sql+= " FROM  MRREL_SUBSET_HAS_ATC r";
				sql += " UNION " ; 
				sql += " SELECT DISTINCT  r.CUI2 AS CUI, r.STR2 as DRUG, r.STR1 as INGREDIENT, r.CUI1 as ING_CUI,  r.ATC , r.RELA, r.SAB ";
				sql += " FROM MRREL_SUBSET_WHO_ATC r ";
				sql += " WHERE  r.CUI1 NOT In (SELECT CUI FROM MRREL_SUBSET_HAS_ATC) ";
				sql += " AND r.RELA  IN (\"has_ingredient\", \"has_ingredients\", \"has_active_ingredient\",\"has_direct_substance\", \"tradename_of\", \"has_precise_ingredient\",\"form_of\") "; 
				sql += " UNION "; 
				sql += " SELECT DISTINCT  r.CUI2 AS CUI, r.STR2 as DRUG, r.STR1 as INGREDIENT, r.CUI1 as ING_CUI,  r.ATC , r.RELA, r.SAB ";
				sql += " FROM MRREL_SUBSET_WHO_ATC r ";
				sql += " WHERE  r.CUI2 NOT In (SELECT CUI FROM MRREL_SUBSET_HAS_ATC) ";
				sql += " AND r.RELA  IN (\"active_ingredient_of\",\"ingredient_of\",\"ingredients_of\", \"precise_ingredient_of\",\"has_form\") ";
				
				sql += " FROM MRREL_SUBSET r INNER JOIN v_CUI_ATC as a ON r.CUI1 = a.CUI ";
				sql += " WHERE r.RELA  IN (\"active_ingredient_of\",\"has_ingredient\", \"has_ingredients\", \"has_active_ingredient\",\"has_direct_substance\", \"tradename_of\", \"has_precise_ingredient\",\"form_of\")";
				sql += " UNION ";
				// -- CUI1 (drug) ---> CUI2 (substance)
				sql += " SELECT DISTINCT r.CUI1 AS CUI, r.STR1 as DRUG, r.STR2 as INGREDIENT, r.CUI2 as ING_CUI,  a.`CODE`, r.RELA, r.SAB ";
				sql += " FROM MRREL_SUBSET r INNER JOIN v_CUI_ATC as a ON r.CUI2 = a.CUI ";
				sql += " WHERE r.RELA  IN (\"ingredient_of\",\"ingredients_of\", \"precise_ingredient_of\",\"has_form\") ";
				
				sql += " ORDER BY INGREDIENT ";*/
				
				String sql = "SELECT DISTINCT CUI, DRUG, INGREDIENT, ING_CUI, `CODE`, RELA, SAB, TTY FROM `DRUG_INGREDIENTS_ALL` WHERE LENGTH(`CODE`)=7";
				
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
		        String drug = rs.getString("DRUG");
		        String ingredient = rs.getString("INGREDIENT");
		        String ing_cui = rs.getString("ING_CUI");		       
		        String code = rs.getString("CODE");
		        String rela = rs.getString("rela");
		        String sab = rs.getString("SAB");
				String tty = rs.getString("TTY");
		        
		        doc.addField("uuid", ConceptUtils.getUuid(cui, drug, ing_cui,ingredient,code,sab,rela,tty));
		        doc.addField("cui", cui );
		        doc.addField("drug", drug);
		        doc.addField("ingredient", ingredient );
		        doc.addField("ingredient_norm", ConceptUtils.normalizeConcept(ingredient));
		        doc.addField("ing_cui", ing_cui );
		        doc.addField("code", code );
		        doc.addField("rela", rela );
		        doc.addField("sab", sab );
		        
		        solr.add( doc );
		        if ( cnt % 10000 == 0 ) {
		        	solr.commit();
		        	System.out.println("Commiting 10000 documents....");
		        }
			} while ( rs.next() );
        	solr.commit();
		}
	}
	
/*	public static String normalizeString(String str){
		// remove punctuation and symbols  [!"\#$%&'()*+,\-./:;<=>?@\[\\\]^_`{|}~]\
		
		Pattern punctPattern = Pattern.compile("\\p{Punct}");
		Pattern spacePattern = Pattern.compile("\\s+");
		String normalized = punctPattern.matcher(str.toLowerCase()).replaceAll(" ");
		normalized = spacePattern.matcher(normalized).replaceAll(" ");
		
		return normalized;
	}
	
	private static String generateKey( String text ){
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA1");
			md.update(text.getBytes());
			byte[] mdbytes = md.digest();

			//convert the byte to hex format
			StringBuffer sb = new StringBuffer("");
			for (int i = 0; i < mdbytes.length; i++) {
				sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}*/
}
