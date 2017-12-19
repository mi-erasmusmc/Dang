package nl.erasmusmc.biosemantics.eudra.evaluate;

public class AnalysisRecord {
	private int FP;
	private int TP;
	private int TN;
	private int FN;
	
	
	public AnalysisRecord(){
		this.TP = 0;
		this.FP = 0;
		this.TN = 0;
		this.FN = 0;
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

	public void addTP(){
		this.TP++;
	}

	public void addFN(){
		this.FN++;
	}

	public void addTN(){
		this.TN++;
	}
	
	public String toString(){
//		return "tp=" + this.getTP() + ",fp=" + this.getFP() + ",fn=" + this.getFN() + ",tn=" + this.getTN() + 
//			   ",prec=" + this.getPrecision() + ",spec=" + this.getSpecificity() + ",sens=" + this.getSensitivity(); 
		
		//return "TP = " + this.getTP() + "\nFP = " + this.getFP() + "\nFN = " + this.getFN() + "\nTN = " + this.getTN() + 
		//		   "\nAccuracy = " + this.getAccuracy() + "\nPrecision = " + getPrecision() + "\nRecall (Sensitivity) = " + getRecall() + 
		//		   "\nSpecificity = " + getSpecificity() + "\nF-score = " + getFscore(); 
		//
		return String.format("TP = %d\nFP = %d\nFN = %d\nTN = %d\nAccuracy = %.3f\nPrecision = %.3f\nRecall(Sensitivity) = %.3f" +
							"\nSpecificity = %.3f\nF-score = %.3f",getTP(), getFP(), getFN(), getTN(), getAccuracy(), getPrecision(),getRecall(), getSpecificity(),getFscore());
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
}
