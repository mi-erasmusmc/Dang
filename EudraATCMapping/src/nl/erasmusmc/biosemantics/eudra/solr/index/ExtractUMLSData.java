package nl.erasmusmc.biosemantics.eudra.solr.index;

 
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.erasmusmc.biosemantics.eudra.Utils.Utils;
import nl.erasmusmc.biosemantics.eudra.drugs.EudraRecord;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.solr.client.solrj.SolrServerException;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import nl.erasmusmc.biosemantics.eudra.Utils.ConceptUtils;

public class ExtractUMLSData {
	
	String supressionListPath = "data/suppression.csv";
	
	public static void main(String[] args) throws ParseException, SQLException, SolrServerException, IOException {
		
		Options options = new Options().addOption( "i", true, "input file" ).addOption( "dir", true, "out put directory" )
				.addOption( "solr", true, "drug server" )
				.addOption( "dbserver", true, "mysql server" ).addOption( "d", true, "database" ).
				addOption( "u", true, "user" ).addOption( "p", true, "password" );;
		String solr = new PosixParser().parse( options, args).getOptionValue( "solr" );
		String server = new PosixParser().parse( options, args).getOptionValue( "dbserver" );
		String database = new PosixParser().parse( options, args).getOptionValue( "d" );
		String user = new PosixParser().parse( options, args).getOptionValue( "u" );
		String password = new PosixParser().parse( options, args).getOptionValue( "p" );
		String fInput = new PosixParser().parse( options, args).getOptionValue( "i" );
		String fOutputDir = new PosixParser().parse( options, args).getOptionValue( "dir" );
		
		if ( solr == null || server == null || database == null || user == null || password == null || fInput == null || fOutputDir == null ){
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( ExtractUMLSData.class.getCanonicalName(), options );
		}
		else {

			fOutputDir = fOutputDir.endsWith("/")?fOutputDir:fOutputDir+"/";

			 ExtractUMLSData process = new ExtractUMLSData();
			 process.extractDrugCombinations(server, user, password, database, fOutputDir);
			 System.out.println("Done!");
		}
		 
		
	}
	
	
	public void extractIngredients(String mysqlserver, String user, String password, String database, String fOutputDir ){
		 
		DbServer db = new DbServer(mysqlserver,user,password,database);

		String fOutput = fOutputDir + "Ingredients.CSV";

		ResultSet rs;
		String sql;

		String[] header = {"uuid","cui","drug", "tty", "drug_norm" ,"ingredient", "ingredient_norm", "ing_cui", "code", "rela", "sab"};
		

		sql = "SELECT DISTINCT CUI, DRUG, INGREDIENT, ING_CUI, `CODE`, RELA, SAB, TTY FROM `EUDRA_DRUGS` ";

		//sql = "SELECT DISTINCT CUI, DRUG, INGREDIENT, ING_CUI, `CODE`, RELA, SAB, TTY FROM `DRUGS` ";

		rs = db.query(sql);
		processData(fOutput, rs, header);
		//processData(fOutput, rs, header);
		System.out.println("Extracted to " + fOutput);
		
	}

