/**
 * 
 */
package nl.erasmusmc.biosemantics.eudra.evaluate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.opencsv.CSVWriter;

import info.debatty.java.stringsimilarity.Levenshtein;
import nl.erasmusmc.biosemantics.eudra.drugs.DrugCandidate;

/**
 * @author dangvh
 *
 */
public class EudraEvaluateResult {
	
	
	private int FP;	// if drug found but atc does not matched
	private int TP;	// if drug found and matched ATC
	private int TN;	// if drug does not found and drug has atc
	private int FN; // if drug does not found  but the drug has atc
	private String datasetPath;
	
	
	private String output;
	List<EudraEvaluateEntry> evaluated;

	public EudraEvaluateResult(){
		this.TP = 0;
		this.FP = 0;
		this.TN = 0;
		this.FN = 0;
		this.datasetPath = null;
		
		evaluated = new ArrayList<EudraEvaluateEntry>();
	}

	public EudraEvaluateResult(String dataset){
		this.TP = 0;
		this.FP = 0;
		this.TN = 0;
		this.FN = 0;
		this.datasetPath = dataset;

		evaluated = new ArrayList<EudraEvaluateEntry>();
	}

	public String getDataset(){
		if (datasetPath != null){
			Path p = Paths.get(datasetPath);
			return p.getFileName().toString();
		}else{
			return "";
		}
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
		return String.format("TP = %d\nFP = %d\nFN = %d\nTN = %d\nAccuracy = %.3f\nPrecision = %.3f\nRecall(Sensitivity) = %.3f" +
							"\nSpecificity = %.3f\nF-score = %.3f" +
				"\nTP\t\tFP\t\tFN\t\tTN\t\tAccuracy\tPrecision\tRecall (Sensitivity)\tSpecificity\t\tF-score" +
				"\n%d\t\t%d\t\t%d\t\t%d\t\t%.3f\t\t%.3f\t\t%.3f\t\t\t\t\t%.3f\t\t\t%.3f",
				getTP(), getFP(), getFN(), getTN(), getAccuracy(), getPrecision(),getRecall(), getSpecificity(),getFscore(),
				getTP(), getFP(), getFN(), getTN(), getAccuracy(), getPrecision(),getRecall(), getSpecificity(),getFscore());
	}

	public String format(){
		return String.format("TP\tFP\tFN\tTN\tAccuracy\tPrecision\tRecall (Sensitivity)\tSpecificity\tF-score" +
						"\n%d\t%d\t%d\t%d\t%.3f\t%.3f\t%.3f\t%.3f\t%.3f",
				getTP(), getFP(), getFN(), getTN(), getAccuracy(), getPrecision(),getRecall(), getSpecificity(),getFscore());
	}
	
	private Double getAccuracy() {
		return ( (double) getTP() + (double) getTN() ) / ( (double) getTP() + (double) getTN() + (double) getFP() + (double) getFN() );
	}

	// 
	public Double getSpecificity(){
		return (double) getTN() / ( (double) getFP() + (double) getTN() );
	}
	
	// Recall
	public Double getSensitivity(){
		return (double) getTP() / ( (double) getTP() + (double) getFN() );
	}
	
	public Double getPrecision(){
		return (double) getTP() / ( (double) getTP() + (double) getFP() );
	}
	
	public Double getRecall(){
		return (double) getTP() / ((double) getTP() + (double) getFN());
	}
	
	public Double getFscore(){
		return (double) 2*getPrecision()*getRecall()/(getPrecision() + getRecall());
	}
	
	
	public String getOutput() {
		return output;
	}


	public void setOutput(String output) {
		this.output = output;
	}




	public List<EudraEvaluateEntry> getEvaluated() {
		return evaluated;
	}

	public void setEvaluated(List<EudraEvaluateEntry> evaluated) {
		this.evaluated = evaluated;
	}
	
	public void addEudraEvaluate(EudraEvaluateEntry e){
		this.evaluated.add(e);
		this.addTP(e.getTPcandidates().size());
		this.addFP(e.getFPcandidates().size());
		this.addFN(e.getFNcandidates().size());
		this.addTN(e.getTNcandidates().size());
		
	}
	
