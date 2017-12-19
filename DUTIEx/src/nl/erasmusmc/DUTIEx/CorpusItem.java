package nl.erasmusmc.DUTIEx;

public class CorpusItem {
	
	private int doc_id;
	private int sentence_id;
	private String txt;
	
	public int getDoc_id() {
		return doc_id;
	}
	public void setDoc_id(int doc_id) {
		this.doc_id = doc_id;
	}
	public int getSentence_id() {
		return sentence_id;
	}
	public void setSentence_id(int sentence_id) {
		this.sentence_id = sentence_id;
	}
	public String getTxt() {
		return txt;
	}
	public void setTxt(String txt) {
		this.txt = txt;
	}
	

}
