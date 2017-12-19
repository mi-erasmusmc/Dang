package nl.erasmusmc.biosemantics.eudra.drugs;

import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import nl.erasmusmc.biosemantics.eudra.Utils.ConceptUtils;
import nl.erasmusmc.biosemantics.eudra.Utils.Utils;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;

import com.opencsv.CSVReader;

import nl.erasmusmc.biosemantics.eudra.evaluate.AnnotatorEntry;
import nl.erasmusmc.biosemantics.eudra.evaluate.AnnotatorResult;
import nl.erasmusmc.biosemantics.eudra.evaluate.EVMapping;
import nl.erasmusmc.biosemantics.eudra.evaluate.EudraEvaluateEntry;
import nl.erasmusmc.biosemantics.eudra.evaluate.EudraEvaluateResult;
import nl.erasmusmc.biosemantics.eudra.evaluate.JBIEvaluateEntry;
import nl.erasmusmc.biosemantics.eudra.evaluate.JBIEvaluateResult;
import nl.erasmusmc.biosemantics.eudra.evaluate.Measurement;
import nl.erasmusmc.biosemantics.eudra.evaluate.ObsrvCalculator;
import nl.erasmusmc.biosemantics.eudra.solr.index.AddDrugs;

public class EudraTest {
    public static final int LEVEL2 = 3;
    public static final int LEVEL3 = 4;
    public static final int LEVEL4 = 5;
    public static final int LEVEL5 = 7;
    private static String outDir;
    
    private static String drugDict = "drugs";
    //private static String drugDict2 = "drugs2";
    private static String requestHandler = "/drug_tag";
    
    //private static String annotator1 = "data/FEAR_reference_14JUL2016_Esme.csv";
    private static String annotator1 = "data/EV_Peds_Alexandra_05SEP2016_updated14102016.csv";
    //private static String annotator1 = "data/EV_Adults_Christel_02SEP2016.csv";
    
    //private static String annotator2 = "data/FEAR_reference_14JUL2016_Veronique.csv";
    private static String annotator2 = "data/EV_Peds_Carmen_06SEP2016.csv";
    //private static String annotator2 = "data/EV_Adults_Esme_02SEP2016.csv";

    //private static String arbitratedset = "data/FAERS_GoldStandard_14JUL2016_arbitrated.csv";
    private static String arbitratedset = "data/EV_Peds_Osemeke_Arbitrated_26SEP2016.csv";
    //private static String arbitratedset = "data/EV_Adults_Osemeke_Arbitrated_26SEP2016.csv";
 
    //private static String eudraCorpusPath = "data/EV_Adults_Arbitration_Alexandra_2016NOV02_training.csv";
    //private static String eudraCorpusPath = "data/EV_Adults_Arbitration_Alexandra_2016NOV02_test.csv";
    
    //private static String eudraCorpusPath = "data/EV_Peds_Arbitration_Esme_2016NOV02_training.csv";
    private static String eudraCorpusPath = "data/EV_Peds_Arbitration_Esme_2016NOV02_test.csv";
    
    
    private static String jbiCorpusPath = "data/JBI_drugs_manual_annotated_original.csv";
 
    private String solrServer = "http://localhost:8995/solr";
   

    public EudraTest(String solrServer) {
        this.solrServer = solrServer;
        
    }

    public static void main(String[] args) throws ParseException, SQLException, SolrServerException, IOException {

        Options options = new Options().addOption("solr", true, "drug server")
                 .addOption("dir", true, "output dir");
        String solrServer = new PosixParser().parse(options, args).getOptionValue("solr");
        outDir = new PosixParser().parse(options, args).getOptionValue("dir");
        if (solrServer == null) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(AddDrugs.class.getCanonicalName(), options);
        } else {

            EudraTest serverTest = new EudraTest(solrServer);
            //serverTest.EVMapWithoutFilterSalts();
            serverTest.EVMapFilterSalts();
            
            System.out.println("Done!");


        }

    }
    
    
public void EVMapFilterSalts(){
    	
    	EVMapping evMap = new EVMapping(solrServer, drugDict, outDir);
    	
    	evMap.EudraMap(eudraCorpusPath, true);
    	
    	evMap.printResults();
    	evMap.exportResult();
    }
    
    public void EVMapWithoutFilterSalts(){
    	
    	EVMapping evMap = new EVMapping(solrServer, drugDict, outDir);
    	
    	evMap.EudraMap(eudraCorpusPath, false);
    	
    	evMap.printResults();
    	evMap.exportResult();
    	
    }
  
     


    private ArrayList<EudraRecord> ReadEudravigilanceData(String inFilename) {

        ArrayList<EudraRecord> result = new ArrayList<EudraRecord>();
        CSVReader reader = null;
        try {
            //Get the CSVReader instance with specifying the delimiter to be used
            reader = new CSVReader(new FileReader(inFilename), ',');
            String[] nextLine;
            // skip header
            reader.readNext();
            //Read one line at a time
            while ((nextLine = reader.readNext()) != null) {
                EudraRecord record;
                if (nextLine.length >= 2) {
                    record = new EudraRecord(nextLine[0], nextLine[1].split(","));
                } else {
                    record = new EudraRecord(nextLine[0], null);
                }

                result.add(record);
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


    
}
