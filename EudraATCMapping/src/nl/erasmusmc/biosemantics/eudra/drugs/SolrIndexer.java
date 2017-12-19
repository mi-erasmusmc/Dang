package nl.erasmusmc.biosemantics.eudra.drugs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.ContentStreamBase;
import org.apache.solr.common.util.NamedList;

public class SolrIndexer {
	
	private String url;
	private HttpSolrClient client;
	
	public SolrIndexer(String url){
		this.url = url;
	}
	
	public void connectSolrServer(String collection){	
		
		client = new HttpSolrClient(url + "/" + collection,
				HttpClientBuilder.create().setUserAgent("MyAgent").setMaxConnPerRoute(4).build());
	}
	
	public void closeSolrServer(){
		
		try {
			
			client.close();
			
		} catch (IOException e) {			
			e.printStackTrace();
		}finally{
			client = null;
		}
				
	}
	
	 
	public List<DrugCandidate> getDrugIngredientConcept(String collection, String requestHandler, String text){
		List<DrugCandidate> result = new ArrayList<DrugCandidate>();
		
		if (client == null){
			this.connectSolrServer(collection);
		}
		
		
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.add("overlaps", "LONGEST_DOMINANT_RIGHT");
		// params.set("q", "substance:\"" + clean + "\""); // set query
		params.set("fl", "uuid, cui, drug, drug_norm,ingredient, ingredient_norm, ing_cui, code, rela,sab, score"); // set field list 
		params.set("rows", 100);
		// params.set("qt", requestHandller); 
		// set requesthandler
		// /requesthandler

		ContentStreamUpdateRequest req = new ContentStreamUpdateRequest("");
		req.addContentStream(new ContentStreamBase.StringStream(text));
		req.setMethod(SolrRequest.METHOD.POST);
		req.setPath(requestHandler);
		req.setParams(params);

		SolrDocumentList documentList;

		try {

			NamedList<Object> solrResponse = client.request(req);

			documentList = (SolrDocumentList) solrResponse.get("response");

			Float maxScore = documentList.getMaxScore();
			Long frequency = documentList.getNumFound();
			String uuid, cui, drug, ingredient, ing_cui, code, rela, sab, tty;
			for (SolrDocument document : documentList) {
											 
				uuid = document.getFieldValue("uuid").toString();
				cui = document.getFieldValue("cui").toString();			
				drug = document.getFieldValue("drug").toString();
				ingredient = document.getFieldValue("ingredient").toString();				
				ing_cui = document.getFieldValue("ing_cui").toString();
				code = document.getFieldValue("code").toString();
				rela = document.getFieldValue("rela").toString();
				sab = document.getFieldValue("sab").toString();
				tty = document.getFieldValue("tty").toString();

				Float score = null; // Float.parseFloat(document.getFieldValue("score").toString()) / maxScore;
				
				DrugCandidate candicate = new DrugCandidate(uuid,cui, drug, ingredient, ing_cui, code, rela,sab,tty);
				
				//candicate(cui, drug, ingredient, ing_cui, code, rela,sab);
				
				candicate.setScore(score);
				candicate.setFrequency(frequency);

				result.add(candicate);
			}

		} catch (IOException e) {
			 e.printStackTrace();
		}catch(SolrServerException e){
			e.printStackTrace();
		}
		
			
		return result;
	}
	
}
