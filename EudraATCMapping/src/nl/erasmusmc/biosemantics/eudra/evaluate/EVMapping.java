package nl.erasmusmc.biosemantics.eudra.evaluate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.PosixParser;

import nl.erasmusmc.biosemantics.eudra.Utils.ConceptUtils;
import nl.erasmusmc.biosemantics.eudra.Utils.Utils;
import nl.erasmusmc.biosemantics.eudra.drugs.DrugCandidate;
import nl.erasmusmc.biosemantics.eudra.drugs.DrugServer;
import nl.erasmusmc.biosemantics.eudra.drugs.EudraCorpus;
import nl.erasmusmc.biosemantics.eudra.drugs.EudraRecord;

public class EVMapping {
	
	private String solrServer;
//	private String dbServer ;
//	private String database;
//	private String user;
//	private String password ;
    private String outDir;
    private String solrCollection;
    private static final int  ATC_5TH_LEVEL = 7; // default the 5th level
    private static final int  ATC_4TH_LEVEL = 5; // default the 5th level
    private static final int  ATC_3RD_LEVEL = 4; // default the 5th level
    private static final int  ATC_2ND_LEVEL = 3; // default the 5th level
    
    private boolean filterSalts;
    
    private List<DrugCandidate> drugCandidates;
    private EudraCorpus corpus;
    private EudraEvaluateResult evaluateResult;
	
    /**
     * @param solrServer Solr Server
     * @param solrCollection	Drug collection
     * @param dbServer	Database server
     * @param db		Database
     * @param user		Database Username
     * @param password	DB Passowrd
     * @param outDir	Output director
     */
    
    public EVMapping(String solrServer, String solrCollection
    		//, String dbServer, String database,String user, String password
    		, String outDir
    		){
    	this.solrServer = solrServer;
    	this.solrCollection = solrCollection;
    	
    	this.outDir = outDir;
    	
    	drugCandidates = new ArrayList<DrugCandidate>();
    	corpus = new EudraCorpus();
    	evaluateResult = new EudraEvaluateResult();
    	
    }
    
    public void filterATCLevel(List<DrugCandidate> drugCandidates , int level){
    	
    	List<DrugCandidate> delDrugs = new ArrayList<DrugCandidate>();
        for(DrugCandidate d: drugCandidates){
        	if (d.getAtc().length() < level){
        		delDrugs.add(d);
        	}
        }
        
        drugCandidates.removeAll(delDrugs);
    }
    
    public void filterExcipientConcepts(List<DrugCandidate> drugCandidates){
    	
    	List<DrugCandidate> tmp = new ArrayList<DrugCandidate>();
    	List<String> excipients = Utils.loadExcipientsList();
    	
    	for(DrugCandidate d: drugCandidates){
    		
    		for(String excipient: excipients){
    		
    			if (d.getDrugName().toString().toLowerCase().equals(excipient.trim().toLowerCase())){
        			tmp.add(d);
        		}
    			
    		}
    		
    	}
    	
    	// remove all excipients 
    	drugCandidates.removeAll(tmp);
    	
    }
    
