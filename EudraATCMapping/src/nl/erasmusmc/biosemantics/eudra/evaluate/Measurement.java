/**
 * 
 */
package nl.erasmusmc.biosemantics.eudra.evaluate;

/**
 * @author dangvh
 *
 */
public enum Measurement {
	TP{
		@Override
		public String toString(){
			return "TP";
		}
	},
	FP{
		@Override
		public String toString(){
			return "FP";
		}
	},	
	TN{
		@Override
		public String toString(){
			return "TN";
		}
	},
	FN{
		@Override
		public String toString(){
			return "FN";
		}
	}
	
}
