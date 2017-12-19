/**
 * 
 */
package nl.erasmusmc.biosemantics.eudra.drugs;

/**
 * @author dangvh
 *
 */
public interface SubstanceInterface {
	
	public void setCui(String cui);
	public String getCui();

	public String getAtc();	
	public void setAtc(String atc);
	
	public String getSubstanceName();	
	public void setSubstanceName(String substance);
	
	public String getNormalizeSubstanceName();
	
}