    public List<DrugCandidate> search(String drug, int atcLevel, boolean filterExcipients){
    	 drugCandidates = new ArrayList<DrugCandidate>();    	 
         // look for substances only
         
         drugCandidates = getTaggerConcepts(drug, false, null);
         // remove all drugs which has ATC does not match the level
         filterATCLevel(drugCandidates, ATC_5TH_LEVEL);
         //dump(drugCandidates);
         /*// if no single substance or combinations found, look for ingredients
         if (drugCandidates.isEmpty()) {
             // look for ingredients
             drugCandidates = getTaggerConcepts(drug, false, null);
          // remove all drugs which has ATC does not match the level
             filterATCLevel(drugCandidates, ATC_5TH_LEVEL);
         }*/
         
         if (filterExcipients) {            
         	
        	 if (drugCandidates.isEmpty()){
        		 
        		 // remove excipients search again	         	 
            	 drug = Utils.filterExcipients(drug);	
            	 
                 drugCandidates = getTaggerConcepts(drug, false, null);
                 
                 //dump(drugCandidates);
                 // remove all drugs which has ATC does not match the level
                 filterATCLevel(drugCandidates, ATC_5TH_LEVEL);
        	 }
        	 
        	 filterExcipientConcepts(drugCandidates);
        	
         }
         
        
    	 
    	 return drugCandidates;
    }
    
   
    public void EudraMap(String eudraCorpusPath, boolean filterSalts) {
    	this.filterSalts = filterSalts;
        drugCandidates = new ArrayList<DrugCandidate>();

        corpus = new EudraCorpus(eudraCorpusPath, ATC_5TH_LEVEL);

        evaluateResult = new EudraEvaluateResult(eudraCorpusPath);
        
        //String drug;
        List<DrugCandidate> results;
        
        for (EudraRecord record : corpus.getRecords()) {
 
        	
            // DEBUG purpose only
            if (record.getSubstance().toString().startsWith("#")){
                continue;
            }
            
            results = new ArrayList<>();
            
            drugCandidates = search(record.getSubstance(), this.ATC_5TH_LEVEL, filterSalts);
            
            // check rule1: If found combination code, reject all others
            checkRule1(drugCandidates, results);
            
            // if no combination code found, check if there is a combination drug or single drug
            if (results.isEmpty() && drugCandidates.size() > 0) {
             
           
                List<DrugCandidate> newCandidates;
                // if more than one CUIs found
                 
                switch ( countConcept(drugCandidates)) {
                
                    case 1:
                        List<String> lsCheck = new ArrayList<>();
                        List<DrugCandidate> lsFound = new ArrayList<>();
                      
                        //check combination drug by looking for if there are two or more ingredients refer to the same CUI
                        for (int i = 0; i < drugCandidates.size(); i++) {
                            for (int j = i + 1; j < drugCandidates.size(); j++) {

                                // if two drugs have the same CUI but the ING_CUI and ATC are different
                            	// Drug: Amitriptyline + Perphenazine(C0936105); Ingredient: Amitriptyline(C0002600); ATC: N06AA09; 	Rela: ingredient_of;	STT: MIN;	SAB: RXNORM
                            	// Drug: Amitriptyline / Perphenazine(C0936105); Ingredient: Perphenazine(C0031184);  ATC: N05AB03; 	Rela: ingredient_of;	STT: MIN;	SAB: RXNORM
                            	
                                if (drugCandidates.get(i).getCui().equalsIgnoreCase(drugCandidates.get(j).getCui())
                                		// if drugname contains + or - or /
                                		&& drugCandidates.get(i).getDrugName().matches("(.*)(\\s)[\\+|\\-|\\/](\\s)(.*)")
                                        && !drugCandidates.get(i).getIngCui().equalsIgnoreCase(drugCandidates.get(j).getIngCui())
                                        && !drugCandidates.get(i).getAtc().equalsIgnoreCase(drugCandidates.get(j).getAtc())) {
                                    if (!lsCheck.contains(drugCandidates.get(i).getCui() + drugCandidates.get(i).getIngCui() + drugCandidates.get(i).getAtc())) {
                                        lsCheck.add(drugCandidates.get(i).getCui() + drugCandidates.get(i).getIngCui() + drugCandidates.get(i).getAtc());
 
                                        DrugCandidate newD = drugCandidates.get(i);
                                        newD.setDrugName(newD.getIngredient());
                                        newD.setDrugNorm(ConceptUtils.normalizeDrugName(newD.getIngredient()));
                                        newD.setCui(newD.getIngCui());
                                        lsFound.add(newD);
                                        //newD.println();
                                        // 
                                        if (!lsCheck.contains(drugCandidates.get(j).getCui() + drugCandidates.get(j).getIngCui() + drugCandidates.get(j).getAtc())) {
                                            lsCheck.add(drugCandidates.get(j).getCui() + drugCandidates.get(j).getIngCui() + drugCandidates.get(j).getAtc());
                                            drugCandidates.get(j).println();
                                            newD = drugCandidates.get(j);
                                            newD.setDrugName(newD.getIngredient());
                                            newD.setDrugNorm(ConceptUtils.normalizeDrugName(newD.getIngredient()));
                                            newD.setCui(newD.getIngCui());
                                            lsFound.add(newD);
                                            //newD.println();
                                        }

                                    }

                                }
                            }
                        }// end for

                        //dump(lsFound);
                        
                        // select unique CUI
                        lsCheck = new ArrayList<>();
                        List<DrugCandidate> newPotentials = new ArrayList<>();
                        for (DrugCandidate d1 : lsFound) {
                            if (!lsCheck.contains(d1.getCui())) {
                                lsCheck.add(d1.getCui());
                                newPotentials.add(d1);
                               // System.out.println(d1.toString());
                            }

                        }
                        
                        
                        
                        newCandidates = new ArrayList<>();
                        if (newPotentials.size() >= 2) {
                            //newCandidates = new ArrayList<>();
                        	//dump(newPotentials);
                            processTwoCombinations(newPotentials, newCandidates);
                            if (!newCandidates.isEmpty()) {
                                //drugCandidates = new ArrayList<>(newCandidates);
                            	//dump(newCandidates);
                                results = new ArrayList<>(newCandidates);
                            }
                        }else{
                        	 // select only ATC from ATC source
                        	
                        	results = new ArrayList<>(selectUniqueCandidate(drugCandidates)); 	
                        }
                        
                       
                        

                        break;

                    case 2:

                        //System.out.println("processing " + record.getSubstance());
                        newCandidates = new ArrayList<>();
                        
                        List<DrugCandidate> potentials = new ArrayList<DrugCandidate>();
                        potentials = selectUniqueCandidate(drugCandidates);
                        
                        //dump(potentials);
                        
                        processTwoCombinations(potentials, newCandidates);
                        if (!newCandidates.isEmpty()) {
                            //drugCandidates = new ArrayList<>(newCandidates);
                            results = new ArrayList<>(newCandidates);
                        }
                        
                        break;
                   default:
                	   
                	   
                	   
                	   break;
                }
                
            }   

            EudraEvaluateEntry e = new EudraEvaluateEntry();
            e.setTerm(record.getSubstance());
            e.setAtcs(record.getAtcs());
            e.setDrugCandidates(results);
            e.evaluate();
            evaluateResult.addEudraEvaluate(e);

        }

        //evaluateResult.printAll();
        //evaluateResult.exportAllResults(outDir);
         
        //evaluateResult.exportResultForDelivery(outDir);

    }
    
    
    
