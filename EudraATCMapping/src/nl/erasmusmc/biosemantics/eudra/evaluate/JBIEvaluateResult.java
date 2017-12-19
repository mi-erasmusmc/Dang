/**
 * 
 */
package nl.erasmusmc.biosemantics.eudra.evaluate;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opencsv.CSVWriter;

import nl.erasmusmc.biosemantics.eudra.drugs.RxNormCandidate;



/**
 * @author dangvh
 *
 */
public class JBIEvaluateResult {
	
	
	private int FP;
	private int TP;
	private int TN;
	private int FN;
	
	private int TPMedEx;
	private int FPMedEx;
	private int TNMedEx;
	private int FNMedEx;
	
	
	private String output;
	List<JBIEvaluateEntry> evaluated;

	public JBIEvaluateResult(){
		this.TP = 0;
		this.FP = 0;
		this.TN = 0;
		this.FN = 0;
		
		this.FNMedEx = 0;
		this.FPMedEx = 0;
		this.TNMedEx = 0;
		this.TPMedEx = 0;
		evaluated = new ArrayList<JBIEvaluateEntry>();
	}
	
		
	public int getTPMedEx() {
		return TPMedEx;
	}


	public void addTPMedEx() {
		TPMedEx += 1;
	}

	public int getFPMedEx() {
		return FPMedEx;
	}

	public void addFPMedEx() {
		FPMedEx += 1;
	}

	public int getTNMedEx() {
		return TNMedEx;
	}

	public void addTNMedEx() {
		TNMedEx += 1;
	}

	public int getFNMedEx() {
		return FNMedEx;
	}

	public void addFNMedEx() {
		FNMedEx += 1;
	}

	public int getFP() {
		return FP;
	}
	
	public void setFP(int FP) {
		this.FP = FP;
	}
	
	public int getTP() {
		return TP;
	}
	
	public void setTP(int TP) {
		this.TP = TP;
	}
	
	public int getTN() {
		return TN;
	}
	
	public void setTN(int TN) {
		this.TN = TN;
	}
	
	public int getFN() {
		return FN;
	}
	
	public void setFN(int FN) {
		this.FN = FN;
	}
	
	public void addFP(){
		this.FP++;
	}
	
	public void addFP(int i){
		this.FP += i;
	}

	public void addTP(){
		this.TP++;
	}
	
	public void addTP(int i){
		this.TP += i;
	}

	public void addFN(){
		this.FN++;
	}
	
	public void addFN(int i){
		this.FN += i;
	}

	public void addTN(){
		this.TN++;
	}

	public void addTN(int i){
		this.TN += i;
	}

	public String toString(){
		String s = String.format("\nMedEx TP  = %d\nMedEx FP = %d\nMedEx FN = %d\nTN MedEx = %d\nMedEx Accuracy = %.3f\nMedEx Precision = %.3f\nMedEx Recall(Sensitivity) = %.3f" +
				"\nMedEx Specificity = %.3f\nMedEx F-score = %.3f",getTPMedEx(), getFPMedEx(), getFNMedEx(), getTNMedEx(), getJBIAccuracy(), getJBIPrecision(),getJBIRecall(), getJBISpecificity(),getJBIFscore());
		s += String.format("\n\nTP = %d\nFP = %d\nFN = %d\nTN = %d\nAccuracy = %.3f\nPrecision = %.3f\nRecall(Sensitivity) = %.3f" +
				"\nSpecificity = %.3f\nF-score = %.3f",getTP(), getFP(), getFN(), getTN(), getAccuracy(), getPrecision(),getRecall(), getSpecificity(),getFscore());
		return s;
	}
	
	private Double getAccuracy() {
		return ( (double) getTP() + (double) getTN() ) / ( (double) getTP() + (double) getTN() + (double) getFP() + (double) getFN() );
	}

	private Double getJBIAccuracy() {
		return ( (double) getTPMedEx() + (double) getTNMedEx() ) / ( (double) getTPMedEx() + (double) getTNMedEx() + (double) getFPMedEx() + (double) getFNMedEx() );
	}
	
	  
	public Double getSpecificity(){
		return (double) getTN() / ( (double) getFP() + (double) getTN() );
	}
	
