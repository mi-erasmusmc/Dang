package nl.erasmusmc.biosemantics.eudra.drugs;

/**
 * @author dangvh
 *
 */
public class RxNorm extends Concept{
	
	private String rxCui;
	private String ingredient;
	private String ingCui;
	private String rela;
	
	public RxNorm(){
		super();
		this.rxCui = null;		
	}
	
	/**
	 * @param cui
	 * @param term
	 */
	public RxNorm(String cui, String term){
		super(cui, term);
		this.rxCui = null;
	}
	
	/**
	 * @param cui
	 * @param term
	 * @param code
	 */
	public RxNorm(String cui, String term, String code){
		super(cui,term,code);
		this.rxCui = null;
	}
	
	public RxNorm(String cui, String term, String ingredient, String code){
		super(cui, term, code);
		this.rxCui = null;
		this.ingredient = ingredient;
		this.ingCui = null;
				
	}
	
	public RxNorm(String cui, String term, String ingredient, String code, String rela){
		super(cui, term, code);
		this.rxCui = null;
		this.ingredient = ingredient;
		this.ingCui = null;
		this.rela = rela;
				
	}
	
	public RxNorm(String cui, String term, String ingredient, String ingCui, String code, String rela){
		super(cui, term, code);
		this.rxCui = null;
		this.ingredient = ingredient;
		this.ingCui = ingCui;
		this.rela = rela;
				
	}
	
	public RxNorm(String cui, String term, String ingredient, String ingCui, String code, String rela, String sab){
		super(cui, term, code, sab);
		this.rxCui = null;
		this.ingredient = ingredient;
		this.ingCui = ingCui;
		this.rela = rela;
				
	}
	
	public RxNorm(String cui, String term, String ingredient, String ingCui, String code, String rela, String sab, String tty){
		super(cui, term, code, sab, tty);
		this.rxCui = null;
		this.ingredient = ingredient;
		this.ingCui = ingCui;
		this.rela = rela;
				
	}
	 
	
	public String getRela() {
		return rela;
	}

	public void setRela(String rela) {
		this.rela = rela;
	}

	public String getIngredient() {
		return ingredient;
	}

	public void setIngredient(String ingredient) {
		this.ingredient = ingredient;
	}

	public String getIngCui() {
		return ingCui;
	}

	public void setIngCui(String ingCui) {
		this.ingCui = ingCui;
	}

	public String getRxCui() {
		return rxCui;
	}

	public void setRxCui(String rxCui) {
		this.rxCui = rxCui;
	}
	 
	public String getAtc(){
		return super.getCode();
	}
	
	public void setAtc(String atc){
		super.setCode(atc);
	}
	
	public void setDrugName(String drug){
		super.setTerm(drug);
	}
	
	public String getDrugName(){
		return super.getTerm();
	}
}
