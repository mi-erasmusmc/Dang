/**
 * 
 */
package nl.erasmusmc.biosemantics.eudra.drugs;

/**
 * @author haidangvo
 *
 */
public class Drug extends Concept{
 
	private String drugNorm;
	private String ingredient;
	private String ingredientNorm;
	private String ingCui;
	private String rela;

	
	public Drug(){
		super();	 
		drugNorm = null;
		ingredient = null;
		ingredientNorm = null;
		ingCui = null;
		rela = null;
	}
	 
	
	/**
	 *
	 * @param cui
	 * @param drug
	 * @param code
	 */
	public Drug(String cui, String drug, String code){
		super(cui,drug,code,null,null);		 
		this.drugNorm = null;
		this.ingredient = null;
		this.ingredientNorm = null;
		this.ingCui = null;
		this.rela = null;
	}

	/**
	 *
	 * @param cui
	 * @param drug
	 * @param ingredient
	 * @param ingCui
	 * @param code
	 * @param rela
	 * @param sab
	 * @param tty
	 */
	public Drug(String cui, String drug, String ingredient,  String ingCui, String code, String rela, String sab, String tty){
		super(cui,drug,code,sab,tty);
		this.rela = rela;
		this.drugNorm = null;
		this.ingredient = ingredient;
		this.ingredientNorm = null;
		this.ingCui = ingCui;
	}
	
	
	/**
	 * 
	 * @param cui
	 * @param drug
	 * @param drugNorm
	 * @param ingredient
	 * @param ingredientNorm
	 * @param ingCui
	 * @param code
	 * @param rela
	 * @param sab
	 * @param tty
	 */
	public Drug(String cui, String drug, String drugNorm, String ingredient, String ingredientNorm, String ingCui, String code, String rela, String sab, String tty){
		super(cui,drug,code,sab,tty);
		this.rela = rela;
		this.drugNorm = drugNorm;
		this.ingredient = ingredient;
		this.ingredientNorm = ingredientNorm;
		this.ingCui = ingCui;
	}
	
 
	public void setDrugName(String drugname){
		super.setTerm(drugname);
	}
	
	public String getDrugName(){
		return super.getTerm();
	}
	
	 
	public void setAtc(String atc){
		super.setCode(atc);
	}
	
	public String getAtc(){
		return super.getCode();
	}
	
	public String getDrugNorm() {
		return drugNorm;
	}

	public void setDrugNorm(String drugNorm) {
		this.drugNorm = drugNorm;
	}

	public String getIngredient() {
		return ingredient;
	}

	public void setIngredient(String ingredient) {
		this.ingredient = ingredient;
	}

	public String getIngredientNorm() {
		return ingredientNorm;
	}

	public void setIngredientNorm(String ingredientNorm) {
		this.ingredientNorm = ingredientNorm;
	}

	public String getIngCui() {
		return ingCui;
	}

	public void setIngCui(String ingCui) {
		this.ingCui = ingCui;
	}
	
	public String getRela(){
		return  this.rela;
	}

	public void setRela(String rela){
		this.rela = rela;
	}
	
	
}
