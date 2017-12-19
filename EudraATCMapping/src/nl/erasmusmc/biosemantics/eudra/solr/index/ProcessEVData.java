package nl.erasmusmc.biosemantics.eudra.solr.index;


import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.solr.client.solrj.SolrServerException;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import nl.erasmusmc.biosemantics.eudra.drugs.EudraCorpus;
import nl.erasmusmc.biosemantics.eudra.drugs.EudraRecord;

public class ProcessEVData {
	
	private static String fInput = "data/FULL_LSIT_EV_Ped_and_Adults_remove_duplicated.csv";
	private static String fOutput = "data/";
	
	
	
	public static void main(String[] args) throws ParseException, SQLException, SolrServerException, IOException {
		
	 
			Options options = new Options().addOption( "i", true, "input file" ).addOption( "o", true, "out put file" );
				 
			/*fInput = new PosixParser().parse( options, args).getOptionValue( "i" );
			fOutput = new PosixParser().parse( options, args).getOptionValue( "o" );
			*/
			/*if ( fInput == null || fOutput == null ){
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp( ProcessEVData.class.getCanonicalName(), options );
				
			}else {
				ProcessEVData p = new ProcessEVData();
				//p.process();
				p.processMedication("data/medicationlist_EV_20160630_full.csv", "data/medicationList_EV_20160630_extracted.csv");
				System.out.println("Done!");
			}*/
			
			ProcessEVData p = new ProcessEVData();
			p.process();
			System.out.println("done!");
	}
	
	
	public void process(){
		
		EudraCorpus corpus = new EudraCorpus(fInput);
		List<Map<String, Integer>>  wordCountList;
		SortableValueMap<String, Integer> allWords = new SortableValueMap<String, Integer>();
		
		int maxWord = 0;
		ArrayList<String[]> wordList = new ArrayList<String[]>();
		
		for(EudraRecord r : corpus.getRecords()){
			String[] words = r.getSubstance().replaceAll("/\\s{2,}/g", "").split(" ");
			wordList.add(words);
			maxWord = words.length>maxWord?words.length:maxWord;
	
			for(String w : words){
				if (!allWords.containsKey(w)){
					allWords.put(w, 1);					
				}else{
					allWords.put(w, allWords.get(w) + 1);
				}				 
			}			
			//System.out.println(String.join("|", words));			
		}
		
		wordCountList = new ArrayList<Map<String, Integer>>(maxWord);
		
		for(int k =0; k<maxWord; k++){
			SortableValueMap<String, Integer> list = new SortableValueMap<String, Integer>();
			for(String[] words : wordList){		
				 if (words.length > k){
					 if (!list.containsKey(words[k])){
							list.put(words[k],1);
						}else{
							list.put(words[k], list.get(words[k]) + 1);
						} 
				 }
				 
			}
			list.sortByValue();			
			wordCountList.add(k, list);
		}
		
		 
		allWords.sortByValue();
		String[] header = {"word", "freq"};
		
		saveData("data/allWords.csv",header, false);
		
		for (String key : allWords.keySet()){
			String[] line = new String[2];
			line[0] = key;
			line[1] = allWords.get(key).toString();
			saveData("data/allWords.csv",line, true);
		}
		
		Map<String, Integer> list = new HashMap<String, Integer>();
		
		
		list  = wordCountList.get(0);
		ArrayList<String[]> matrix = new ArrayList<String[]>();
		
		header = new String[maxWord*2]; 
		for(int i =0; i < maxWord; i++){
			header[i*2] = "word" + Integer.toString(i+1);
			header[i*2+1] = "freq";			
		}
		
		saveData("data/all.csv",header, false);
		
		for(String key : list.keySet()){
			String[] row = new String[maxWord*2];
			row[0] = key;
			row[1] = list.get(key).toString();					
			matrix.add(row);
		}
		
		
		
		 for(int i = 1; i < wordCountList.size(); i++){
			int index = 0;
			for(String key : wordCountList.get(i).keySet()){			 
				matrix.get(index)[i*2] = key;
				matrix.get(index)[i*2+1] = wordCountList.get(i).get(key).toString();
				index++;
			}
		}
		
		 
		
		for(String[] line : matrix){
			saveData("data/all.csv", line, true);
		}
		
		// generate word | count by position
		ArrayList<String[]> pos = new ArrayList<String[]>();
		
		for (String w : allWords.keySet()){
			String[] r = new String[maxWord + 1];
			r[0] = w;
			for (int i=0; i < wordCountList.size(); i++){
				for (String key : wordCountList.get(i).keySet()){
					if (key.equals(w)){
						r[i+1] = wordCountList.get(i).get(key).toString();
						break;
					}
				}
			}
			pos.add(r);
		}
		
		header = new String[maxWord + 1];
		header[0] = "word";
		
		for(int i = 1; i <= maxWord; i++){
			header[i] = "pos" + i;
		}
		
		saveData("data/orderbyPos.csv", header, false);
		saveData("data/orderbyPos.csv", pos, true);
	}
	
  
	public void saveData(String filename, String[] line, boolean append){
    	
    	CSVWriter writer;
		try {
			
			writer = new CSVWriter(new FileWriter(filename, append), ',', CSVWriter.DEFAULT_QUOTE_CHARACTER);
			writer.writeNext(line);
	    	writer.close();
	    	
		} catch (IOException e) {
			 
			System.out.println("Could not write data to csv file.");
			e.printStackTrace();
		}    	    	
	}
	
