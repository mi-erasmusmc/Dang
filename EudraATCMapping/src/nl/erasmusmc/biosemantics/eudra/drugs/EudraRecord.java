package nl.erasmusmc.biosemantics.eudra.drugs;

/**
 * @author haidangvo
 *
 */
public class EudraRecord {
	private String substance;
	private String[] atcs;
	
	
	public EudraRecord(){
		this.substance = null;	
		this.atcs = null;
	}

	
	public EudraRecord(String line){
		 
		String[] pieces = line.split(",");
		
		if (pieces.length >= 2){
			setSubstance(pieces[0]);
			setAtcs(pieces[1].split(","));
		}		
	}
	
	public EudraRecord(String substance, String[] atcs){
		this.substance = substance;
		this.atcs = atcs;
	}

	public String  atcToString(){

		return  String.join(",", atcs);

	}

	public String getSubstance() {
		return substance;
	}


	public void setSubstance(String substance) {
		this.substance = substance;
	}


	public String[] getAtcs() {
		return atcs;
	}


	public void setAtcs(String[] assigned_atc) {
		this.atcs = assigned_atc;
	}
	
	
	
	
}