    /**
     * 
     * Select unique drug and ATC, if there is only one concept then choose drugs from ATC source only
     * 
     * @param candidates
     * @return list of drugs
     */
    public List<DrugCandidate> selectUniqueCandidate(List<DrugCandidate> candidates) {
    	
        List<DrugCandidate> potentials = new ArrayList<DrugCandidate>();
        
        List<String> tmp = new ArrayList<>();
        
        
        for(DrugCandidate d: candidates){
        	if (!tmp.contains(d.getCui())){
        		tmp.add(d.getCui());
        	}
        }
        
        
        
        List<String> selectedCUIs = new ArrayList<>();
        List<String> selectedINGCUI_ATCs = new ArrayList<>();
        List<String> selectedCUI_ATCs = new ArrayList<>();
        
        for(DrugCandidate d: candidates){
        	
        	if (tmp.size() > 1){
        		// if CUI and ING_CUI & ATC is not selected 
            	if (!selectedCUIs.contains(d.getCui()) && !  selectedINGCUI_ATCs.contains(d.getIngCui()+d.getAtc())  ){
            		selectedCUIs.add(d.getCui());
            		selectedINGCUI_ATCs.add(d.getIngCui()+d.getAtc());
            		potentials.add(d);
            	}
            
        	}else{
        		
        		// if there only concept found, get all ATC from ATC source
        		if (d.getRela().isEmpty() && !selectedCUI_ATCs.contains(d.getCui()+d.getAtc())){
        			selectedCUI_ATCs.add(d.getCui() + d.getAtc());
        			potentials.add(d);
        		}
        	}
        }
        
        // if one concept found but there is no ATC code from ATC source, then select from other sources
        selectedCUI_ATCs = new ArrayList<>();
        if (potentials.isEmpty()){
        	 for(DrugCandidate d: candidates){
        		 if ( !selectedCUI_ATCs.contains(d.getCui()+d.getAtc())){
         			selectedCUI_ATCs.add(d.getCui() + d.getAtc());
         			potentials.add(d);
         		}
        	 }
        }
     
        
        return potentials;
    } 
    
