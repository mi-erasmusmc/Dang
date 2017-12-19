package nl.erasmusmc.biosemantics.eudra.solr.index;

 
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.solr.client.solrj.SolrServerException;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import nl.erasmusmc.biosemantics.eudra.Utils.ConceptUtils;

public class ProcessDictionary {
	
	public static void main(String[] args) throws ParseException, SQLException, SolrServerException, IOException {
		
		
		
		Options options = new Options().addOption( "dir", true, "output dir" ).addOption( "f", true, "input file" )				 
				.addOption( "dbserver", true, "mysql server" ).addOption( "d", true, "database" ).
				addOption( "u", true, "user" ).addOption( "p", true, "password" );		 
		String server = new PosixParser().parse( options, args).getOptionValue( "dbserver" );
		String database = new PosixParser().parse( options, args).getOptionValue( "d" );
		String user = new PosixParser().parse( options, args).getOptionValue( "u" );
		String password = new PosixParser().parse( options, args).getOptionValue( "p" );
		String fInput = new PosixParser().parse( options, args).getOptionValue( "f" );
		String dir = new PosixParser().parse( options, args).getOptionValue( "dir" );
		
		if ( server == null || database == null || user == null || password == null || fInput == null || dir == null ){
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( ExtractUMLSData.class.getCanonicalName(), options );
		}else{
			 ProcessDictionary p = new ProcessDictionary();
			 p.process(server, user,password, database,dir);
			System.out.println("Done!");
		}
		
		
		
	}

	// String mysqlserver, String user, String password, String database, String fOutputDir
	/**
	 * @param mysqlserver
	 * @param user
	 * @param password
	 * @param database
	 * @param dir
	 * @param fCSV
	 */
	@SuppressWarnings("resource")
	public void process(String mysqlserver, String user, String password, String database, String dir){
	 
		String[] header = {"uuid","cui","drug", "tty", "drug_norm" ,"ingredient", "ingredient_norm", "ing_cui", "code", "rela", "sab"};
		dir = dir.endsWith("/")?dir:dir+"/";
		 
		String fOutput = dir + "EUDRA_DRUGS_DICT_" + new SimpleDateFormat("yyyy.MM.dd").format(new Date())  + ".CSV";
		
		DbServer db = new DbServer(mysqlserver,user,password,database);
		
		ResultSet rs ;
		String sql; 
		
		sql = "SELECT DISTINCT CUI, DRUG, TTY, INGREDIENT, ING_CUI, `CODE`, RELA, SAB FROM EUDRA_DRUGS ";

		rs= db.query(sql);
 
		
		try{
			 
		     String uuid, cui, drug, tty, drug_norm,ingredient, ingredient_norm,ing_cui,code,rela,sab;
		     
		     int k=0;
		     ArrayList<String[]> lines = new ArrayList<String[]>();
		     
		     
		     if ( rs.first() ){
		    	 
		    	 	saveData(fOutput, header);
		    	 
					do {
						 
						 cui = rs.getString("CUI");
				    	 drug = rs.getString("DRUG");
				    	 tty = rs.getString("TTY");
				    	 ingredient = rs.getString("INGREDIENT");
				    	 ing_cui = rs.getString("ING_CUI");
				    	 code = rs.getString("CODE");
				    	 rela = rs.getString("RELA");
				    	 sab = rs.getString("SAB");
				    	 
				    	 drug = ConceptUtils.cleanDrugName(drug);
				    	 ingredient = ConceptUtils.cleanIngredient(ingredient);
				    	 
				    	 drug_norm = ConceptUtils.normalizeDrugName(drug);
				    	 ingredient_norm = ConceptUtils.normalizeIngredient(ingredient);
				    	 
				    	 uuid = ConceptUtils.getUuid(cui, drug, ing_cui, ingredient, code, sab, rela, tty);
				    	 
				    	 
				    	 
				    	 // ignore drug/substance with one or two characters
				    	 if (drug.length() > 2){
				    		  
				    		 String[] line = {uuid, cui,drug, tty, drug_norm,ingredient, ingredient_norm, ing_cui, code, rela, sab};
			    			 lines.add(line);				    		
				    		 
				    	 }
				    	 
				    	 
				    	 if (lines.size() % 10000 ==0){
				    		 saveData(fOutput, lines, true);	
				    		 lines = new ArrayList<String[]>();
				    		 System.out.println("processed " + (k += 10000) + "lines.");
				    	 }
						
					}while(rs.next());
					
		     }
		     
		    saveData(fOutput, lines, true);
		     
		} catch (SQLException e) {
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
	}

	
	
	public void saveData(String filename, String[] line){
	    	
	    	CSVWriter writer;
			try {
				
				writer = new CSVWriter(new FileWriter(filename, false), ',', CSVWriter.DEFAULT_QUOTE_CHARACTER);
				writer.writeNext(line);
		    	writer.close();
		    	
			} catch (IOException e) {
				 
				System.out.println("Could not write data to csv file.");
				e.printStackTrace();
			}
	    	
	    	
	 }
	
	
	public void saveData(String filename, ArrayList<String[]> lines, Boolean append){
    	
    	CSVWriter writer;
		try {
			
			writer = new CSVWriter(new FileWriter(filename, append), ',', CSVWriter.DEFAULT_QUOTE_CHARACTER);
			for(String[] line : lines){
				writer.writeNext(line);
			}
			
	    	writer.close();
	    	
		} catch (IOException e) {
			 
			System.out.println("Could not write data to csv file.");
			e.printStackTrace();
		}
    	 
	}
}