	public void saveData(String filename, Map<String, Integer> records, Boolean append){
    	
    	CSVWriter writer;
		try {
			
			writer = new CSVWriter(new FileWriter(filename, append), ',', CSVWriter.DEFAULT_QUOTE_CHARACTER);
			for(String line : records.keySet()){
				writer.writeNext(new String[]{line});
			}
			
	    	writer.close();
	    	
		} catch (IOException e) {
			 
			System.out.println("Could not write data to csv file.");
			e.printStackTrace();
		}
    	 
	}
	
	
public void saveData(String filename, ArrayList<String[]> records, Boolean append){
    	
    	CSVWriter writer;
		try {
			
			writer = new CSVWriter(new FileWriter(filename, append), ',', CSVWriter.DEFAULT_QUOTE_CHARACTER);
			for(String[] line : records){
				writer.writeNext(line);
			}
			
	    	writer.close();
	    	
		} catch (IOException e) {
			 
			System.out.println("Could not write data to csv file.");
			e.printStackTrace();
		}
    	 
	}
	
	public void processMedicationAudltsList(String fIn, String fOut){
		ArrayList<String> list;
		
		Map<String, Integer> result = new HashMap<String, Integer>();
		
		
		
		Pattern p = Pattern.compile("\\[([a-zA-Z0-9\\_\\.\\?\\-\\:\\,\\s\\%])*(\\]?)");
		
		list = readMedList(fIn);
		
		Matcher m;
		String[] parts;
		String matched;
		for(String s : list){
			m = p.matcher(s);
				
			if (m.find()){
				
				matched =  m.group(0).substring(1).trim();
				if (matched.endsWith("]")){
					matched = matched.substring(0, matched.length() -1);
				}
				 
				System.out.println(m.group(0) + " ==> " + matched);
				parts  = matched.split(",");
				for(String sub : parts){
					if (sub.trim().length() > 1 && !sub.trim().matches("\\d+")){						 
						if (!result.containsKey(sub)){
							result.put(sub.trim(), 1);
						}else{
							result.put(sub.trim(), result.get(sub.trim()));
						}
					}
					
				}
			}else{
				if (s.trim().length() > 1){
					if (!result.containsKey(s.trim())){
						result.put(s.trim(), 1);
					}else{
						result.put(s.trim(), result.get(s.trim()));
					}
				}
								
			}
		}
		
		saveData(fOut,new String[]{"substance"},false);
		
		saveData(fOut,result,true);
		
	}
	
	public ArrayList<String> readMedList(String fIn){
		ArrayList<String> list = new ArrayList<String>();
		
		CSVReader reader = null;
        try{
            //Get the CSVReader instance with specifying the delimiter to be used
            reader = new CSVReader(new FileReader(fIn),',');
            String [] nextLine;
            // skip header
            reader.readNext();
            //Read one line at a time
            while ((nextLine = reader.readNext()) != null) 
            {
            	 list.add(nextLine[0]);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
		
		return list;
	}
}