    public int countConcept (List<DrugCandidate> candidates) {
    	 
        List<String> tmp = new ArrayList<>();
        
        
        for(DrugCandidate d: candidates){
        	if (!tmp.contains(d.getCui())){
        		tmp.add(d.getCui());
        	}
        }
        
         
        return tmp.size();
       
    }
    
    
    /**
     * get all ATCs mapped to a drug based on CUI
     * 
     * @param drug
     * @param cui
     * @return list of drugs
     */
    public List<DrugCandidate> getDrugList(String drug, String cui){
    	List<DrugCandidate> drugs,tmp;
    	tmp = search(drug, ATC_5TH_LEVEL, filterSalts);
    	List<String> selected = new ArrayList<>();

    	drugs = new ArrayList<>();
    	
    	 for (DrugCandidate d : tmp) {
             if (d.getCui().equalsIgnoreCase(cui) && !selected.contains(d.getCui() + d.getAtc())) {
                 if (d.getAtc().length() == ATC_5TH_LEVEL) {
                     drugs.add(d);
                 }
                 selected.add(d.getCui() + d.getAtc());
             }
         }
    	
    	return drugs;
    }
    

    public void processTwoCombinations(List<DrugCandidate> potentials, List<DrugCandidate> results) {
        //List<DrugCandidate> results = new ArrayList<>();
        List<DrugCandidate> firstList, secondList;

        DrugCandidate first, second;

        if (potentials.size() > 1){

            first = potentials.get(0);
            second = potentials.get(1);

            firstList = getDrugList(first.getDrugName(), first.getCui());
            secondList = getDrugList(second.getDrugName(), second.getCui());
            
            //dump(secondList);
            // if two ingredients belong to the same 4th level, look for combination code is classified using the 5th level
            // code 30 or 20, for example: 
            //	B01AC06 acetylsalicylic acid 
   		 	//* B01AC07 dipyridamole 
   		 	//* B01AC30 combinations(acetylsalicylic acid & dipyridamole )
            checkRuleW3(first, second, results);
           
            
            // check each ATC in the fist list vs each one in the second list
            checkRulesByLists(firstList,secondList,results); 
            
            // if there is no combination found, take all individual codes
            if (results.isEmpty()){
            	results.addAll(firstList);
            	results.addAll(secondList);
            }
            
           

        }
    }
    
