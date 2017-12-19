package nl.erasmusmc.DUTIEx;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.erasmusmc.data_mining.ontology.api.Concept;
import org.erasmusmc.data_mining.ontology.api.Label;
import org.erasmusmc.data_mining.ontology.api.Language;
import org.erasmusmc.data_mining.ontology.api.Ontology;
import org.erasmusmc.data_mining.ontology.api.SemanticType;
import org.erasmusmc.data_mining.ontology.common.DatabaseId;
import org.erasmusmc.data_mining.ontology.common.LabelTypeComparator;
import org.erasmusmc.data_mining.ontology.impl.file.SingleFileOntologyImpl;
import org.erasmusmc.data_mining.peregrine.api.IndexingResult;
import org.erasmusmc.data_mining.peregrine.api.Peregrine;
import org.erasmusmc.data_mining.peregrine.disambiguator.api.DisambiguationDecisionMaker;
import org.erasmusmc.data_mining.peregrine.disambiguator.api.Disambiguator;
import org.erasmusmc.data_mining.peregrine.disambiguator.api.RuleDisambiguator;
import org.erasmusmc.data_mining.peregrine.disambiguator.impl.ThresholdDisambiguationDecisionMakerImpl;
import org.erasmusmc.data_mining.peregrine.disambiguator.impl.rule_based.LooseDisambiguator;
import org.erasmusmc.data_mining.peregrine.disambiguator.impl.rule_based.StrictDisambiguator;
import org.erasmusmc.data_mining.peregrine.disambiguator.impl.rule_based.TypeDisambiguatorImpl;
import org.erasmusmc.data_mining.peregrine.impl.hash.PeregrineImpl;
import org.erasmusmc.data_mining.peregrine.normalizer.api.Normalizer;
import org.erasmusmc.data_mining.peregrine.normalizer.api.NormalizerFactory;
import org.erasmusmc.data_mining.peregrine.normalizer.impl.NormalizerFactoryImpl;
import org.erasmusmc.data_mining.peregrine.normalizer.impl.SnowballNormalizer;
import org.erasmusmc.data_mining.peregrine.tokenizer.api.TokenizerFactory;
import org.erasmusmc.data_mining.peregrine.tokenizer.impl.TokenizerFactoryImpl;
import org.erasmusmc.data_mining.peregrine.tokenizer.impl.UMLSGeneChemTokenizer;
import org.tartarus.snowball.ext.dutchStemmer;
import org.tartarus.snowball.ext.englishStemmer;


public class PeregrineIndexer {
	private Peregrine peregrine = null;
    private Ontology ontology;
    private String ontologyFile;   
    private Language lang;
    
    public Map<String, Counters> conceptCount = new HashMap<String, Counters>(500000);
	public ArrayList<ResultSet> results;
	
    
    final static Logger logger = Logger.getLogger(PeregrineIndexer.class);
    
    public  PeregrineIndexer(Language lang, String ontologyFile){
    	this.lang = lang;
    	this.ontologyFile = ontologyFile;
    	results = new ArrayList<ResultSet>();
    	 
    }
    
    public void initPeregrine(String ontologyfile) throws IOException {
        ontology = new SingleFileOntologyImpl(ontologyfile);
        TokenizerFactory tokenizerFactory = TokenizerFactoryImpl.createDefaultTokenizerFactory(new UMLSGeneChemTokenizer());

        // For English, uncomment this line
        //NormalizerFactory normalizerFactory = NormalizerFactoryImpl.createDefaultNormalizerFactory(new LVGNormalizer("/Volumes/Data/ErasmusMC/lvg2013lite/data/config/lvg.properties"));

        /**
         * Special code for Dutch stemmer
         */
        Map<Language, Normalizer> stemmerMap = new HashMap<Language, Normalizer>();
        stemmerMap.put(Language.NL, new SnowballNormalizer(new dutchStemmer()));
        stemmerMap.put(Language.EN, new SnowballNormalizer(new englishStemmer()));
        NormalizerFactory normalizerFactory = new NormalizerFactoryImpl(stemmerMap);

        // ------ end  here -------------->

        Disambiguator disambiguator = new TypeDisambiguatorImpl(new RuleDisambiguator[]{new StrictDisambiguator(), new LooseDisambiguator()});
        DisambiguationDecisionMaker disambiguationDecisionMaker = new ThresholdDisambiguationDecisionMakerImpl();

        // This parameter is used to define the set of languages in which the ontology should be loaded. Language code used is ISO639.		
        // String ontologyLanguageToLoad = "en, nl, de";

        // For now, this feature is only available for DBOntology. Thus, we can leave it as null or empty string in this sample code. 
        String ontologyLanguageToLoad = "";
        peregrine = new PeregrineImpl(ontology, tokenizerFactory, normalizerFactory, disambiguator, disambiguationDecisionMaker, ontologyLanguageToLoad);
        System.out.println("---> Loading ontology ... done! ");
    }
    