	public Double getJBISpecificity(){
		return (double) getTNMedEx() / ( (double) getFPMedEx() + (double) getTNMedEx() );
	}
	
	// Recall
	public Double getSensitivity(){
		return (double) getTP() / ( (double) getTP() + (double) getFN() );
	}
	
	// Recall
		public Double getJBISensitivity(){
			return (double) getTPMedEx() / ( (double) getTPMedEx() + (double) getFNMedEx() );
		}
	
	public Double getPrecision(){
		return (double) getTP() / ( (double) getTP() + (double) getFP() );
	}
	
	public Double getJBIPrecision(){
		return (double) getTPMedEx() / ( (double) getTPMedEx() + (double) getFPMedEx() );
	}
	
	public Double getRecall(){
		return (double) getTP() / ((double) getTP() + (double) getFN());
	}
	
	public Double getJBIRecall(){
		return (double) getTPMedEx() / ((double) getTPMedEx() + (double) getFNMedEx());
	}
	
	public Double getFscore(){
		return (double) 2*getPrecision()*getRecall()/(getPrecision() + getRecall());
	}
	
	public Double getJBIFscore(){
		return (double) 2*getJBIPrecision()*getJBIRecall()/(getJBIPrecision() + getJBIRecall());
	}
	
	public String getOutput() {
		return output;
	}


	public void setOutput(String output) {
		this.output = output;
	}

 

	public List<JBIEvaluateEntry> getEvaluated() {
		return evaluated;
	}

	public void setEvaluated(List<JBIEvaluateEntry> evaluated) {
		this.evaluated = evaluated;
	}
	
		
	public void addJBIEvaluate(JBIEvaluateEntry e){
		this.evaluated.add(e);
		 
		switch (e.getEvaluated()) {
		case TP:
			this.addTP();
			break;
		case FP:
			this.addFP();
			break;
		case TN:
			this.addTN();
			break;
		case FN:
			this.addFN();
			break;
		}
		
	 
		switch (e.getComparison()){
		case "TP":
			this.addTPMedEx();
			break;
		case "FP":
			this.addFPMedEx();
			break;
		case "TN":
			this.addTNMedEx();
			break;
		case "FN":
			this.addFNMedEx();
			break;		
		}
		
	}
	
	public void printAll(){
		int i =0;
		for (JBIEvaluateEntry e : evaluated){
			System.out.println((++i) + ". Drug: " + e.getDrugname() + "\tRxCUI: " + e.getRxCui() + "\tMedEx:" + e.getComparison());
			System.out.println("\t" + e.getEvaluated().toString() + ": " + e.getDrugnameMapped() + "(" + e.getCui() + "); RxCUI: " + e.getRxCuiMapped());			
		}
		
		System.out.println(this.toString());
		
	}
	
	public void printTP(){
		
		int i =0;
		for (JBIEvaluateEntry e : evaluated){
			if (e.getEvaluated().equals(Measurement.TP)){
				System.out.println((++i) + ". Drug: " + e.getDrugname() + "\tRxCUI: " + e.getRxCui() + "\tMedEx:" + e.getComparison());
				System.out.println("\t" +e.getEvaluated().toString() + ": " + e.getDrugnameMapped() + "(" + e.getCui() + "); RxCUI: " + e.getRxCuiMapped());
			}
						
		}
		
		System.out.println(this.toString());
	}
	
	public void printFP(){
		int i =0;
		for (JBIEvaluateEntry e : evaluated){
			if (e.getEvaluated().equals(Measurement.FP)){
				System.out.println((++i) + ". Drug: " + e.getDrugname() + "\tRxCUI: " + e.getRxCui() + "\tMedEx:" + e.getComparison());
				System.out.println("\t" +e.getEvaluated().toString() + ": " + e.getDrugnameMapped() + "(" + e.getCui() + "); RxCUI: " + e.getRxCuiMapped());
			}
						
		}
		
		System.out.println(this.toString());
	}
	
