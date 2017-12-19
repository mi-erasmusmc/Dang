package nl.erasmusmc.biosemantics.eudra.evaluate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AnnotatorEntry {
	private String drug;
	private List<String> atc1; // list of annotator 1
	private List<String> atc2; // list of annotator 2
	private List<String> atc3; // list of annotator 3
	

	
	public AnnotatorEntry(){
		drug = null;
		atc1 = new ArrayList<String>();
		atc2 = new ArrayList<String>();
		atc3 = new ArrayList<String>();
		
	}
	
	public AnnotatorEntry(String drug){
		this.drug = drug;
		atc1 = new ArrayList<String>();
		atc2 = new ArrayList<String>();
		atc3 = new ArrayList<String>();
	}
	

	public AnnotatorEntry(String drug, List<String> atc1, List<String> atc2, List<String> atc3){
		
		this.drug = drug;
		this.atc1 = new ArrayList<String>(atc1);
		this.atc2 = new ArrayList<String>(atc2);	
		this.atc2 = new ArrayList<String>(atc3);		
		
	}
	
	public String toString(){
		this.sort();
		
		return String.format("%s\n \tAnnotator1: %s\n\tAnnotator2: %s\n\tAnnotator3: %s", this.drug, String.join("\t", this.atc1), String.join("\t", this.atc2), String.join("\t", this.atc3));
	}
	
	public void addATC1(String atc){
		this.atc1.add(atc);		 
	}
	
	public void addATC1(String[] atcs){
		for(String s : atcs){
			this.atc1.add(s);
		}
	}
	
	public void addATC2(String[] atcs){
		for(String s : atcs){
			this.atc2.add(s);
		}
	}
	
	public void addATC2(String atc){
		this.atc2.add(atc);
	}
	
	public void addATC3(String[] atcs){
		for(String s : atcs){
			this.atc3.add(s);
		}
	}
	
	public void addATC3(String atc){
		this.atc3.add(atc);
	}
	
	public void sort(){
		
		// sort atc1
		Collections.sort(atc1, new Comparator<Object>(){
			@Override
			public int compare(Object arg0, Object arg1) {
				 
				return ((String)arg0).compareTo((String) arg1) ;
			}
			
		});
		
		// sort atc2
		Collections.sort(atc2, new Comparator<Object>(){
			@Override
			public int compare(Object arg0, Object arg1) {
				 
				return ((String)arg0).compareTo((String) arg1) ;
			}
			
		});
		
		// sort atc3
		Collections.sort(atc3, new Comparator<Object>(){
			@Override
			public int compare(Object arg0, Object arg1) {
				 
				return ((String)arg0).compareTo((String) arg1) ;
			}
			
		});
	}


	public String getDrug() {
		return drug;
	}



	public void setDrug(String drug) {
		this.drug = drug;
	}



	public List<String> getAtc1() {
		return atc1;
	}



	public void setAtc1(List<String> atc1) {
		this.atc1 = atc1;
	}



	public List<String> getAtc2() {
		return atc2;
	}


	public void setAtc2(List<String> atc2) {
		this.atc2 = atc2;
	}
	
	public void setAtc3(List<String> atc3) {
		this.atc3 = atc3;
	}
	
	public List<String> getAtc3() {
		return atc3;
	}
	
}
