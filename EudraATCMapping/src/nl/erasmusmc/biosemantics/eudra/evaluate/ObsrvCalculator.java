package nl.erasmusmc.biosemantics.eudra.evaluate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.erasmusmc.biosemantics.eudra.drugs.DrugCandidate;
import nl.erasmusmc.biosemantics.eudra.drugs.EudraCorpus;
import nl.erasmusmc.biosemantics.eudra.drugs.EudraRecord;

/**
 * @author dangvo
 * 
 * Calculate observed agreement 
 *
 */
public class ObsrvCalculator {
	
	private String outDir;
	
	public ObsrvCalculator(String outDir){
		this.outDir = outDir;
	}
	
	public void observeredAgreementATCLevel(String annotator1, String annotator2){
 
        // user annotator2 as a referenceset

        EudraCorpus corpus = new EudraCorpus(annotator1);
        EudraCorpus gs = new EudraCorpus(annotator2);

        EudraEvaluateResult evaluateResult = new EudraEvaluateResult(annotator1);
        
        
        AnnotatorResult annResult = new AnnotatorResult("EVPeds_", this.outDir);

        // each drug in the reference set checks drug in the other list as candidates
        for (EudraRecord record : gs.getRecords()) {
        	
        	// look for corresponding drug in the other list
        	List<DrugCandidate> drugCandidates = new ArrayList<>();
            for(EudraRecord c : corpus.getRecords()){

                if (record.getSubstance().trim().equalsIgnoreCase(c.getSubstance().trim())){

                    for(String atc : c.getAtcs()){

                        DrugCandidate d = new DrugCandidate();
                        d.setDrugName(c.getSubstance());
                        d.setAtc(atc);
                        drugCandidates.add(d);
                    }
                    
                    AnnotatorEntry aE = new AnnotatorEntry(record.getSubstance());
                    
                    aE.addATC1(record.getAtcs());
       			 	aE.addATC2(c.getAtcs());
       			 
       			 	annResult.addEntry(aE);

                    break;
                }

            }
            
            EudraEvaluateEntry e = new EudraEvaluateEntry();
            e.setTerm(record.getSubstance());
            e.setAtcs(record.getAtcs());
            e.setDrugCandidates(drugCandidates);
            e.evaluate();
            evaluateResult.addEudraEvaluate(e);
           

        }

        
        
        evaluateResult.printAll();
        //evaluateResult.exportAllResults(outDir, level);
        //evaluateResult.exportResult(outDir);
        annResult.exportResults2Sets();  
    }
    
    public void observeredAgreement(String annotator1, String annotator2, String annotator3){

       
        // user annotator2 as a referenceset

        EudraCorpus corpus1 = new EudraCorpus(annotator1);
        EudraCorpus corpus2 = new EudraCorpus(annotator2); 
        EudraCorpus corpus3 = new EudraCorpus(annotator3);
       
        
        AnnotatorResult annResult = new AnnotatorResult("EVPeds_", "data/output/");

        // each drug in the reference set checks drug in the other list as candidates
        for (EudraRecord c1 : corpus1.getRecords()) {
        	
        	// look for corresponding drug in the other list   
        	AnnotatorEntry aE = new AnnotatorEntry(c1.getSubstance());
        	aE.addATC1(c1.getAtcs());
            for(EudraRecord c2 : corpus2.getRecords()){
            	
                if (c1.getSubstance().trim().equalsIgnoreCase(c2.getSubstance().trim())){          
                   
       			 	aE.addATC2(c2.getAtcs());
                    break;
                }

            }
            
            for(EudraRecord c3 : corpus3.getRecords()){
            	
                if (c1.getSubstance().trim().equalsIgnoreCase(c3.getSubstance().trim())){    
       			 	aE.addATC3(c3.getAtcs());
                    break;
                }

            }
            
            annResult.addEntry(aE);
            System.out.println(aE.toString()); 

        } 
        
        annResult.exportResults();  
    }
	
	public void observeredAgreementDrugLevel(String annotator1, String annotator2){
   	  
        // user annotator2 as a referenceset

        EudraCorpus corpus = new EudraCorpus(annotator1);

        EudraCorpus gs = new EudraCorpus(annotator2);
        
        EudraEvaluateResult evaluateResult = new EudraEvaluateResult(annotator1);
        
        for (EudraRecord record : gs.getRecords()) {
       	 String[] atcList1 = record.getAtcs();
       	
       	 List<DrugCandidate> drugCandidates = new ArrayList<>();
       	
       	 
       	 for(EudraRecord c : corpus.getRecords()){        		 
       		 
       		 if (record.getSubstance().trim().equalsIgnoreCase(c.getSubstance().trim())){
       			 
       			 String[] atcList2 = c.getAtcs();
       			 DrugCandidate d = new DrugCandidate();
       			 d.setDrugName(c.getSubstance());
       			 String atc = "";
       			 if (Arrays.equals(atcList1, atcList2)){
       				 
                        atc = String.join(",", atcList1);
                        d.setAtc(atc);                        
                        record.setAtcs(new String[]{atc});                        
                        
       				 //System.out.println(String.format("%s [%s] vs [%s]; equal: %s",record.getSubstance(), String.join(",", atcList1), String.join(",", atcList2),Arrays.equals(atcList1, atcList2)));
                    // if does not match, make two ATCs are different
       			 }else{
       				 atc = String.join(",", atcList2);
       				 d.setAtc(atc);
       				 record.setAtcs(new String[]{ (String.join(",", atcList1) )}) ;  
       				 
       			 }
       			 
       			 
       			 drugCandidates.add(d);
       			 
       			 break;
       			 
       		 }        		
       		 
       	 }
       	 
       	 
   		 EudraEvaluateEntry e = new EudraEvaluateEntry();
            e.setTerm(record.getSubstance());
            e.setAtcs(record.getAtcs());
            e.setDrugCandidates(drugCandidates);
            e.evaluate();
            evaluateResult.addEudraEvaluate(e);
       	 
        }
        
       
        evaluateResult.printAll();
        //evaluateResult.exportAllResults(outDir, level);
        //evaluateResult.exportResult(outDir);
        
   }

}
