package nl.erasmusmc.DUTIEx;

import java.util.ArrayList;

public class ResultSet {
	private String filename;	
	private String txt;	
	private ArrayList<IndexedConcept> concept;	
	
	public ArrayList<IndexedConcept> getConcept() {
		return concept;
	}


	public Boolean existsCUI(String cui){
		for(IndexedConcept i : concept){
			if (i.getCui().equals(cui)){
				return true;
			}
		}
		
		return false;
	}
	
	public void addFoundText(String cui, String text){
		
		for(IndexedConcept i : concept){
			if (i.getCui().equals(cui)){
				i.addFound_text(text);
				break;
			}
		}
		
	}
	
	public void setConcept(ArrayList<IndexedConcept> item) {
		this.concept = item;
	}


	public ResultSet(){
		concept = new ArrayList<IndexedConcept>();		 
	}
	 
	
	 
	public String getFilename() {
		return filename;
	}


	public void setFilename(String filename) {
		this.filename = filename;
	}


	public String getTxt() {
		return txt;
	}
	public void setTxt(String txt) {
		this.txt = txt;
	}
	 
	
	public void addConcept(IndexedConcept i){
		this.concept.add(i);
	}
	

}