    public void start(){
    	try {
			initPeregrine(ontologyFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // init Peregrine
   }
    
    public List<IndexingResult> getIndex(String text) {
        if (peregrine == null) {
            System.out.println("---Peregrine is not initialized ..");
            return null;
        }
        return peregrine.indexAndDisambiguate(text, lang);
        //return peregrine.index(text, lang);

    }
    
    public ResultSet indexConcept(String s){
    	List<IndexingResult> indexingResults = getIndex(s);
    	String found_text;
    	ResultSet rs = new ResultSet();
    	IndexedConcept c;
    	String CUI;
    	for (final IndexingResult indexingResult : indexingResults) {
    		
    		
    		found_text = s.substring(indexingResult.getStartPos(), indexingResult.getEndPos() + 1);
    		
    		final Serializable conceptId = indexingResult.getTermId().getConceptId();
    		final Concept concept = ontology.getConcept(conceptId);
			final String preferredLabelText = LabelTypeComparator.getPreferredLabel(concept.getLabels()).getText();
			
			CUI = fillCUI(conceptId.toString());
			
			if (rs.existsCUI(CUI)){
				rs.addFoundText(CUI, found_text);
			}else{
				c = new IndexedConcept();						
				c.setCui(CUI);
				c.addFound_text(found_text);
				c.setPreferred_term(preferredLabelText);				
				rs.addConcept(c);
			}
			
			 
			
			
			//System.out.println("CUI: " + fillCUI(conceptId.toString()));
			//System.out.println("\tFound text: " + found_text + "\t");
			//System.out.println("\tPreferred concept label is: \"" + preferredLabelText + "\".\t" );
			
			logger.info("CUI: " + fillCUI(conceptId.toString()));
			logger.info("\tFound text: " + found_text + "\t");
			logger.info("\tPreferred concept label is: \"" + preferredLabelText + "\".\t" );
			
			if (!conceptCount.containsKey(found_text)) {
                Counters counter = new Counters();
                counter.cID = fillCUI(conceptId.toString());
                conceptCount.put(found_text, counter);
            } else {
                Counters counter = conceptCount.get(found_text);
                counter.counter++;
            }
			
			Collection<DatabaseId> dbs =  concept.getDatabaseIds();
			
			for (DatabaseId id: dbs){				 				
					//System.out.print("\t" +id.getSource() + ": " + id.getCode() + "\t" );
					logger.info("\t" +id.getSource() + ": " + id.getCode() + "\t" );
				//System.out.println("DB " + id.getSource().length()+ ": " + id.getCode() );
			}
			System.out.println();
    	}
    	
    	return rs;
    	
    }
    
   /* public ResultSet indexingConcept(CorpusItem i) {
    	
    	ResultSet result = new ResultSet();
    	Concept item;
        try {
        	String s = i.getTxt();
        	
        	
        	result.setDoc_id(i.getDoc_id());
        	result.setSentence_id(i.getSentence_id());
        	result.setTxt(i.getTxt());
        	
            List<IndexingResult> indexingResults = getIndex(s);           
            //System.out.println(rs.size() +  " ==> " + s );
            
            String found_text;
            
            for (final IndexingResult indexingResult : indexingResults) {
    			final Serializable conceptId = indexingResult.getTermId().getConceptId();
    			item = new Concept();
    			//System.out.println();
    			String CUI;
    			CUI = fillCUI(conceptId.toString());
    			item.setCui(CUI);
    			
    			found_text = s.substring(indexingResult.getStartPos(), indexingResult.getEndPos() + 1);
    			item.setFound_text(found_text);
    			
    			//System.out.println("- Found concept with id: " + CUI + ", matched text: \"" + found_text + "\".");

    			final Concept concept = ontology.getConcept(conceptId);
    			final String preferredLabelText = LabelTypeComparator.getPreferredLabel(concept.getLabels()).getText();
    			//System.out.println("  Preferred concept label is: \"" + preferredLabelText + "\".");
    			
    			Collection<DatabaseId> dbs =  concept.getDatabaseIds();
    			
    			for (DatabaseId id: dbs){
    				if (id.getSource().toString().equals("ICD10")) {
    					item.addIcd10_code(id.getCode());
    					//System.out.println("DB " + id.getSource() + ": " + id.getCode() );
    				}
    				//System.out.println("DB " + id.getSource().length()+ ": " + id.getCode() );
    			}
    			
    			result.addConcept(item); 
    		}
            
           
          
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return result;
    }*/
   
    
    /**
     * Fill concept ID with 'zero' characters
     *
     * @param id: concept without format
     * @return: formatted concept ID
     */
    public String fillTypeID(String id) {
        int idx = 3 - id.length();
        return preFix[idx] + id;
    }
    static String preFix[] = {"T", "T0", "T00"};

    /**
     * Fill CUI with 'zero' character
     *
     * @param cui: unformatted cui
     * @return: formatted CUI
     */
    public String fillCUI(String cui) {
        int len = 7 - cui.length();
        StringBuilder CUI = new StringBuilder("C");
        for (int i = 0; i < len; i++) {
            CUI.append('0');
        }
        CUI.append(cui);
        return CUI.toString();
    }
	
}