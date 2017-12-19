/**
 * 
 */
package nl.erasmusmc.biosemantics.eudra.drugs;

/**
 * @author haidangvo
 *
 */
public class Concept {
	private String cui;
	private String term;
	private String code;
	private String sab;
	private String tty;
	
	public Concept(){
		cui = null;
		term = null;
		code = null;
		sab = null;
		tty = null;
	}
	
	/**
	 * @param cui
	 * @param term
	 */
	public Concept(String cui, String term){
		this.cui = cui;
		this.term = term;
		this.code = null;
		this.sab = null;
		this.tty = null;		
	}
	
	/**
	 * @param cui
	 * @param term
	 * @param code
	 */
	/**
	 * @param cui
	 * @param term
	 * @param code
	 */
	public Concept(String cui, String term, String code){
		this.cui = cui;
		this.term = term;
		this.code = code;
		this.sab = null;
		this.tty = null;		
	}
	
	/**
	 * @param cui
	 * @param term
	 * @param code
	 * @param sab
	 */
	public Concept(String cui, String term, String code, String sab){
		this.cui = cui;
		this.term = term;
		this.code = code;
		this.sab = sab;
		this.tty = null;		
	}
	
	
	/**
	 * @param cui
	 * @param term
	 * @param code
	 * @param sab
	 * @param tty
	 */
	public Concept(String cui, String term, String code, String sab, String tty){
		this.cui = cui;
		this.term = term;
		this.code = code;
		this.sab = sab;
		this.tty = tty;		
	}

	public String getCui() {
		return cui;
	}

	public void setCui(String cui) {
		this.cui = cui;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getSab() {
		return sab;
	}

	public void setSab(String sab) {
		this.sab = sab;
	}

	public String getTty() {
		return tty;
	}

	public void setTty(String tty) {
		this.tty = tty;
	}
	
	
}
