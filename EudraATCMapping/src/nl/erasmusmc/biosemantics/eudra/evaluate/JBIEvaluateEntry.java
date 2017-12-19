package nl.erasmusmc.biosemantics.eudra.evaluate;

import java.util.ArrayList;
import java.util.List;

import nl.erasmusmc.biosemantics.eudra.drugs.DrugCandidate;
import nl.erasmusmc.biosemantics.eudra.drugs.RxNormCandidate;

/**
 * @author dangvh
 *
 */
public class JBIEvaluateEntry {
	private String cui;
	private String drugname;
	private String drugnameMapped;
	private String rxCui;
	private String rxCuiMedEx;
	private String comparison;
	private String rxCuiMapped;
	private Measurement evaluated;
	
	/**
	 * 
	 */
	public JBIEvaluateEntry(){
		cui = null;
		drugname = null;
		rxCui = null;
		rxCuiMedEx = null;
		this.comparison = null;
		this.rxCuiMapped = null;
		this.evaluated = null;
	}
	
	
	/**
	 * @param cui
	 * @param rxCui
	 * @param drugname
	 * @param rxCuiMedEx
	 * @param drugnameMapped
	 * @param comoparison
	 * @param rxCuimapped
	 */
	public JBIEvaluateEntry(String cui, String rxCui, String drugname, String rxCuiMedEx, String drugnameMapped, String comoparison, String rxCuimapped){
		this.cui = cui;
		this.rxCui = rxCui;
		this.rxCuiMedEx = rxCuiMedEx;
		this.drugnameMapped = drugnameMapped;
		this.drugname = drugname;
		this.comparison = comoparison;
		this.rxCuiMapped = rxCuimapped;
	}
	
		
	public String getRxCuiMedEx() {
		return rxCuiMedEx;
	}

	public void setRxCuiMedEx(String rxCuiMedEx) {
		this.rxCuiMedEx = rxCuiMedEx;
	}

	public String getCui() {
		return cui;
	}

	public void setCui(String cui) {
		this.cui = cui;
	}

	public String getDrugnameMapped() {
		return drugnameMapped;
	}

	public void setDrugnameMapped(String drugnameMapped) {
		this.drugnameMapped = drugnameMapped;
	}

	public String getDrugname() {
		return drugname;
	}
	public void setDrugname(String drugname) {
		this.drugname = drugname;
	}
	public String getRxCui() {
		return rxCui;
	}
	public void setRxCui(String rxCuiJBI) {
		this.rxCui = rxCuiJBI;
	}
	public String getComparison() {
		return comparison;
	}
	public void setComparison(String comparison) {
		this.comparison = comparison;
	}
	public String getRxCuiMapped() {
		return rxCuiMapped;
	}
	public void setRxCuiMapped(String rxCuiMapped) {
		this.rxCuiMapped = rxCuiMapped;
	}

	public Measurement getEvaluated() {
		return evaluated;
	}

	public void setEvaluated(Measurement evaluated) {
		this.evaluated = evaluated;
	}

	
		
	
}