	public void extractSubstances(String mysqlserver, String user, String password, String database, String fOutputDir ){

		DbServer db = new DbServer(mysqlserver,user,password,database);

		String fOutput = fOutputDir + "Substances.CSV";

		String dropvew = "DROP VIEW IF EXISTS `v_CUI_ATC`; ";
		String createview = " CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_CUI_ATC` AS "
				+ " SELECT distinct `MRCONSO`.`CUI` AS `CUI`,`MRCONSO`.`CODE` AS `CODE` from `MRCONSO` where (`MRCONSO`.`SAB` = 'ATC');";

		String sql = "SELECT DISTINCT c.CUI, c.STR AS DRUG, c.STR AS INGREDIENT, c.CUI AS ING_CUI,  c.`CODE`, NULL AS RELA, c.SAB, c.TTY ";
		sql	+= " FROM MRCONSO c  " ;
		sql += " WHERE SAB='ATC' AND c.LAT='ENG' ";

		// combine with all terms associated with CUIs from ATC source
		sql += " UNION ";
		sql += " SELECT DISTINCT c.CUI, c.STR AS DRUG, c.STR AS INGREDIENT, c.CUI AS ING_CUI,  v.`CODE`, NULL AS RELA, c.SAB, c.TTY ";
		sql += "  FROM MRCONSO c INNER JOIN  ";
		sql += " v_CUI_ATC as v ON v.CUI= c.CUI ";
		sql += " WHERE c.CUI IN (SELECT CUI FROM MRCONSO WHERE SAB='ATC') ";
		sql += " AND  c.STR NOT IN (select DISTINCT  STR FROM v_CUI_ATC) ";
		sql += " AND c.LAT='ENG' ";


		ResultSet rs;
		String[] header = {"uuid","cui","drug", "tty", "drug_norm" ,"ingredient", "ingredient_norm", "ing_cui", "code", "rela", "sab"};

		db.execute(dropvew);
		db.execute(createview);

		rs= db.query(sql);
		processData(fOutput,rs, header);


		System.out.println("Extracted to " + fOutput);

	}

	
	
