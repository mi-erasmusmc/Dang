package nl.erasmusmc.biosemantics.eudra.drugs;

public class JBIRecord {
	
	private String drugname;
	private String rxCui;
	private String comparison;
	private String rxCuiMedEx;
	
	public JBIRecord(String drugname, String rxCui, String comparison, String rxCuiMedEx){
		this.drugname = drugname;
		this.rxCui = rxCui;
		this.comparison = comparison;
		this.rxCuiMedEx = rxCuiMedEx;
	}

		
	public String getRxCuiMedEx() {
		return rxCuiMedEx;
	}


	public void setRxCuiMedEx(String rxCuiMedEx) {
		this.rxCuiMedEx = rxCuiMedEx;
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

	public void setRxCui(String rxCui) {
		this.rxCui = rxCui;
	}

	public String getComparison() {
		return comparison;
	}

	public void setComparison(String comparison) {
		this.comparison = comparison;
	}
	
	@Override
	public String toString(){
		return String.format("%s | %s | %s", getRxCui(), getDrugname(), getComparison());
	}

}
