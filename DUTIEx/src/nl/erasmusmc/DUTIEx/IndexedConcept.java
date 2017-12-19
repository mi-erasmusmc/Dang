package nl.erasmusmc.DUTIEx;

import java.util.ArrayList;

public class IndexedConcept {
	
	private String cui;
	private ArrayList<String> found_text;
	private String preferred_term;
		
 
	public IndexedConcept() {
		found_text = new ArrayList<String>();
	}
	
	public String getCui() {
		return cui;
	}
	public void setCui(String cui) {
		this.cui = cui;
	}
	
	public ArrayList<String> getFound_textList() {
		return found_text;
	}
	
	public void addFound_text(String found_text) {
		if (! this.found_text.contains(found_text)){
			this.found_text.add(found_text);
		}
		
	}
	
	public String getPreferred_term() {
		return preferred_term;
	}


	public void setPreferred_term(String preferred_term) {
		this.preferred_term = preferred_term;
	}

}
