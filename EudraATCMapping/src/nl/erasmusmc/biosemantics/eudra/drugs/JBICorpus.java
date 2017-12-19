package nl.erasmusmc.biosemantics.eudra.drugs;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVReader;

public class JBICorpus {

	private String path;
	private List<JBIRecord> records;
	
	
	public JBICorpus(){
		this.path = null;
		records = new ArrayList<JBIRecord>();
	}
	
	public JBICorpus(String path){
		
		this.path = path;
		records = new ArrayList<JBIRecord>();
		
		loadCorpus();
		
	}
	
	
	public void loadCorpus(){
		
		 CSVReader reader = null;
	        try{
	            //Get the CSVReader instance with specifying the delimiter to be used
	            reader = new CSVReader(new FileReader(this.path),',');
	            String [] nextLine;
	            // skip header
	            reader.readNext();
	            //Read one line at a time
	            while ((nextLine = reader.readNext()) != null) 
	            {
	            	JBIRecord record;
	            	if (nextLine.length >=4){
	            		record = new JBIRecord(nextLine[3],nextLine[0], nextLine[2], nextLine[1]);
	            		records.add(record);
	            	}
	            	     		
	            	
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
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public List<JBIRecord> getRecords() {
		return records;
	}

	public void setRecords(List<JBIRecord> records) {
		this.records = records;
	}
	
	
	
}
