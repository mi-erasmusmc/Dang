package nl.erasmusmc.biosemantics.eudra.Utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConceptUtils {

	
	public static String normalizeConcept(String str){
		// remove punctuation and symbols  [!"\#$%&'()*+,\-./:;<=>?@\[\\\]^_`{|}~]\
		
		Pattern punctPattern = Pattern.compile("\\p{Punct}");
		//Pattern punctPattern = Pattern.compile("[^\\w\\d\\s(\\d\\%)(\\d\\.)\\~\\!\\@\\#\\$\\^\\]");
		Pattern spacePattern = Pattern.compile("\\s+");
		String normalized = punctPattern.matcher(str.toLowerCase()).replaceAll(" ");
		normalized = spacePattern.matcher(normalized).replaceAll(" ");		
		return normalized;
	}

	public static String normalizeDrugName(String drug){
		 
		drug = normalizeConcept(drug);

		return cleanDrugName(drug);

	}
	
	public static String cleanDrugName(String drug){
		
		String normalized;
		Pattern p1 = Pattern.compile("(\\[\\bChemical\\/Ingredient\\b\\]|\\(\\bproduct\\b\\)|\\(\\bsubstance\\b\\)|\\(\\bmedication\\b\\)|\\[\\bAmbiguous\\b\\]|\\[\\bVA Product\\b\\])");
		Matcher matcher =  p1.matcher(drug);

		normalized = matcher.replaceAll("");
 

		Pattern spacePattern = Pattern.compile("\\s+");
		 
		normalized = spacePattern.matcher(normalized).replaceAll(" ");	

		return  normalized.trim();
		
	}
	
public static String cleanIngredient(String drug){
		
		String normalized;
		Pattern p1 = Pattern.compile("(\\[\\bChemical\\/Ingredient\\b\\]|\\(\\bproduct\\b\\)|\\(\\bsubstance\\b\\)|\\(\\bmedication\\b\\)|\\[\\bAmbiguous\\b\\]|\\[\\bVA Product\\b\\])");
		Matcher matcher =  p1.matcher(drug);

		normalized = matcher.replaceAll("");
 

		Pattern spacePattern = Pattern.compile("\\s+");
		 
		normalized = spacePattern.matcher(normalized).replaceAll(" ");	

		return  normalized.trim();
		
	}

	

	public static String normalizeIngredient(String ingredient){
		
		ingredient = normalizeConcept(ingredient);
		
		return cleanIngredient(ingredient);


	}
	

	
	private static String generateSHA1Key( String text ){
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA1");
			md.update(text.getBytes());
			byte[] mdbytes = md.digest();

			//convert the byte to hex format
			StringBuffer sb = new StringBuffer("");
			for (int i = 0; i < mdbytes.length; i++) {
				sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}

	public static String getUuid(String cui, String drug, String ing_cui, String ingredient, String code, String sab, String rela, String tty){
		return  generateSHA1Key(cui + drug + ing_cui+ ingredient + code + sab + rela + tty);
	}
}
