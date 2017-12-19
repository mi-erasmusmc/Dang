package nl.erasmusmc.biosemantics.eudra.drugs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import nl.erasmusmc.biosemantics.eudra.Utils.Utils;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
//import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.ContentStreamBase;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.search.DocSlice;

public class SubstanceServer {

 
	private static HttpSolrClient substanceServer;
	private static String server = null;
	private static String collection = null;
	private static String requestHandller = "/drug_tag";
	//private static String suppressionList = "data/suppression.csv";

	private  static List<String> suppression;

	public SubstanceServer( String server, String acollection){
		this.server = server;
		this.collection = acollection;
		//this.suppression = new ArrayList<>();
	}
	
	
	 
	public static HttpSolrClient getSubstanceServer() {
		return substanceServer;
	}


	public static void setSubstanceServer(HttpSolrClient drugsServer) {
		SubstanceServer.substanceServer = drugsServer;
	}


	public static String getServer() {
		return server;
	}


	public static void setServer(String server) {
		SubstanceServer.server = server;
	}




	public List<DrugCandidate> getTaggerConcepts(String text) {
		List<DrugCandidate> result = new ArrayList<DrugCandidate>();

		//this.suppression = Utils.loadSuppressionList(suppressionList);

		if (!text.isEmpty()) {

			try {

				if (substanceServer == null) {
					substanceServer = new HttpSolrClient(server + "/" + collection, HttpClientBuilder.create().setUserAgent("MyAgent").setMaxConnPerRoute(4).build());
				}

				ModifiableSolrParams params = new ModifiableSolrParams();
				//params.add("overlaps", "ALL");
				params.add("overlaps", "NO_SUB");
				//params.add("overlaps", "LONGEST_DOMINANT_RIGHT");
				params.set("rows", 1000);
				// params.set("q", "substance:\"" + clean + "\""); // set query
				params.set("fl", "uuid, cui, drug, tty, drug_norm, ingredient, ingredient_norm, ing_cui, code, rela ,sab, score"); // set field list

				params.set("matchText", "true");
				// params.set("qt", requestHandller); // set requesthandler

				ContentStreamUpdateRequest req = new ContentStreamUpdateRequest("");
				req.addContentStream(new ContentStreamBase.StringStream(text));
				req.setMethod(SolrRequest.METHOD.POST);
				req.setPath(requestHandller);
				req.setParams(params);

				SolrDocumentList documentList;

				try {

					NamedList<Object> solrResponse = substanceServer.request(req);
					documentList = (SolrDocumentList) solrResponse.get("response");

					String uuid, cui, drug, tty, drugNorm, ingredient, ingredientNorm, ingCui, code, rela, sab;
					for (SolrDocument document : documentList) {

						drug = document.getFieldValue("drug").toString();

						// skip drug in suppression list
						if (suppression.contains(drug.toLowerCase())){
							continue;
						}

						uuid = document.getFieldValue("uuid").toString();
						cui = document.getFieldValue("cui").toString();

						code = document.getFieldValue("code").toString();
						tty = document.getFieldValue("tty").toString();
						drugNorm =  document.getFieldValue("drug_norm").toString();
						ingredient = document.getFieldValue("ingredient").toString();
						ingredientNorm = document.getFieldValue("ingredient_norm").toString();
						ingCui = document.getFieldValue("ing_cui").toString();

						rela = "";

						sab = document.getFieldValue("sab").toString();

						DrugCandidate drugCand = new DrugCandidate(uuid, cui, drug, drugNorm, ingredient, ingredientNorm, ingCui, code, rela, sab, tty);

						result.add(drugCand);
					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} catch (SolrServerException e) {
				e.printStackTrace();
			}
		}

		return result;
	}
	
	/*public List<Substance> getTaggerConcepts(String substance){
		List<Substance> result = new ArrayList<Substance>();
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.add("overlaps", "LONGEST_DOMINANT_RIGHT");
		ContentStreamUpdateRequest req = new ContentStreamUpdateRequest("");
		
		String normalizedSubstance = normalizeSubstance(substance);
		req.addContentStream(new ContentStreamBase.StringStream(normalizedSubstance));
	    req.setMethod(SolrRequest.METHOD.POST);
	    req.setPath(requestHandller);
	    req.setParams(params);
	    
	    SolrQuery query = new SolrQuery();
		 
		query.setQuery("substance:\"" + normalizedSubstance + "\"");
		query.setRows(100);
		query.setFields("cui, substance, code, sab, termtype, score");
		query.setRequestHandler(requestHandller);
		query.setParam("overlaps", "LONGEST_DOMINANT_RIGHT");
		
		QueryResponse response = new QueryResponse();
		try {
			response = substanceServer.query(query);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    
		if (! substance.isEmpty()){
			try{
		    	
		    	if (substanceServer == null){
		    		substanceServer = new HttpSolrClient( server + "/" + collection, HttpClientBuilder.create().setUserAgent("MyAgent").setMaxConnPerRoute(4).build() );
					try {
						
						UpdateResponse  res =  req.process(substanceServer);
						NamedList<Object> docs;
						SolrDocumentList documentList;
						if (res.getStatus()  ==0 ){	
							
							docs = res.getResponse();
							System.out.println(docs.toString()); 
							Float maxScore = null;
							Long frequency = null;
							
							Float maxScore = documentList.getMaxScore();
							Long frequency = documentList.getNumFound();
							//ArrayList values = new ArrayList();
							for ( Object doc : docs ){
								
								SolrDocument document = (SolrDocument) doc;
								
								Substance term = new Substance();
								//values = (ArrayList) document.getFieldValue("cui");
								//term.setCui(values.get(0).toString() );
								
								term.setCui(document.getFieldValue("cui").toString());
								term.setSubstance(document.getFieldValue("substance").toString().replace( "(substance)", "" ).replace( "(product)", "" ).replace( "product", "" ).replace( "preparation", "" ).trim() );
								term.setAtc(document.getFieldValue("code").toString());
								term.setSab(document.getFieldValue("sab").toString());
								term.setPreferred(document.getFieldValue("termtype").toString());
								 
								
								//values = (ArrayList) document.getFieldValue("term");
								//term.setTerm( values.get(0).toString().replace( "(substance)", "" ).replace( "(product)", "" ).replace( "product", "" ).replace( "preparation", "" ).trim() );
								//term.setAtc( (String) document.getFieldValue("ATC").toString() );
								
								values = (ArrayList) document.getFieldValue("code");
								term.setAtc(values.get(0).toString() );
								
								values = (ArrayList) document.getFieldValue("sab");
								term.setSab(values.get(0).toString() );
								
								values = (ArrayList) document.getFieldValue("termtype");
								term.setPreferred(values.get(0).toString() );
								 
								 
								Float score = Float.parseFloat( document.getFieldValue("score").toString() ) / maxScore;
								term.setScore( score );
								term.setFrequency( frequency );						 
								 
								
								result.add( term );
							}
						}
						 
						
					} catch (IOException e) {
						
						System.out.println(e.toString());
					}
				}
		    	
		    }catch(SolrServerException e){
				e.printStackTrace();
			}
		}
	    
	    
		return result;
	}
 */
	public List<DrugCandidate> getConcepts(String substance){
		List<DrugCandidate> result = new ArrayList<>();
		
		if (!substance.isEmpty()){
			
			try{
				
				if (substanceServer == null){
					substanceServer = new HttpSolrClient( server + "/" + collection, HttpClientBuilder.create().setUserAgent("MyAgent").setMaxConnPerRoute(4).build() );
				}
				

				SolrQuery query = new SolrQuery();
				//query.setQuery("drug:\"" + substance + "\" OR drug_norm:\"" + ConceptUtils.normalizeConcept(substance) + "\"");
				query.setQuery("drug:\"" + substance + "\"");
				query.setRows(100);
				query.setFields("uuid, cui, drug, tty, drug_norm, ingredient, ingredient_norm, ing_cui, code, rela ,sab, score");
				
				QueryResponse response = new QueryResponse();
				try {
					response = substanceServer.query(query);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				SolrDocumentList documentList;
				
				if (response.getStatus() ==0 ){					
					documentList = response.getResults();
					
					//Float maxScore = documentList.getMaxScore();
					//Long frequency = documentList.getNumFound();
					//ArrayList values = new ArrayList();

					String uuid, cui, drug, tty, drugNorm, ingredient, ingredientNorm, ingCui, code, rela, sab;

					for ( SolrDocument document : documentList ){


						uuid = document.getFieldValue("uuid").toString();
						cui = document.getFieldValue("cui").toString();
						drug = document.getFieldValue("drug").toString().replace("[Chemical/Ingredient]", "");
						tty = document.getFieldValue("tty").toString();
						drugNorm =  document.getFieldValue("drug_norm").toString();
						ingredient = document.getFieldValue("ingredient").toString();
						ingredientNorm = document.getFieldValue("ingredient_norm").toString();
						ingCui = document.getFieldValue("ing_cui").toString();
						code = document.getFieldValue("code").toString();

						rela = "";

						sab = document.getFieldValue("sab").toString();

						DrugCandidate drugCand = new DrugCandidate(uuid, cui, drug, drugNorm, ingredient, ingredientNorm, ingCui, code, rela, sab, tty);

						result.add(drugCand);

						//Float score = Float.parseFloat( document.getFieldValue("score").toString() ) / maxScore;

					}
				}
				
				
			}catch(SolrServerException e){
				e.printStackTrace();
			}
		}
		return result;
		
	}
}