    public void checkRulesByLists(List<DrugCandidate> firstList, List<DrugCandidate> secondList, List<DrugCandidate> results){
    	
    	DrugCandidate drug = firstList.get(0);
    	 
    	
    	// search by ingredient
    	List<DrugCandidate> combinations = getCombinationDrugs(drug.getIngredient());
    	List<DrugCandidate> tmpResults = new ArrayList<>();
    	
    	//dump(combinations);
    	// check ATC in the first list vs second list
    	for (DrugCandidate d1 : firstList) {
            for (DrugCandidate d2 : secondList) {
            	 checkRules(d1, d2, combinations, tmpResults);                 
            }
        }
    	
    	// if no combination found, try to find combination with any drugs
    	if (tmpResults.isEmpty()){
    		checkCombineWithAny(drug, combinations, tmpResults);
    	}
    	
    	if (!tmpResults.isEmpty()){
    		results.addAll(tmpResults);
    	}
    	
    	tmpResults = new ArrayList<>();
    	drug = secondList.get(0);
    	
    	// search by ingredient
    	combinations = getCombinationDrugs(drug.getIngredient());
    	 
    	//dump(combinations);
    	// second vs first
    	for (DrugCandidate d1 : firstList) {
            for (DrugCandidate d2 : secondList) {
            	 checkRules(d2, d1, combinations, tmpResults);                 
            }
        }
    	
    	 
    	// if no combination found, try to find combination with any drugs
    	if (tmpResults.isEmpty()){
    		checkCombineWithAny(drug, combinations, tmpResults);
    	}
    	
    	if (!tmpResults.isEmpty()){
    		results.addAll(tmpResults);
    	}
    	
    }
    
    
    public boolean checkCombination(List<DrugCandidate> drugCandidates){
   	 
    	for (DrugCandidate d : drugCandidates) {
            if (d.getRela().toString().equalsIgnoreCase("combinations")) {
               return true;
            }
        }
    	return false;
    }
    
    public void checkRule1(List<DrugCandidate> drugCandidates, List<DrugCandidate> results){    	
    	 
        // ignore all others, just select drug combination atc
        for (DrugCandidate d : drugCandidates) {
            if (d.getRela().toString().equalsIgnoreCase("combinations")) {
            	results.add(d);
            }
        }
 
        
    }
    
