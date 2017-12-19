package nl.erasmusmc.biosemantics.eudra.drugs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class RxNormSolrServer {
	
	private String url;
	private String collection;
	private String requestHandler;
	private HttpSolrClient client;
	private String dbServer;
	private String database;
	private String user;
	private String password;
	
	public RxNormSolrServer(String url, String dbServer, String database, String user, String password){
		this.url = url;
		this.dbServer = dbServer;
		this.database = database;
		this.user = user;
		this.password = password;
		this.collection = null;
		this.requestHandler = null;
	}
	
	
	
	public String getUrl() {
		return url;
	}



	public void setUrl(String url) {
		this.url = url;
	}



	public String getCollection() {
		return collection;
	}



	public void setCollection(String collection) {
		this.collection = collection;
	}



	public String getRequestHandler() {
		return requestHandler;
	}



	public void setRequestHandler(String requestHandler) {
		this.requestHandler = requestHandler;
	}



	public void connectSolrServer(String collection){	
		this.collection = collection;
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
	
	
	public RxNormCandidate getPotentialCandidate(String collection, String requestHandler, String text){
		
		List<RxNormCandidate> candidates = new ArrayList<RxNormCandidate>();
		
		RxNormServer rxServer = new RxNormServer(dbServer,database, user, password);
		
		candidates = getConcepts(collection, requestHandler, text);
		
		if (candidates.size() == 0){
			return null;
		}
		
		// find RxCUIs
		System.out.println("-->" + text);
		int i =1;
		for(RxNormCandidate r : candidates){
			
			r.setRxCui(rxServer.getRxCui(r.getCui()));	
			//System.out.println( i++ +"." + r.toString());
			
		}
		
		
		RxNormCandidate result = new RxNormCandidate();
		Map<String, Integer> conceptCount = new HashMap<String, Integer>(500000);
		
		for(RxNormCandidate r : candidates){
			if (!conceptCount.containsKey(r.getCui())){				
				 conceptCount.put(r.getCui(), 1);
			}else{
				
				conceptCount.put(r.getCui(), conceptCount.get(r.getCui()) + 1);
				
			}
		}
		
		int max = 0;
		String key = null;
		// find max
		for (Map.Entry<String, Integer> entry : conceptCount.entrySet()) {
			if ( entry.getValue() > max){
				max = entry.getValue();
				key = entry.getKey();
				
			}			
		}
		
		for(RxNormCandidate r : candidates){
			if (r.getCui() == key){
				result = r;
			}
		}
		
		return result;
		
	}
	 
	public List<RxNormCandidate> getConcepts(String collection, String requestHandler, String text){
		List<RxNormCandidate> result = new ArrayList<RxNormCandidate>();
		
		if (client == null || this.collection != collection || this.requestHandler != requestHandler){
			this.connectSolrServer(collection);
		}
				
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.add("overlaps", "LONGEST_DOMINANT_RIGHT");		 
		params.set("fl", "uuid, cui, drug, ingredient, ing_cui, code, rela,sab, tty, score"); // set field list 
		params.set("rows", 100);		 
		ContentStreamUpdateRequest req = new ContentStreamUpdateRequest("");
		req.addContentStream(new ContentStreamBase.StringStream(text));
		req.setMethod(SolrRequest.METHOD.POST);
		req.setPath(requestHandler);
		req.setParams(params);

		SolrDocumentList documentList;

		try {

			NamedList<Object> solrResponse = client.request(req);

			documentList = (SolrDocumentList) solrResponse.get("response");
			
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
				
				if (cui.equalsIgnoreCase("C0024467")){
					continue;
				}
				
				RxNormCandidate candicate = new RxNormCandidate(uuid,cui, drug, ingredient, ing_cui, code, rela,sab,tty);
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
