package nl.erasmusmc.biosemantics.eudra.drugs;

import nl.erasmusmc.biosemantics.eudra.evaluate.Measurement;

public class DrugCandidate extends Drug{

	private String uuid;
	private Float score;
	private Long frequency;
	private Measurement evaluated;

	public DrugCandidate(){
		super();
	}
	
	/**
	 * @param uuid
	 */
	public DrugCandidate(String uuid){
		super();
		this.uuid = uuid;
		this.evaluated = null;
	}
	
	/**
	 * @param uuid
	 * @param cui
	 * @param drug
	 * @param code
	 */
	public DrugCandidate(String uuid, String cui, String drug, String code){
		super(cui,drug,code);
		this.uuid = uuid;
		this.score = null;
		this.frequency = null;
		this.evaluated = null;
	}
	
	/**
	 * @param uuid
	 * @param cui
	 * @param drug
	 * @param ingredient
	 * @param ingCui
	 * @param code
	 * @param rela
	 * @param sab
	 * @param tty
	 */
	public DrugCandidate(String uuid, String cui, String drug, String ingredient,  String ingCui, String code, String rela, String sab, String tty){
		super(cui,drug,ingredient,ingCui,code,rela,sab,tty);
		this.uuid = uuid;
		this.score = null;
		this.frequency = null;
		this.evaluated = null;
	}
	
	/**
	 * @param uuid
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
	public DrugCandidate(String uuid, String cui, String drug, String drugNorm, String ingredient, String ingredientNorm, String ingCui, String code, String rela, String sab, String tty){
		super(cui,drug,drugNorm,ingredient,ingredientNorm,ingCui,code,rela,sab,tty);	 
		this.uuid = uuid;
		this.score = null;
		this.frequency = null;
		this.evaluated = null;
	}

	
	public Measurement getEvaluated() {
		return evaluated;
	}

	public void setEvaluated(Measurement evaluated) {
		this.evaluated = evaluated;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
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
		return String.format("Drug: %s(%s); Normalized: %s; \tIngredient: %s(%s); Normalized: %s; \tATC: %s; \tRela: %s;\tTTY: %s;\tSAB: %s",
				getDrugName(),getCui(), getDrugNorm(), getIngredient(),getIngCui(), getIngredientNorm(), getAtc(), getRela(), getTty(), getSab() );
	}
	
	public String getString(){
		return String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
				getCui(), getDrugName(), getIngCui(), getIngredient(),  getAtc(), getRela(), getTty(), getSab() );
	}
	
	public void print(){
		System.out.print(this.toString());
	}
	
	public void println(){
		System.out.println(this.toString());
	}
}