	public void checkRuleW3(DrugCandidate first, DrugCandidate second, List<DrugCandidate> results) {
		/*
		 * B01AC06 acetylsalicylic acid 
		 * B01AC07 dipyridamole 
		 * B01AC30 combinations(acetylsalicylic acid & dipyridamole )
		 * 
		 */

		List<DrugCandidate> firstList, secondList;
		
		firstList = getDrugList(first.getDrugName(), first.getCui());
        secondList = getDrugList(second.getDrugName(), second.getCui());
 

		List<DrugCandidate> combinationDrugCandidates = getTaggerConcepts("combinations", false, null);
		// dump(combinationDrugCandidates);
		for (DrugCandidate d1 : firstList) {
			for (DrugCandidate d2 : secondList) {

				// each drug combination check Rule 3
				if (d1.getAtc().substring(0, 5).equalsIgnoreCase(d2.getAtc().substring(0, 5))) {

					for (DrugCandidate newd : combinationDrugCandidates) {

						// if ATCs not equal XXXXX20 or XXXXX30 the ignore
						if ((newd.getAtc().equalsIgnoreCase(first.getAtc().substring(0, 5) + "20")
								|| newd.getAtc().equalsIgnoreCase(first.getAtc().substring(0, 5) + "30"))) {
							// modify drug name: combinations (drug name 1 & drug name 2)
							newd.setDrugName(newd.getDrugName() + "(" + d1.getDrugName() + " & " + d2.getDrugName() + ")" );
							results.add(newd);
						}
					}

				} 
			}
		}
    }

	
	public List<DrugCandidate> checkMapping(List<DrugCandidate> combinations, String parentATC, int level ){
		List<DrugCandidate> results = new ArrayList<>();
		
		if (parentATC.length() >= level){
			parentATC = parentATC.substring(0,level);
	         
	         List<DrugCandidate> candidates = getConcepts("code:\"" + parentATC + "\" && sab:\"ATC\"");
	         DrugCandidate parent;
	         
	         if (candidates.size() > 0){
	             parent = candidates.get(0);
	             for(DrugCandidate d: combinations){
	                 if (d.getDrugName().toLowerCase().matches("(.*?)\\b" + parent.getDrugName().toLowerCase()+ "\\b(.*?)")
	                         && !(
	                        		 (d.getDrugName().toLowerCase().matches("(.*?)\\b" + parent.getDrugName().toLowerCase() + "\\b(.*?)(other\\bdrug|combination|psycholeptic)(.*?)"))
	                        	 ||
	                        	     (d.getDrugName().toLowerCase().matches("(.*?)(other\\bdrug|combination|psycholeptic)(.*?)\\b" + parent.getDrugName().toLowerCase() + "\\b(.*?)"))
	                        	)
	                         
	                         //&& !d.getDrugName().toLowerCase().matches("(.*?)\\b(combinations|psycholeptics|psycholeptic)\\b(.*?)") 
	                         ){
	                	 if (!d.getTty().toString().contains("REVERSED")){
	                		 results.add(d);
	                	 }
	                     
	                     
	                 }
	             }
	         }
		}
		
	 
		return results;
		
	}
	
	
	/**
	 * 
	 * try to find matches between drugs and parent drug of the other
     * for example: S01CA05	betamethasone and antiinfectives ; S03CA06	betamethasone and antiinfectives
     * matched with	A01AB18	clotrimazole ==> A01AB is	"Antiinfectives and antiseptics for local oral treatment"
	 * 
	 * @param combinations
	 * @param parentATC
	 * @param level
	 * @return
	 */
	public List<DrugCandidate> checkMapping2(List<DrugCandidate> combinations, String parentATC, int level ){
		
		
		List<DrugCandidate> results = new ArrayList<>();
		
		if (parentATC.length() >= level){
			parentATC = parentATC.substring(0,level);
	         
	         List<DrugCandidate> candidates = getConcepts("code:\"" + parentATC + "\" && sab:\"ATC\"");
	         
	         if (candidates.size() > 0){	        	 
	        	 
	        		 // check each combination drugs, for example: betamethasone, antiinfinitives; betamethasone, antiseptics
	        		 for(DrugCandidate cDrug: combinations){
	        			 // split words by "and" or ","
	        			 String[] words = cDrug.getDrugName().toLowerCase().split("(\band\b|\\,)");	        			  
	        			 for(String word: words){
	        				 for(DrugCandidate pDrug: candidates){
	        					 // if contain any word then take this combination
	        					 if (pDrug.getDrugName().toLowerCase().matches("(.*?)\\b" + word.trim() + "\\b(.*?)") && !results.contains(cDrug) ){
	        						 results.add(cDrug);
	        						 break;
	        					 }
	        				 }
	        			 }
	        		 }
	        	 	             
	         }
		}
		
	 
		return results;
		
	}
	
	public void mapHigherLevel(DrugCandidate d1, DrugCandidate d2, List<DrugCandidate> combinations, List<DrugCandidate> results){
		System.out.println(String.format("Checking %s[%s] & %s[%s]", d1.getDrugName(), d1.getAtc(),d2.getDrugName(), d2.getAtc()));
		String parentATC;
		boolean foundCombination = false;
		
		 // try to map to the 4th level (5 characters) first
        parentATC = d2.getAtc().toString();
        
        List<DrugCandidate> tmp;
        
        // map to the 5th level
        tmp = checkMapping(combinations, parentATC, ATC_5TH_LEVEL);
        if (! tmp.isEmpty()){
        	results.addAll(tmp);
        }else{
        	
        	// map to the 4th level
        	tmp = checkMapping(combinations, parentATC, ATC_4TH_LEVEL);
        	
        	if (! tmp.isEmpty()){
            	results.addAll(tmp);
            }else{
            	
            	// map to the 3rd level
            	tmp = checkMapping(combinations, parentATC, ATC_3RD_LEVEL);            	
            	if (! tmp.isEmpty()){
                	results.addAll(tmp);
                }else{
                	
                	// map to 2nd level
                	tmp = checkMapping(combinations, parentATC, ATC_2ND_LEVEL);
                	
                	if (! tmp.isEmpty()){
                		results.addAll(tmp);
                	}else{               		
                		// check the second rule, map to the 5th level
                		tmp = checkMapping2(combinations, parentATC,ATC_5TH_LEVEL );
                		
                		if (!tmp.isEmpty()){
                			results.addAll(tmp);
                		}else{
                			
                			tmp = checkMapping2(combinations, parentATC,ATC_4TH_LEVEL );
                			if (!tmp.isEmpty()){
                    			results.addAll(tmp);
                    		}else{
                    			
                    			tmp = checkMapping2(combinations, parentATC,ATC_3RD_LEVEL );
                    			
                    			if (!tmp.isEmpty()){
                        			results.addAll(tmp);
                        		}else{
                        			tmp = checkMapping2(combinations, parentATC,ATC_2ND_LEVEL );
                        			
                        			if (!tmp.isEmpty()){
                            			results.addAll(tmp);
                            		}                        			
                        		}
                    		}
                		}
                	}
                	
                	
                	
                }
            }
        }
        
        
	}
	
