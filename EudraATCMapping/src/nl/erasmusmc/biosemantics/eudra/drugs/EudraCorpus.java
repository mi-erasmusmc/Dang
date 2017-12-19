/**
 * 
 */
package nl.erasmusmc.biosemantics.eudra.drugs;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVReader;

/**
 * @author dangvh
 *
 */
public class EudraCorpus {
	private List<EudraRecord> records;
	private String path;	
	private int atcLevel;

	public EudraCorpus(){
		this.atcLevel = -1;
		records = new ArrayList<EudraRecord>();

	}
	
	public EudraCorpus(String path){
		this.path = path;
		this.atcLevel = -1;
		records = new ArrayList<EudraRecord>();
		this.loadCorpus();
		
	}

	public EudraCorpus(String path, int level){
		this.path = path;
		this.atcLevel = level;
		records = new ArrayList<EudraRecord>();
		this.loadCorpus();

	}
	
	public List<EudraRecord> getRecords() {
		return records;
	}

	public void setRecords(List<EudraRecord> records) {
		this.records = records;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	private void loadCorpus() {
		
        CSVReader reader = null;
        try{
            //Get the CSVReader instance with specifying the delimiter to be used
            reader = new CSVReader(new FileReader(this.path),',');
            String [] nextLine;
			String atc;
            // skip header
            reader.readNext();
            //Read one line at a time
            while ((nextLine = reader.readNext()) != null) 
            {
            	EudraRecord record;
            	ArrayList<String> ls = new ArrayList<String>();

            	if (nextLine.length >=2){
            		
            		// skip comment column
            		for(int i=1; i<nextLine.length - 1; i++){
            			if (nextLine[i].length() > 0){
            				atc = nextLine[i].trim();
            				atc = nextLine[i].replaceAll("\\s", "");
            				atc = atc.trim();

            				if (!atc.isEmpty()){
            					
            					// add all atcs if level is not specified
    							if (atcLevel == -1){
    								ls.add(atc);
    							}else if (atc.length() >= atcLevel){
    								ls.add(atc);
    							}else{
    								ls.add(""); // set ATC to null if it does not match the level
    							}
            				}

							
            			}

            		}
            		
            		record = new EudraRecord(nextLine[0], ls.toArray(new String[ls.size()]));
            	}else{
            		record = new EudraRecord(nextLine[0], null);
            	}
            	     		
            	records.add(record);
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
}