	public void printAll(String dir){
		if (! dir.endsWith("/")){
			this.output = dir + "/";
		}else{
			this.output = dir;
		}
		
		printAll();
	}
	
	public void printAll(){
		int i =0;
		
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd_HH.mm").format(new Date());
		
		String fOutpuf =  this.output + "Eudra_" +  getDataset().substring(0, getDataset().length()-4) + "_" + timeStamp + ".txt";
		String text;
		
		text = String.format("Date: %s\n", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
		writeText(fOutpuf, text, false);
		
		for (EudraEvaluateEntry e : evaluated){
			
			
			
			System.out.println((++i) + ". Drug: " + e.getTerm() + "\t" + e.getAtcString());
			text = (i) + ". Drug: " + e.getTerm() + "\t" + e.getAtcString() + "\n";
			int k = 1;
			
			text += "\n\t-------------TP------------------ \n";
			
			System.out.println("\n\t-------------TP------------------ ");
			for(DrugCandidate d: e.getTPcandidates()){
								
				System.out.println(String.format("\t%d.%s", k++, d.toString()));
				text += String.format("\t%d.%s", k, d.toString()) + "\n";
				
			}
			
			writeText(fOutpuf, text, true);			
			k = 1;			
			text = "\n\t-------------FP------------------ \n";
			System.out.println("\n\t-------------FP------------------ ");
			for(DrugCandidate d: e.getFPcandidates()){
				 
				System.out.println(String.format("\t%d.%s", k++, d.toString()));
				text += String.format("\t%d.%s", k, d.toString()) + "\n";
			}
			
			writeText(fOutpuf, text, true);			
			k = 1;			
			text = "\n\t-------------FN------------------ \n";
			System.out.println("\n\t-------------FN------------------ ");
			for(DrugCandidate d: e.getFNcandidates()){
				
				System.out.println(String.format("\t%d.%s", k++, d.toString()));
				text += String.format("\t%d.%s", k, d.toString()) + "\n";
			}
			
			writeText(fOutpuf, text, true);			
			k = 1;			
			text = "\n\t-------------TN------------------ \n";
			System.out.println("\n\t-------------TN------------------ ");
			for(DrugCandidate d: e.getTNcandidates()){
				System.out.println(String.format("\t%d.%s", k++, d.toString()));
				text += String.format("\t%d.%s", k, d.toString()) + "\n";
			}
			
			writeText(fOutpuf, text, true);	
			System.out.print("");
			
		}
		
		
		writeText(fOutpuf, "\n\n" +this.toString(), true);	
		writeText(fOutpuf, "\n" + this.format(), true);	
		System.out.println(this.toString());
		System.out.println(this.format());
		
	}
	
	public void printTP(){
		int i =0;
		for (EudraEvaluateEntry e : evaluated){
			if (e.getTPcandidates().size() > 0){
				System.out.println((++i) + ". Drug: " + e.getTerm()+ "\t" + e.getAtcString());
				System.out.println("\t--- TP --- ");
				for(DrugCandidate d: e.getTPcandidates()){
					System.out.println("\tDrug:" + d.getDrugName() +  "("+ d.getCui() + ")" + ";\tATC: " + d.getAtc());
				}
			}
			
		}
		
		System.out.println(this.toString());
	}
	
	public void printFP(){
		int i =0;
		for (EudraEvaluateEntry e : evaluated){
			if (e.getFPcandidates().size() > 0){
				System.out.println((++i) + ". Drug: " + e.getTerm()+ "\t" + e.getAtcString());
				System.out.println("\t--- FP --- ");
				for(DrugCandidate d: e.getFPcandidates()){
					System.out.println("\tDrug:" + d.getDrugName() +  "("+ d.getCui() + ")" + ";\tATC: " + d.getAtc());
				}
			}
			
		}
		
		System.out.println(this.toString());
	}
	
	public void printTN(){
		int i =0;
		for (EudraEvaluateEntry e : evaluated){
			if (e.getTNcandidates().size() > 0){
				System.out.println((++i) + ". Drug: " + e.getTerm()+ "\t" + e.getAtcString());
				System.out.println("\t--- TN --- ");
				for(DrugCandidate d: e.getTNcandidates()){
					System.out.println("\tDrug:" + d.getDrugName() +  "("+ d.getCui() + ")" + ";\tATC: " + d.getAtc());
				}
			}
			
		}
		
		System.out.println(this.toString());
	}
	
	public void printFN(){
		int i =0;
		for (EudraEvaluateEntry e : evaluated){
			if (e.getFNcandidates().size() > 0){
				System.out.println((++i) + ". Drug: " + e.getTerm()+ "\t" + e.getAtcString());
				System.out.println("\t--- FN --- ");
				for(DrugCandidate d: e.getFNcandidates()){
					System.out.println("\tDrug:" + d.getDrugName() +  "("+ d.getCui() + ")" + ";\tATC: " + d.getAtc());
				}
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
	
	public void exportResultForDelivery(String dir){
		if (! dir.endsWith("/")){
			this.output = dir + "/";
		}else{
			this.output = dir;
		}
		 
		exportResultForDelivery();
		System.out.println("Eudra_" + getDataset() + "_" + new SimpleDateFormat("yyyy.MM.dd.HH.mm").format(new Date()) + ".csv");
	}
	 
	public void exportResultForDelivery(){
		
		String[] header = {"drugname", "ATC1","ATC2","ATC3","ATC4","ATC5","ATC6","ATC7","ATC8","ATC9","ATC10","ATC11"};
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd_HH.mm").format(new Date());
		String fFile = this.output + "Eudra_" + getDataset() + "_" + timeStamp + "_delivery.csv";
		
		saveData(fFile, header);
		
		ArrayList<String[]> lines = new ArrayList<String[]>();
		 
		for (EudraEvaluateEntry e : evaluated){
			String[] line = new String[20];
			
			line[0] = e.getTerm();
			 			
			int i = 1;			 
			for(DrugCandidate d: e.getFPcandidates()){
				line[i++] =   d.getAtc().toUpperCase()  ;
			}
			 
			 
			lines.add(line);
			if (lines.size() % 1000 == 0){
				saveData(fFile, lines, true);
				lines = new ArrayList<String[]>();
			}
			
		}
		
		saveData(fFile, lines, true);
		
		
		
	}

	public void exportResult(){
		
		String[] header = {"drugname", "TP", "FP", "FN", "TN"};
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd_HH.mm").format(new Date());
		
		String fOutpuf =  "Eudra_" + getDataset().substring(0, getDataset().length()-4) + "_" + timeStamp + ".csv";
		
		String fFile = this.output +  fOutpuf;
		
		saveData(fFile, header);
		
		ArrayList<String[]> lines = new ArrayList<String[]>();
		 
		for (EudraEvaluateEntry e : evaluated){
			
			String[] tp, fp, tn, fn; 
		 
			int i = 0;
			tp = new String[e.getTPcandidates().size()];
			for(DrugCandidate d: e.getTPcandidates()){
				tp[i++] = d.getDrugName() + "("+ d.getCui()  + ")[" + d.getAtc() + "]";
			}
			
			i = 0;
			fp = new String[e.getFPcandidates().size()];
			for(DrugCandidate d: e.getFPcandidates()){
				fp[i++] = d.getDrugName() + "("+ d.getCui() + ")[" + d.getAtc() + "]";
			}
			
			i = 0;
			tn = new String[e.getTNcandidates().size()];
			for(DrugCandidate d: e.getTNcandidates()){
				tn[i++] = d.getDrugName() + " ("+ d.getCui() + ")[" + d.getAtc() + "]";
			}
			
			i = 0;
			fn = new String[e.getFNcandidates().size()];
			for(DrugCandidate d: e.getFNcandidates()){
				fn[i++] = d.getDrugName() + "("+ d.getCui() + ")[" + d.getAtc() + "]";
			}
			String[] line = {e.getTerm() + e.getAtcString(), String.join(",\n", tp), String.join(",\n", fp), String.join(",\n", fn), String.join(",\n", tn)};
			lines.add(line);
			if (lines.size() % 1000 == 0){
				saveData(fFile, lines, true);
				lines = new ArrayList<String[]>();
			}
			
		}
		
		String[] line = {"TP:" ,  Integer.toString(getTP()), null, null,null};
		lines.add(line);
		
		line = new String[5];
		line[0] = "FP:";
		line[1] = Integer.toString(getFP());
		lines.add(line);
		
		
		line = new String[5];		
		line[0] = "FN:";
		line[1] = Integer.toString(getFN());
		lines.add(line);
		
		line = new String[5];
		line[0] = "TN:";
		line[1] = Integer.toString(getTN());
		
		lines.add(line);
		
		
		line = new String[5];
		line[0] = "Accuracy: ";
		line[1] = String.format("%.3f", getAccuracy());
		lines.add(line);
		
		line = new String[5];
		line[0] = "Precision:";
		line[1] =  String.format("%.3f", getPrecision());
		lines.add(line);
		
		line = new String[5];
		line[0] = "Recall (Sensitivity):";
		line[1] =  String.format("%.3f", getRecall());
		lines.add(line);
		
		line = new String[5];
		line[0] = "Specificity:";
		line[1] =  String.format("%.3f", getSpecificity());
		lines.add(line);
		
		line = new String[5];
		line[0] = "F-score:";
		line[1] =  String.format("%.3f", getFscore());
		lines.add(line);
		
		saveData(fFile, lines, true);
		
		System.out.println(fFile);
		
	}
	
	
	
	public void export(String mode){
		String[] header = {"substance", "indexing", "atc", "Levenshtein distance"};
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd_HH.mm").format(new Date());
		String fFile = this.output + "Eudra_" + getDataset() + "_" +  mode +"_" + timeStamp + ".csv";
		saveData(fFile, header);
		ArrayList<String[]> lines = new ArrayList<String[]>();
		int i = 0;
		
		Levenshtein levendis = new Levenshtein();
		double distance; 
		for (EudraEvaluateEntry e : evaluated){
			
			List<DrugCandidate> candidates;
			switch (mode){
			case "TP":
				candidates = e.getTPcandidates();
				break;
			case "FP":
				candidates = e.getFPcandidates();
				break;
			case "TN":
				candidates = e.getTNcandidates();
				break;
			case "FN":
				candidates = e.getFNcandidates();
				break;
			default:
				candidates = new ArrayList<DrugCandidate>();
			}
			
			
			for(DrugCandidate d: candidates){
				if (d.getDrugNorm() != null && e.getNormalizedTerm() != null){
					distance = levendis.distance(d.getDrugNorm(), e.getNormalizedTerm()) ;
				}else{
					distance = 0.0;
				}
				
				 
				lines.add(new String[]{e.getTerm() + " " + e.getAtcString(), d.getDrugName(), d.getAtc(),  Double.toString(distance) });
				 
				
				 i++;
				 if (i % 100 == 0){
					 saveData(fFile,lines,true);
					 lines = new ArrayList<String[]>();
				 }
			}
		}
		saveData(fFile,lines,true);
		
	}
	
	
	
	public void exportAllResults(String dir){
		if (! dir.endsWith("/")){
			this.output = dir + "/";
		}else{
			this.output = dir;
		}
		 
		 exportAllResults();
	}
	
	public void exportAllResults(){
		export("TP");
		export("FP");
		export("TN");
		export("FN");
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
	
	public void writeText(String filename, String text, Boolean append){
		 
		try{
			File f = new File(filename);
			 
			FileOutputStream foS = new FileOutputStream(f,append);
			OutputStreamWriter writer = new OutputStreamWriter(foS);
			writer.write(text);
			
			writer.close();
		}catch(Exception e){
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