	/**
     * 
     * check for combine with any drugs. For example: N02BE51 paracetamol, combinations  (paracetamol combines with caffeine or acetylsalicilic) 
     * 
     * @param drug
     * @param combinations
     * @param tmpResults
     */
    public void checkCombineWithAny(DrugCandidate drug, List<DrugCandidate> combinations, List<DrugCandidate> results){
    	
    	results = new ArrayList<>();
    	// 
    	for (DrugCandidate d : combinations) {
            
            if ( d.getDrugName().matches("\\b" + drug.getDrugName().toString().toLowerCase() + "\\b\\,?\\s?combinations") 
            		&& d.getDrugName().matches("\\b" + drug.getIngredient().toString().toLowerCase() + "\\b\\,?\\s?combinations") ) {
            	results.add(d);
                
            }
            
        }
    }
	
	
    public void checkRules( DrugCandidate d1, DrugCandidate d2,List<DrugCandidate> combinations , List<DrugCandidate> results){

        
        boolean foundCombination = false;
       
        List<DrugCandidate> tmpResults = new ArrayList<>();
        
       // List<DrugCandidate> combinations = getCombinationDrugs(d1.getDrugName());
        //dump(combinations);

        if (combinations.size() > 0) {

            // if no combination found, try to map HIGHER LEVEL
            // Drug 1 map to a higher level of drug 2
           
        	mapHigherLevel(d1, d2, combinations, tmpResults);
        	
        	if (!tmpResults.isEmpty()){
        		foundCombination = true;
        		results.addAll(tmpResults);
        	}
        	
        	
            // check rule2, combinations with  psycholeptics
            if (d2.getAtc().startsWith("N05") || d2.getAtc().startsWith("N06")) {
                // select all 70-series
                for (DrugCandidate d : combinations) {
                    if (d.getAtc().length() == ATC_5TH_LEVEL) {
                        if (d.getAtc().substring(5, 6).equals("7")) {
                            results.add(d);
                            foundCombination = true;

                        }
                    }
                }
            }
            
           
        }

    }
    
    

    public List<DrugCandidate> getCombinationDrugs(String substance){
    	
    	
    	if (this.filterSalts){
    		substance = Utils.filterExcipients(substance);
    	}
    	
        List<DrugCandidate> tmp = getConcepts(substance,null);
        
        List<DrugCandidate> combinations  = new ArrayList<>();
        for(DrugCandidate d: tmp){
            if (d.getRela().equalsIgnoreCase("combinations")){
                combinations.add(d);
            }
        }
       
        return combinations;
    }

 
    public void exportForDelivery(String outDir){
    	evaluateResult.exportResultForDelivery(outDir);
    }
    
    public void exportForDelivery(){
    	evaluateResult.exportResultForDelivery(this.outDir);
    }
    
    public void printResults(){
    	evaluateResult.printAll(this.outDir);
    }
    
    public void printResults(String dir){
    	evaluateResult.printAll(dir);
    }
    
    public void exportResult(String outDir){    	 
    	exportResult(outDir);
    }
    
