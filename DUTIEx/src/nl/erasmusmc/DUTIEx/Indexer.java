package nl.erasmusmc.DUTIEx;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.erasmusmc.data_mining.ontology.api.Language;


public class Indexer {
	
	private String ontologyFileNL = "Resources/UMLS2015ab_disease_DUT_6_jun_2016.ontology";
	private String ontologyFileEN = "Resources/UMLS2015ab_disease_ENG_6_jun_2016.ontology";
	private String base_dir = "/media/data/SPCExtraction/";
	private String indi_dir = base_dir + "IndiText/";
	 
	static Logger logger = Logger.getLogger(Indexer.class.getName());
	
	
	public static void main(String[] args) {
		 
		Indexer indexer = new Indexer();
		indexer.indexSPC(Language.EN);
		System.out.println("Done!");

	}
	
	public void indexSPC(Language lang){
			 
			String output =  base_dir + "indexing_results_Disambiguate.txt";
			String ontologyFile = lang==Language.NL?ontologyFileNL:ontologyFileEN;
		 	
			PeregrineIndexer pr = new PeregrineIndexer(lang,ontologyFile);		
		    pr.start();
		    System.out.print("---> Loading corpus");
		    
		    Map<String, String> indiList = loadIndication(indi_dir);	    
		    
		    ArrayList<ResultSet> rsList = new ArrayList<ResultSet>();
		    ResultSet rs;
		    
		    for (Map.Entry<String, String> entry:  indiList.entrySet()){
		    	
		    	rs = new ResultSet();	    	
		    	
		    	System.out.println("------------------------------------------------------------------------------------------");
		    	logger.info("------------------------------------------------------------------------------------------");
		    	//System.out.println("Text: " + entry.getValue());
		    	logger.info(entry.getKey());
		    	logger.info("Text: " + entry.getValue());
			    
			    System.out.println("Indexing concept...");	    
			    
			    rs = pr.indexConcept(entry.getValue());		    
			    rsList.add(rs);
			    rs.setFilename(entry.getKey());
		    	rs.setTxt(entry.getValue());
			    System.out.println("done!");
			    
		    }
		    
		     
			FileWriter cwriter;
			String line;
			try {
				cwriter = new FileWriter(output);
				for (ResultSet r : rsList) {
	
					System.out.println("File: " + r.getFilename());
					System.out.println("Indication: " + r.getTxt());
					//line = "\n--------------------------------------------------------\n";
					line = String.format("-File: %s\n-Indication:\n%s\n", r.getFilename(), r.getTxt());
	
					for (IndexedConcept ic : r.getConcept()) {
						System.out.println("-CUI: " + ic.getCui() + "\tPreferred term: " + ic.getPreferred_term());
						line += String.format("-CUI: %s \tPreferred term: %s\n", ic.getCui(), ic.getPreferred_term());					
						for (String s : ic.getFound_textList()) {
							System.out.println("\tFound text:" + s);
							line += String.format("\tFound text: %s\n", s);
						}
						
					}
					
					line += "\n--------------------------------------------------------\n";
					cwriter.write(line);
					System.out.println();
	
				}
	
				cwriter.close();
	
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
	    	 System.out.println("done!");
	}


	public ArrayList<CorpusItem> loadCorpus(String filename){
		ArrayList<CorpusItem> list  = new ArrayList<CorpusItem>();
		BufferedReader br;
		try {
	        br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "utf-8"));         

	        String line;
	        String[] parts;
	        CorpusItem item;
	        while ((line = br.readLine()) != null) {	        	
	        	if (line.contains(";")) {
	        		item = new CorpusItem();
	        		parts = line.split(";");
	        		
	        		item.setDoc_id(Integer.parseInt(parts[0]));
	        		item.setSentence_id(Integer.parseInt(parts[1]));
		        	item.setTxt(parts[2]);		        	
		        	list.add(item);
	        	} else {
	        	    throw new IllegalArgumentException("String " + line + " is not a valid string");
	        	}
	        	
	        }
	        br.close();

	    } catch (IOException e) {
	        e.printStackTrace();
	    } 
		  
		return list;
		
	}
	
	
	public Map<String, String> loadIndication(String dir){
		Map<String, String> list = new HashMap<String, String>();
		File folder = new File(dir);
		File[] listOfFiles = folder.listFiles();
		String text;
		for (File file : listOfFiles) {
		    if (file.isFile() && getFileExtension(file).equals("txt")) {		     
		       
		        try{
		        	 text = new String(Files.readAllBytes(Paths.get(file.getPath())));
		        	 list.put( file.getName() ,text);
		        }catch (IOException ie){
		        	System.out.println(ie.toString());
		        }
		       
		    }
		     
		}
		
		return list;
	}
    
	public String getFileExtension(File f) {
	    String ext = null;
	    String s = f.getName();
	    int i = s.lastIndexOf('.');

	    if (i > 0 &&  i < s.length() - 1) {
	        ext = s.substring(i+1).toLowerCase();
	    }
	     
	    return ext;
	}
	 
	
 
}
