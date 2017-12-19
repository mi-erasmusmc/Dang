package nl.erasmusmc.biosemantics.eudra.Utils;

import com.opencsv.CSVReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

/**
 * Created by haidangvo on 8/2/16.
 */
public class Utils {

	 
	private static String excipientsPath	= "data/excipients.csv";
	
/*    public static List<String> loadSuppressionList(String fname) {

        List<String> result = new ArrayList<>();
        CSVReader reader = null;
        try{
            //Get the CSVReader instance with specifying the delimiter to be used
            reader = new CSVReader(new FileReader(fname),',');
            String [] nextLine;
            // skip header
            reader.readNext();
            //Read one line at a time
            while ((nextLine = reader.readNext()) != null)
            {
                result.add(nextLine[0]);

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
        return  result;

    }*/

 /*   public static Map<Integer, List> loadStabilizer(String fname){
        Map<Integer, List> result =  new HashMap<Integer, List>();
        CSVReader reader = null;

        try{
            // maximum 50 words
            for(int i = 0; i < 50; i++){
                result.put(i, new ArrayList<String>());
            }

            //Get the CSVReader instance with specifying the delimiter to be used
            reader = new CSVReader(new FileReader(fname),',');
            String [] nextLine;
            // skip header
            reader.readNext();
            //Read one line at a time

            while ((nextLine = reader.readNext()) != null)
            {

                result.get(Integer.parseInt(nextLine[1])-1).add(nextLine[0]);

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

        return result;

    }
    */
    
	
	public static String filterExcipients(String drug){
		 
		String newDrug = "";
		
		//String drugCleaned = ConceptUtils.cleanDrugName(drug);		
		List<String> excipients = loadExcipientsList(excipientsPath);
		 
		newDrug = drug;
		boolean found = false;
		for(int i = 0; i <excipients.size(); i++){
			
			if (! newDrug.equals( newDrug.replaceAll( "(?i)\\b" + excipients.get(i) + "\\b" , "")) ){
				newDrug = newDrug.replaceAll("(?i)\\b" + excipients.get(i) + "\\b", "");		
				found = true;
				// restart searching
				i = 0;
			}
		}
		
		
		if ( found ){
			newDrug =  ConceptUtils.cleanDrugName(newDrug);
			newDrug = ConceptUtils.normalizeConcept(newDrug);
			newDrug = newDrug.trim();
			//newDrug = newDrug.trim().replaceAll("\u00A0", "");
			
			if (newDrug.length() < 3){
				newDrug = "";
			}
			
			System.out.println(String.format("Filter excipients:  %s ==> %s", drug,newDrug));
			return newDrug;
		}else{
			return drug;
		}		
		 
	}

	public static List<String> loadExcipientsList(){
		
		return loadExcipientsList(excipientsPath);
	}

	private static List<String> loadExcipientsList(String inFilename) {

		List<String>  result = new ArrayList<String>();
        CSVReader reader = null;
        try {
            //Get the CSVReader instance with specifying the delimiter to be used
            reader = new CSVReader(new FileReader(inFilename), ',');
            String[] nextLine;
            // skip header
            reader.readNext();
            //Read one line at a time
            while ((nextLine = reader.readNext()) != null) {
                 
                result.add(nextLine[0].trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return result;
    }
	
    private static void swap(ArrayList<String> ourarray, int right, int left) {
        String temp = ourarray.get(right);
        ourarray.set(right,ourarray.get(left));
        ourarray.set(left, temp);
    }

    public static void permute(ArrayList<String> ourArray, int currentPosition, ArrayList<String> output) {
        if (currentPosition == 1) {
           ArrayList<String> copy = new ArrayList<>(ourArray);
            output.add( StringUtils.join(copy, ", "));
        } else {
            for (int i = 0; i < currentPosition; i++) {
                // subtract one from the last position (here is where you are
                // selecting the the next last item
                permute(ourArray, currentPosition - 1, output);

                // if it's odd position
                if (currentPosition % 2 == 1) {
                    swap(ourArray, 0, currentPosition - 1);
                } else {
                    swap(ourArray, i, currentPosition - 1);
                }
            }
        }
    }

}
