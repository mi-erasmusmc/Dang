/**
 * 
 */
package nl.erasmusmc.biosemantics.eudra.drugs;

/**
 * @author dangvh
 *
 */
public class RxNormCandidate extends RxNorm{
	private String uuid;
	
	public RxNormCandidate(){
		super();
		this.uuid = null;				
	}
	
	/**
	 * @param uuid
	 */
	public RxNormCandidate(String uuid){
		super();
		this.uuid = uuid;				
	}
	
	/**
	 * @param uuid
	 * @param cui
	 * @param term
	 */
	public RxNormCandidate(String uuid, String cui, String term){
		super(cui, term);
		this.uuid = uuid;	
	}
	
	
	/**
	 * @param uuid
	 * @param cui
	 * @param term
	 * @param ingredient
	 * @param code
	 */
	public RxNormCandidate(String uuid, String cui, String term, String ingredient, String code){
		super(cui,term, ingredient,code);
		this.uuid = uuid;	
		
	}
	
	
	/**
	 * @param uuid
	 * @param cui
	 * @param term
	 * @param ingredient
	 * @param ingCui
	 * @param code
	 */
	public RxNormCandidate(String uuid, String cui, String term, String ingredient, String ingCui, String code){
		super(cui,term, ingredient, ingCui,code);
		this.uuid = uuid;	
		
	}
	
	/**
	 * @param uuid
	 * @param cui
	 * @param term
	 * @param ingredient
	 * @param ingCui
	 * @param code
	 * @param rela
	 */
	public RxNormCandidate(String uuid, String cui, String term, String ingredient, String ingCui, String code, String rela){
		super(cui,term, ingredient, ingCui,code, rela);
		this.uuid = uuid;	
		
	}
	
	
	/**
	 * @param uuid
	 * @param cui
	 * @param term
	 * @param ingredient
	 * @param ingCui
	 * @param code
	 * @param rela
	 * @param sab
	 */
	public RxNormCandidate(String uuid, String cui, String term, String ingredient, String ingCui, String code, String rela, String sab){
		super(cui,term, ingredient, ingCui,code, rela, sab);
		this.uuid = uuid;	
		
	}
	
	/**
	 * @param uuid
	 * @param cui
	 * @param term
	 * @param ingredient
	 * @param ingCui
	 * @param code
	 * @param rela
	 * @param sab
	 * @param tty
	 */
	public RxNormCandidate(String uuid, String cui, String term, String ingredient, String ingCui, String code, String rela, String sab, String tty){
		super(cui,term, ingredient, ingCui,code, rela, sab, tty);
		this.uuid = uuid;	
		
	}
	
	
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public String toString(){
		return String.format("Term: %s (%s) \"%s\" %s(%s); RxCUI: %s; ATC: %s", getDrugName(), getCui(), getRela(), getIngredient(), getIngCui(), getRxCui(), getAtc());
	}
	 
}
