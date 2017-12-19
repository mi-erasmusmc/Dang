package nl.erasmusmc.biosemantics.eudra.drugs;

import java.util.ArrayList;

public class Substances {
	private String cui;
	private String substance;
	private String substance_norm;
	private Boolean preferred;
	private String atc;
	private String sab;
	private Float score;
	private Long frequency;
	
	public Substances(){
		this.cui = null;
		this.substance = null;
		this.substance_norm = null;
		this.preferred = null;
		this.atc = null;
		this.sab = null;
		this.score = null;
		this.frequency = null;
	}

	
	public String getSubstance_norm() {
		return substance_norm;
	}


	public void setSubstance_norm(String substance_norm) {
		this.substance_norm = substance_norm;
	}


	public void setPreferred(Boolean preferred) {
		this.preferred = preferred;
	}


	public String getCui() {
		return cui;
	}

	public void setCui(String cui) {
		this.cui = cui;
	}

	public String getSubstance() {
		return substance;
	}

	public void setSubstance(String term) {
		this.substance = term;
	}

	public Boolean getPreferred() {
		return preferred;
	}

	public void setPreferred(String preferred) {
		 
		this.preferred = preferred.equals("P");
	}

	public String getAtc() {
		return atc;
	}

	public void setAtc(String atc) {
		this.atc = atc;
	}

	public String getSab() {
		return sab;
	}

	public void setSab(String sab) {
		this.sab = sab;
	}

	public Float getScore() {
		return score;
	}

	public void setScore(Float score) {
		this.score = score;
	}

	public Long getFrequency() {
		return frequency;
	}

	public void setFrequency(Long frequency) {
		this.frequency = frequency;
	}
	
	@Override
	public String toString(){
		return this.score + ":" + this.getSubstance() + " (CUI: " + this.cui + ") " + this.getFrequency() + ", " + "ATC: " + this.getAtc() ;
	}
}