	public void printTN(){
		int i =0;
		for (JBIEvaluateEntry e : evaluated){
			if (e.getEvaluated().equals(Measurement.TN)){
				System.out.println((++i) + ". Drug: " + e.getDrugname() + "\tRxCUI: " + e.getRxCui() + "\tMedEx:" + e.getComparison());
				System.out.println("\t" +e.getEvaluated().toString() + ": " + e.getDrugnameMapped() + "(" + e.getCui() + "); RxCUI: " + e.getRxCuiMapped());
			}
						
		}
		
		System.out.println(this.toString());
	}
	
	public void printFN(){
		int i =0;
		for (JBIEvaluateEntry e : evaluated){
			if (e.getEvaluated().equals(Measurement.FN)){
				System.out.println((++i) + ". Drug: " + e.getDrugname() + "\tRxCUI: " + e.getRxCui() + "\tMedEx:" + e.getComparison());
				System.out.println("\t" +e.getEvaluated().toString() + ": " + e.getDrugnameMapped() + "(" + e.getCui() + "); RxCUI: " + e.getRxCuiMapped());
			}
						
		}
		
		System.out.println(this.toString());
	}
	
	public void exportResult(String dir){
		if (! dir.endsWith("/")){
			this.output = dir + "/";
		}else{
			this.output = dir;
		}
		 
		 exportResult();
		
	}
	
	
public void exportResult(){
		
		String[] header = {"RxCUI", "Drug Name", "RxCUI MedEx","MedEx comparison" , "CUI" , "RxCUI Mapped", "Drug name Mapped", "Comparison"};
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
		String fFile = this.output + "evaluateResult_" + timeStamp + ".csv";
		
		saveData(fFile, header);
		
		ArrayList<String[]> lines = new ArrayList<String[]>();
		 
		for (JBIEvaluateEntry e : evaluated){			
			String[] line = {e.getRxCui(), e.getDrugname(), e.getRxCuiMedEx(), e.getComparison(), e.getCui(), e.getRxCuiMapped(), e.getDrugnameMapped(), e.getEvaluated().toString()};
			lines.add(line);
			if (lines.size() % 1000 == 0){
				saveData(fFile, lines, true);
				lines = new ArrayList<String[]>();
			}
			
		}
		
		String[] line = {"TP:" + getTP(), null, null, null,null,null,null};
		lines.add(line);
		
		line = new String[7];
		line[0] = "FP: " + getFP();
		lines.add(line);
		
		
		line = new String[7];
		line[0] = "FN: " + getFN();
		lines.add(line);
		
		line = new String[7];
		line[0] = "TN: " + getTN();
		lines.add(line);
		
		
		line = new String[7];
		line[0] = "Accuracy: " + String.format("%.3f", getAccuracy());
		lines.add(line);
		
		line = new String[7];
		line[0] = "Precision: " + String.format("%.3f", getPrecision());
		lines.add(line);
		
		line = new String[7];
		line[0] = "Recall (Sensitivity): " + String.format("%.3f", getRecall());
		lines.add(line);
		
		line = new String[7];
		line[0] = "Specificity: " + String.format("%.3f", getSpecificity());
		lines.add(line);
		
		line = new String[7];
		line[0] = "F-score: " + String.format("%.3f", getFscore());
		lines.add(line);
		
		saveData(fFile, lines, true);
		
	}
	
	
	private void saveData(String filename, String[] line){
    	
    	CSVWriter writer;
		try {
			
			writer = new CSVWriter(new FileWriter(filename, false), ',', CSVWriter.DEFAULT_QUOTE_CHARACTER);
			writer.writeNext(line);
	    	writer.close();
	    	
		} catch (IOException e) {
			 
			System.out.println("Could not write data to csv file.");
			e.printStackTrace();
		}
    	
    	
	}
	
	private void saveData(String filename, ArrayList<String[]> lines, Boolean append){
	    	
	    	CSVWriter writer;
			try {
				
				writer = new CSVWriter(new FileWriter(filename, append), ',', CSVWriter.DEFAULT_QUOTE_CHARACTER);
				for(String[] line : lines){
					writer.writeNext(line);
				}
				
		    	writer.close();
		    	
			} catch (IOException e) {
				 
				System.out.println("Could not write data to csv file.");
				e.printStackTrace();
			}
	    	 
		}
}
