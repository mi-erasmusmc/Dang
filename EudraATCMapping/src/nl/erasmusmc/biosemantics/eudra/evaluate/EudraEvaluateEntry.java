package nl.erasmusmc.biosemantics.eudra.evaluate;

import java.util.ArrayList;
import java.util.List;

import nl.erasmusmc.biosemantics.eudra.Utils.ConceptUtils;
import nl.erasmusmc.biosemantics.eudra.drugs.DrugCandidate;

/**
 * @author dangvh
 *
 */
public class EudraEvaluateEntry {
	
	private String term;	
	private String[] atcs;
	private List<DrugCandidate> drugCandidates;
	private List<DrugCandidate> TPcandidates;
	private List<DrugCandidate> FPcandidates;
	private List<DrugCandidate> TNcandidates;
	private List<DrugCandidate> FNcandidates;
	
	public EudraEvaluateEntry(){
		term = null;
		atcs = null;
		drugCandidates = new ArrayList<DrugCandidate>();
		TPcandidates = new ArrayList<DrugCandidate>();
		FPcandidates = new ArrayList<DrugCandidate>();
		TNcandidates = new ArrayList<DrugCandidate>();
		FNcandidates = new ArrayList<DrugCandidate>();
	}
	
	

	public void evaluate(){
		
		// Skip all ATCs do not match the 5th level;
		int atclevel = 7;
				
		TPcandidates = new ArrayList<DrugCandidate>();
		FPcandidates = new ArrayList<DrugCandidate>();
		TNcandidates = new ArrayList<DrugCandidate>();
		FNcandidates = new ArrayList<DrugCandidate>();
		
		//drugCandidates = removeDuplicate(drugCandidates);		 
		boolean match, sabMatch = false;
		
		List<String> atcMatchedList = new ArrayList<String>();	
		List<String> atcUnmatchedList = new ArrayList<String>();	
		String newAtc, dAtc;
		List<DrugCandidate> newDrugCandidates;

		// if found at least one candidate
		if (drugCandidates.size() > 0){

			boolean hasValidCandidate = false;
			for(DrugCandidate d : drugCandidates) {

				// skip all candidates which has atc do not match the current atc level

				if (d.getAtc().length() < atclevel ){
					continue;
				}else{
					hasValidCandidate = true;
				}

				//System.out.println("REL: " + d.getRela() + " \tSAB:" + d.getSab());

				match = false;
				dAtc = d.getAtc().substring(0, atclevel).toLowerCase();

				for(String atc : atcs){

					if (atc.length() >= atclevel){
						if (dAtc.equalsIgnoreCase(atc.substring(0,atclevel))){
							match = true;
							break;
						}
					}

				}

				if ( match ) {

					// if matched and ATC code does not exists in matched list then count TP

					d.setEvaluated(Measurement.TP);
					d.setAtc(dAtc.toUpperCase());

					if (!atcMatchedList.contains(dAtc)) {
						this.addTPCandidate(d);
						atcMatchedList.add(dAtc);
					}

				} else { // if atc does not match assigned atc
					d.setEvaluated(Measurement.FP);
					d.setAtc(dAtc.toUpperCase());
					if (!atcUnmatchedList.contains(dAtc)) {
						this.addFPCandidate(d);
						atcUnmatchedList.add(dAtc);
					}

				}

			} //end for

			// if all candidates which has invalid ATC level is removed but don't have any one else
			if (! hasValidCandidate){
				DrugCandidate d = new DrugCandidate();
				d.setEvaluated(Measurement.TN);
				d.setDrugName(this.term);
				d.setAtc(null);
				this.addTNCandidate(d);
			}

			// Add all ATCs do not match
			for(String atc : atcs){

				if (atc.length() >= atclevel){

					if ( !atcMatchedList.contains(atc.substring(0,atclevel).toLowerCase()) ){
						DrugCandidate d = new DrugCandidate();
						newAtc = atc.substring(0, atclevel).toLowerCase();
						d.setEvaluated(Measurement.FN);
						d.setDrugName(this.term);
						d.setAtc(newAtc.toUpperCase());
						this.addFNCandidate(d);

					}


				}
			}



		}else{ // not found any candidate


			if (atcs.length > 0){

				for(String atc : atcs){
					if (atc.length() >= atclevel){

						DrugCandidate d = new DrugCandidate();
						newAtc = atc.substring(0, atclevel).toLowerCase();
						d.setDrugName(this.term);
						d.setEvaluated(Measurement.FN);
						d.setAtc(newAtc.toUpperCase());
						this.addFNCandidate(d);
					}
				}
			}else{ // if no atc assigned for this substance, count as TN

				DrugCandidate d = new DrugCandidate();
				d.setDrugName(this.term);
				d.setEvaluated(Measurement.TN);
				d.setAtc(null);
				this.addTNCandidate(d);

			}
		}




	}
	
	
	/*private List<DrugCandidate> removeDuplicate(List<DrugCandidate> list){
		List<DrugCandidate> newList = new ArrayList<DrugCandidate>();
		
		for (DrugCandidate d : list){
			boolean notfound = true;
			for (DrugCandidate dn : newList){
				if (d.getDrugName().equalsIgnoreCase(dn.getDrugName()) && d.getAtc().equalsIgnoreCase(dn.getAtc())){
					notfound = false;
					break;
				}
			}
			if (notfound){
				newList.add(d);
			}
			
		}
		 
		 
		 return newList;
	}
	*/
	
	
	
	
	public List<DrugCandidate> getDrugCandidates() {
		return drugCandidates;
	}



	public void setDrugCandidates(List<DrugCandidate> drugCandidates) {
		this.drugCandidates = drugCandidates;
	}



	public String[] getAtcs() {
		return atcs;
	}

	public String getAtcString(){
		return "[" + String.join(",", atcs) + "]";
		
	}

	public void setAtcs(String[] atc) {
		this.atcs = atc;
	}


	public String getNormalizedTerm(){
		return ConceptUtils.normalizeConcept(this.term);
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public List<DrugCandidate> getTPcandidates() {
		return TPcandidates;
	}

	public void setTPcandidates(List<DrugCandidate> TPcandidates) {
		this.TPcandidates = TPcandidates;
	}
	
	public void addTPCandidate(DrugCandidate d){
		this.TPcandidates.add(d);
	}

	public List<DrugCandidate> getFPcandidates() {
		return FPcandidates;
	}

	public void setFPcandidates(List<DrugCandidate> FPcandidates) {
		this.FPcandidates = FPcandidates;
	}
	
	public void addFPCandidate(DrugCandidate d){
		this.FPcandidates.add(d);
	}

	public List<DrugCandidate> getTNcandidates() {
		return TNcandidates;
	}

	public void setTNcandidates(List<DrugCandidate> tNcandidates) {
		TNcandidates = tNcandidates;
	}
	
	public void addTNCandidate(DrugCandidate d){
		this.TNcandidates.add(d);
	}

	public List<DrugCandidate> getFNcandidates() {
		return FNcandidates;
	}

	public void setFNcandidates(List<DrugCandidate> fNcandidates) {
		FNcandidates = fNcandidates;
	}
	
	public void addFNCandidate(DrugCandidate d){
		this.FNcandidates.add(d);
	}
	 
	
}