	public void createWHOdictionary(String mysqlserver, String user, String password, String database, String fOutputDir){
		DbServer db = new DbServer(mysqlserver,user,password,database);

		String fOutput = fOutputDir + "WHO_Dict.CSV";

		ResultSet rs;
		String sql;

		//String[] header = {"uuid","cui","drug", "tty", "drug_norm" ,"ingredient", "ingredient_norm", "ing_cui", "code", "rela", "sab"};
		String[] header = {"cui","drug", "tty","ingredient", "ing_cui", "code", "rela", "sab"};

		sql = "SELECT NULL AS CUI, name AS DRUG, name AS INGREDIENT, NULL as ING_CUI, atc AS CODE, 'NO_RELA' as RELA, 'WHO' as SAB, 'PT' as TTY  FROM who ";

		rs= db.query(sql);

		try {


			saveData(fOutput, header);


			String  cui, drug, tty, ingredient, ing_cui,code,rela ,sab;

			String[] prefix = {"WHO00000","WHO0000", "WHO000", "WHO00", "WHO0", "WHO"};
			int k=0;
			int i =0;
			ArrayList<String[]> lines = new ArrayList<String[]>();
			int c = 1;
			if ( rs.first() ){
				do {

					drug = rs.getString("DRUG");
					ingredient = rs.getString("INGREDIENT");

					code = rs.getString("CODE");
					tty = rs.getString("TTY");
					rela = rs.getString("RELA");
					sab = rs.getString("SAB");

					cui = prefix[Integer.toString(c).length()] + c;
					ing_cui = cui;
					c++;

					//drug_norm = ConceptUtils.normalizeConcept(drug);
					//ingredient_norm = ConceptUtils.normalizeConcept(ingredient);
					//uuid = ConceptUtils.getUuid(cui, drug, ing_cui, ingredient, code, sab, rela, tty);
					String[] line = {cui,drug, tty, ingredient, ing_cui, code, rela, sab};


					lines.add(line);
					i ++;

					if (lines.size() % 1000 ==0){
						saveData(fOutput, lines, true);
						lines = new ArrayList<String[]>();
						System.out.println("processed " + (k += 1000) + "lines.");
					}


				}while(rs.next());

				saveData(fOutput, lines, true);
				System.out.println("Total: " +  i + "lines.");

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}

		System.out.println("Extracted to " + fOutput);
	}


	public void extractDrugCombinations(String mysqlserver, String user, String password, String database, String fOutputDir ){
		DbServer db = new DbServer(mysqlserver,user,password,database);

		String fOutput = fOutputDir + "EUDRA_DRUGS_COMBINATIONS.CSV";

		ResultSet rs;
		String sql;

		String[] header = {"cui","drug", "tty","ingredient", "ing_cui", "code", "rela", "sab"};
 
		sql = "SELECT DISTINCT CUI, DRUG, INGREDIENT, ING_CUI, `CODE`, RELA, SAB, TTY FROM EUDRA_DRUGS_ALL WHERE SAB='ATC' AND LENGTH(`CODE`)=7";

		rs = db.query(sql);
		processDrugCombinations(fOutput, rs, header);

		System.out.println("Extracted to " + fOutput);
	}



	public void processDrugCombinations(String fOutput, ResultSet rs, String[] header){

		//String pAnd = "\\band\\b";
		//Pattern p;
		//Matcher m;
		ArrayList<String>  substanceCombinations;

		try {

			if (header != null){
				saveData(fOutput, header);
			}

			String cui, drug, tty, ingredient, ing_cui,code,rela ,sab;

			 
			int i =0;
		 
			int count = 0;
			ArrayList<String[]> lines = new ArrayList<String[]>();
			
			if ( rs.first() ){
				do {

					ArrayList<String> substances = new ArrayList<>();
					substanceCombinations = new  ArrayList<String>();
					cui = rs.getString("CUI");
					drug = rs.getString("DRUG");
					ingredient = rs.getString("INGREDIENT");
					ing_cui = rs.getString("ING_CUI");
					code = rs.getString("CODE");
					//tty =  "REVERSED";
					tty = rs.getString("TTY");
					//rela = rs.getString("RELA");
					rela = "combinations";
					sab = rs.getString("SAB");

				 
					// if found x and y or x,y and z or x and y,z
					boolean found = false;
					if (drug.matches("(.*)\\band\\b(.*)")){

						System.out.println(cui + "\t" + drug);
						found = true;
						count++;
						String[] parts = drug.split("\\band\\b");
						for(String s: parts){
							if (s.trim().matches("(.*)[\\,](.*)")) {
								String[] subparts = s.trim().split(",");
								for (String sub : subparts) {
									//substances.add(sub.trim());
									if (!substanceFilter(sub.trim()).isEmpty()) {
										substances.add(substanceFilter(sub.trim()));
									}

								}
							// if matches /
							}else if (s.trim().matches("(.*)\\/(.*)")){
								String[] subparts = s.trim().split("/");
								for (String sub : subparts) {
									//substances.add(sub.trim());
									if (!substanceFilter(sub.trim()).isEmpty()) {
										substances.add(substanceFilter(sub.trim()));
									}

								}
							}else{
								//substances.add(s.trim());
								if (! substanceFilter(s.trim()).isEmpty()){
									substances.add(substanceFilter(s.trim()));
								}

							}
						}

					// if matches "in combination with"
					}else if (drug.matches("(.*)(\\bin\\b\\s?)(\\bcombination\\b\\s\\bwith\\b)(.*)")) {
						found = true;
						count++;
						System.out.println(cui + "\t" + drug);

						String[] parts = drug.split("(\\bin\\b\\s?)(\\bcombination\\b\\s\\bwith\\b)");
						for (String s : parts) {
							if (s.trim().matches("(.*)[\\,](.*)")) {
								String[] subparts = s.trim().split(",");
								for (String sub : subparts) {
									/*if (sub.trim().contains("psycholeptics")){
										substances.add("combinations");
									}else{
										if (!substanceFilter(sub).isEmpty()){
											substances.add( substanceFilter(sub));
										}
									}*/
									// if matches /
									if (sub.trim().matches("(.*)\\/(.*)")) {
										String[] subsubparts = s.trim().split("/");
										for (String ss : subsubparts) {
											//substances.add(sub.trim());
											if (!substanceFilter(ss.trim()).isEmpty()) {
												substances.add(substanceFilter(ss.trim()));
											}

										}
									}else if (!substanceFilter(sub).isEmpty()) {
										substances.add(substanceFilter(sub));
									}

								}
							} else {

								/*if (s.trim().contains("psycholeptics")){
									substances.add("combinations");
								}else{
									if (!substanceFilter(s).isEmpty()){
										substances.add( substanceFilter(s));
									}
								}*/

								if (!substanceFilter(s).isEmpty()) {
									substances.add(substanceFilter(s));
								}

							}
						}

						// if matches  /
					}else if (drug.matches("(.*)\\/(.*)")){
						found = true;
						count++;
						System.out.println(cui + "\t" + drug);
						
						String[] subparts = drug.trim().split("/");
						for (String sub : subparts) {
							//substances.add(sub.trim());
							if (!substanceFilter(sub.trim()).isEmpty()) {
								substances.add(substanceFilter(sub.trim()));
							}

						}
					// if matches  combinations incl./excl.	 psycholeptics
					/*}else if (drug.matches("(.*)(combinations)(.*)(psycholeptics)$")){
						String newS =  drug.replaceAll("(combinations)(.*)(psycholeptics)$","") + "combinations";
						substances.add(newS);*/

					// matches combinations with
					}else if (drug.matches("(.*)(\\bcombinations\\b)(\\s\\bwith\\b?)(.*)")){
						found = true;
						count++;
						System.out.println(cui + "\t" + drug);
						String[] parts = drug.split("\\bcombinations\\b(\\s\\bwith\\b?)");
						for(String s: parts){
							if (s.trim().matches("(.*)[\\,](.*)")){
								String[] subparts = s.trim().split(",");
								for(String sub : subparts){
									substances.add(sub.trim());
								}
							}else{
								substances.add(s.trim());
							}
						}
						
					// matches drug, combinations
					}else if (drug.matches("(.*)\\bcombinations\\b")){
						found = true;
						count++;
						System.out.println(cui + "\t" + drug);
						String[] parts = drug.split("(.*)\\bcombinations\\b");
						for(String s: parts){
							if (s.trim().matches("(.*)[\\,](.*)")){
								String[] subparts = s.trim().split(",");
								for(String sub : subparts){
									substances.add(sub.trim());
								}
							}else{
								substances.add(s.trim());
							}
						}
					}

 
					if (found){
						Utils.permute(substances, substances.size(), substanceCombinations);
						
						// TTY="REVERSED", rela = "combinations";
						processSubtanceCombinations(fOutput, substanceCombinations, cui, code, rela, sab, "REVERSED");

						// add original term
						substanceCombinations = new  ArrayList<String>();

						//rela = "combinations";
						substanceCombinations.add(drug);
						processSubtanceCombinations(fOutput, substanceCombinations, cui, code, rela, sab, tty);
					}
					
					i++;

				}while(rs.next());

				saveData(fOutput, lines, true);
				System.out.println("Total: " +  i + "lines.");
				System.out.println("Total combination drugs found: " + count);

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}

	}


	public String substanceFilter(String substance){
		 
		String newSub = substance.trim();
		String[] filters = {"combination with", "combinations with", "combinations", "in combination"};

		for(String f: filters){
			if (substance.matches("((.*)?)" + f + "((.*)?)")){
				//System.out.println("Matched: " + f);

				if (f.equalsIgnoreCase("other drugs")){
					substance = substance.replaceAll(f, "combinations").trim();
				}else{
					substance = substance.replaceAll(f, "").trim();
				}

				if (substance.isEmpty()){
					 return substance; // in case it just contains 1 one like "combinations"
				}

			}
		}

		return substance.trim();
	}


	public void processSubtanceCombinations(String fOutput, ArrayList<String> substances, String cui, String code,  String rela, String sab, String tty ){

		String uuid,  drug, drug_norm, ingredient, ingredient_norm,ing_cui;
		ArrayList<String[]> lines =  new ArrayList<String[]>();
		for(String substance : substances){
			ing_cui = cui;
			drug = substance;
			ingredient = drug;
			 
			//drug_norm = ConceptUtils.normalizeDrugName(drug);
	    	//ingredient_norm = ConceptUtils.normalizeConcept(ingredient);
			//uuid = ConceptUtils.getUuid(cui, drug, ing_cui, ingredient, code, sab, rela, tty);
			
			String[] line = { cui,drug, tty, ingredient, ing_cui, code, rela, sab};
			lines.add(line);
		}

		saveData(fOutput, lines, true);


	}

	
	public void processData(String fOutput, ResultSet rs, String[] header){
		try {
			
			if (header != null){
				 saveData(fOutput, header);
			}
			
		     String uuid, cui, drug, tty, drug_norm,ingredient, ingredient_norm,ing_cui,code,rela ,sab;

			 String[] prefix = {"W0000000","W000000", "W00000", "W0000", "W000", "W00", "W0", "W"};
		     int k=0;
			 int i =0;
		     ArrayList<String[]> lines = new ArrayList<String[]>();
			 int c = 1;
		     if ( rs.first() ){
					do {
						cui = rs.getString("CUI");
				        drug = rs.getString("DRUG");
				        ingredient = rs.getString("INGREDIENT");
				        ing_cui = rs.getString("ING_CUI");		       
				        code = rs.getString("CODE");
				        tty = rs.getString("TTY");
				        rela = rs.getString("RELA");
				        sab = rs.getString("SAB");

						/*if (!drug.matches("(.*)(\\,|and|combinations)(.*)")){
							rela="";
						}*/

						if (cui == null){
							cui = prefix[Integer.toString(c).length()] + c;
							c++;
						}

						drug = ConceptUtils.cleanDrugName(drug);
						ingredient = ConceptUtils.normalizeIngredient(ingredient);

				    	drug_norm = ConceptUtils.normalizeIngredient(drug);
				    	ingredient_norm = ConceptUtils.normalizeIngredient(ingredient);
				    	uuid = ConceptUtils.getUuid(cui, drug, ing_cui, ingredient, code, sab, rela, tty);
				    	String[] line = {uuid, cui,drug, tty, drug_norm,ingredient, ingredient_norm, ing_cui, code, rela, sab};
				    	
				    	// filter out 2 chars only
				    	if (drug_norm.length() > 2){
				    		lines.add(line);
							i ++;
				    	}
				    	 
				    	 if (lines.size() % 1000 ==0){
				    		 saveData(fOutput, lines, true);	
				    		 lines = new ArrayList<String[]>();
				    		 System.out.println("processed " + (k += 1000) + "lines.");
				    	 }
				      
				     
					}while(rs.next());
					
					saveData(fOutput, lines, true);
				 System.out.println("Total: " +  i + "lines.");

		     }
		    	 
		} catch (SQLException e) {		 
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("resource")
	public void process(String fInput, String fOutput){
	 
		String[] header = {"uuid","cui","drug", "tty", "drug_norm" ,"ingredient", "ingredient_norm", "ing_cui", "code", "rela", "sab"};
		 
		try{
			CSVReader reader = new CSVReader(new FileReader(fInput));
		     String [] nextLine; 
		     
		     if ( (nextLine = reader.readNext()) != null){		    	 
		    	 saveData(fOutput, header);
		     }
		     String uuid, cui, drug, tty, drug_norm,ingredient, ingredient_norm,ing_cui,code,rela,sab;
		     
		     int k=0;
		     ArrayList<String[]> lines = new ArrayList<String[]>();
		     while ((nextLine = reader.readNext()) != null) {		         
		    	 
		    	 cui = nextLine[0];
		    	 drug = nextLine[1];
		    	 tty = nextLine[2];
		    	 ingredient = nextLine[3];
		    	 ing_cui = nextLine[4];
		    	 code = nextLine[5];
		    	 rela = nextLine[6];
		    	 sab = nextLine[7];
		    	 
		    	 drug_norm = ConceptUtils.normalizeConcept(drug);
		    	 ingredient_norm = ConceptUtils.normalizeConcept(ingredient);
		    	 uuid = ConceptUtils.getUuid(cui, drug, ing_cui, ingredient, code, sab,rela, tty);
		    	 String[] line = {uuid, cui,drug, tty, drug_norm,ingredient, ingredient_norm, ing_cui, code, rela, sab};
		    	 
		    	 lines.add(line);
		    	 
		    	 if (lines.size() % 10000 ==0){
		    		 saveData(fOutput, lines, true);	
		    		 lines = new ArrayList<String[]>();
		    		 System.out.println("processed " + (k += 10000) + "lines.");
		    	 }
		     }
		     saveData(fOutput, lines, true);
		     
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