    public void exportResult(){
    	evaluateResult.exportResult(this.outDir);
    }
	
    public void EudraMapWithoutRules(String eudraCorpusPath) {

        List<DrugCandidate> drugCandidates = new ArrayList<DrugCandidate>();

        EudraCorpus corpus = new EudraCorpus(eudraCorpusPath);

        EudraEvaluateResult evaluateResult = new EudraEvaluateResult(eudraCorpusPath);
        String substance;

        for (EudraRecord record : corpus.getRecords()) {

            // skip all vaccines
            ArrayList<String> newAtcs = new ArrayList<>();
            int index = 0;
            for (int i = 0; i < record.getAtcs().length; i++) {
                if (!record.getAtcs()[i].startsWith("J07")) {
                    newAtcs.add(record.getAtcs()[i]);
                }
            }

            if (newAtcs.isEmpty() && record.getAtcs().length > 0) {
                //  continue;
            }


            // DEBUG purpose only
            if (record.getSubstance().toString().startsWith("#")){
                continue;
            }

            // look for substances only
            //substance = ConceptUtils.normalizeConcept(record.getSubstance());
            substance = record.getSubstance();
            drugCandidates = getTaggerConcepts(substance, true, null);


            // if no single substance or combinations found, look for ingredients
            if (drugCandidates.isEmpty()) {
                // look for ingredients
                drugCandidates = getTaggerConcepts(substance, false, null);
            }

            EudraEvaluateEntry e = new EudraEvaluateEntry();
            e.setTerm(record.getSubstance());
            e.setAtcs(record.getAtcs());
            e.setDrugCandidates(drugCandidates);
            e.evaluate();
            evaluateResult.addEudraEvaluate(e);

        }

        evaluateResult.printAll();
        evaluateResult.exportAllResults(outDir);
        evaluateResult.exportResult(outDir);

    }
    
	public void baselineMapping(String eudraCorpusPath, String outDir){

        List<DrugCandidate> drugCandidates = new ArrayList<DrugCandidate>();

        EudraCorpus corpus = new EudraCorpus(eudraCorpusPath);

        EudraEvaluateResult evaluateResult = new EudraEvaluateResult(eudraCorpusPath);
        String substance;

        for (EudraRecord record : corpus.getRecords()) {


            // DEBUG purpose only
            if (record.getSubstance().toString().startsWith("#")){
                continue;
            }

            // look for substances only
            //substance = ConceptUtils.normalizeConcept(record.getSubstance());
            substance = record.getSubstance();
            drugCandidates = getTaggerConcepts(substance, true, null);


            EudraEvaluateEntry e = new EudraEvaluateEntry();
            e.setTerm(record.getSubstance());
            e.setAtcs(record.getAtcs());
            e.setDrugCandidates(drugCandidates);
            e.evaluate();
            evaluateResult.addEudraEvaluate(e);

        }

        evaluateResult.printAll();
        evaluateResult.exportAllResults(outDir);
        evaluateResult.exportResult(outDir);

    }
	
	public List<DrugCandidate> getConcepts(String term, String searchField){
		
        DrugServer drugServer = new DrugServer(solrServer, solrCollection);

        return drugServer.getConcepts(term, searchField);
    }

    public List<DrugCandidate> getConcepts(String term){
        DrugServer drugServer = new DrugServer(solrServer, solrCollection);
        return drugServer.getConcepts(term);
    }

    public List<DrugCandidate> getTaggerConcepts(String term, boolean substanceOnly, String mode){

        DrugServer drugServer = new DrugServer(solrServer, solrCollection);

        return drugServer.getTaggerConcepts(term, substanceOnly, mode);

    }
    
    
    public void dump(List<DrugCandidate> drugCandidates){
    	System.out.println("CUI\tDrug\tING_CUI\tIngredient\tATC\tRela\tTTY\tSAB");
    	for(DrugCandidate d: drugCandidates){
    		System.out.println(d.getString());
    	}
    }

}
