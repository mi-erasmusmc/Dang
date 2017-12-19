package nl.erasmusmc.biosemantics.eudra.drugs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nl.erasmusmc.biosemantics.eudra.Utils.Utils;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
//import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.ContentStreamBase;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.client.solrj.SolrRequest;

public class DrugServer {
 
	private static HttpSolrClient drugServer;
	private static String server = null;
	private static String collection = null;
	private static String requestHandller = "/drug_tag";
	//private static String suppressionList = "data/suppression.csv";


	//private  static List<String> suppression;
	
	public DrugServer( String server, String acollection){
		this.server = server;
		this.collection = acollection;
		//this.suppression = new ArrayList<>();
	}
	
	
	 
	public static HttpSolrClient getDrugServer() {
		return drugServer;
	}


	public static void setDrugServer(HttpSolrClient ingServer) {
		DrugServer.drugServer = ingServer;
	}


	public static String getServer() {
		return server;
	}


	public static void setServer(String server) {
		DrugServer.server = server;
	}

		
	public List<DrugCandidate> getTaggerConcepts(String text, boolean substanceOnly, String mode) {
		List<DrugCandidate> result = new ArrayList<DrugCandidate>();

		//this.suppression = Utils.loadSuppressionList(suppressionList);

		if (!text.isEmpty()) {

			try {

				if (drugServer == null) {
					drugServer = new HttpSolrClient(server + "/" + collection,
					HttpClientBuilder.create().setUserAgent("MyAgent").setMaxConnPerRoute(4).build());
				}

				// default mode
				if (mode == null){
					mode = "LONGEST_DOMINANT_RIGHT";
				}

				ModifiableSolrParams params = new ModifiableSolrParams();
				//params.add("overlaps", "ALL"); 
				//params.add("overlaps", "NO_SUB");
				params.add("overlaps", mode);
				// params.set("q", "substance:\"" + clean + "\""); // set query
				params.set("fl", "uuid, cui, drug, tty, drug_norm, ingredient, ingredient_norm, ing_cui, code, rela ,sab, score"); // set field list																						
				params.set("rows", 10000);
				params.set("matchText", "true");
				// params.set("qt", requestHandller); // set requesthandler				

				ContentStreamUpdateRequest req = new ContentStreamUpdateRequest("");
				req.addContentStream(new ContentStreamBase.StringStream(text));
				req.setMethod(SolrRequest.METHOD.POST);
				req.setPath(requestHandller);
				req.setParams(params);

				SolrDocumentList documentList;

				try {

					NamedList<Object> solrResponse = drugServer.request(req);

					documentList = (SolrDocumentList) solrResponse.get("response");
 
					String uuid, cui, drug, tty, drugNorm, ingredient, ingredientNorm, ingCui, code, rela, sab;
					for (SolrDocument document : documentList) {

						drug = document.getFieldValue("drug").toString();

						// skip drug in suppression list
						/*if (suppression.contains(drug.toLowerCase()) &&  !drug.equalsIgnoreCase(text)){
							continue;
						}*/

						uuid = document.getFieldValue("uuid").toString();

						if (document.getFieldValue("cui") != null){
							cui = document.getFieldValue("cui").toString();
						}else{
							cui = "";
						}

						if (document.getFieldValue("tty") != null){
							tty = document.getFieldValue("tty").toString();
						}else{
							tty = "";
						}


						drugNorm =  document.getFieldValue("drug_norm").toString();
						
						
						/*if ( document.getFieldValue("ingredient") != null){
							ingredient = document.getFieldValue("ingredient").toString();
						}else{
							ingredient = "";
						}
						
						if ( document.getFieldValue("ingredient_norm") != null){
							ingredientNorm = document.getFieldValue("ingredient_norm").toString();
						}else{
							ingredientNorm = "";
						}*/
						
						ingredient = document.getFieldValue("ingredient").toString();
						ingredientNorm = document.getFieldValue("ingredient_norm").toString();

						if ( document.getFieldValue("ing_cui") != null){
							ingCui = document.getFieldValue("ing_cui").toString();
						}else{
							ingCui = "";
						}


						code = document.getFieldValue("code").toString();
						if (document.getFieldValue("rela") != null){
							rela = document.getFieldValue("rela").toString();
						}else{
							rela = "";
						}
						
						sab = document.getFieldValue("sab").toString();
						
						DrugCandidate drugCand = new DrugCandidate(uuid, cui, drug, drugNorm, ingredient, ingredientNorm, ingCui, code, rela, sab, tty);

						// rela is null means that match exact substance in the dictionary, it does not look up for ingredients
						if (substanceOnly){
							if (rela == ""){
								result.add(drugCand);
							}
						}else{
							result.add(drugCand);
						}


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

	public List<DrugCandidate> getConcepts(String strQuery){

		List<DrugCandidate> result = new ArrayList<DrugCandidate>();

		if (!strQuery.isEmpty()){

			try{

				if (drugServer == null){
					drugServer = new HttpSolrClient( server + "/" + collection, HttpClientBuilder.create().setUserAgent("MyAgent").setMaxConnPerRoute(4).build() );
				}

				//String clean = ConceptUtils.normalizeConcept(substance);

				SolrQuery query = new SolrQuery();

				query.setQuery( strQuery);

				//query.setQuery("drug:\"" + clean + "\"");
				query.setRows(Integer.MAX_VALUE);
				query.setFields("cui, drug, drug_norm, ingredient, ingredient_norm, ing_cui, code, rela,sab, tty, score");

				QueryResponse response = new QueryResponse();
				try {
					response = drugServer.query(query);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				SolrDocumentList documentList;

				if (response.getStatus() ==0 ){
					documentList = response.getResults();

					Float maxScore = documentList.getMaxScore();
					Long frequency = documentList.getNumFound();

					//System.out.println("Number of found: " + documentList.getNumFound());

					//ArrayList values = new ArrayList();
					for ( SolrDocument document : documentList ){
						DrugCandidate drug = new DrugCandidate();

						if (document.getFieldValue("cui") != null){
							drug.setCui(document.getFieldValue("cui").toString());
						}else{
							drug.setCui("");
						}

						drug.setDrugName(document.getFieldValue("drug").toString());
						
						drug.setDrugNorm((document.getFieldValue("drug_norm").toString()));
						
						/*if (document.getFieldValue("ingredient") != null){
							drug.setIngredient(document.getFieldValue("ingredient").toString());
						}else{
							drug.setIngredient("");
						}*/
						
						drug.setIngredient(document.getFieldValue("ingredient").toString());
						drug.setIngredientNorm((document.getFieldValue("ingredient_norm").toString()));

						if (document.getFieldValue("ing_cui") != null){
							drug.setIngCui(document.getFieldValue("ing_cui").toString());
						}else{
							drug.setIngCui("");
						}

						drug.setAtc(document.getFieldValue("code").toString());

						if (document.getFieldValue("rela") != null){
							drug.setRela(document.getFieldValue("rela").toString());
						}else{
							drug.setRela("");
						}

						if (document.getFieldValue("tty") != null){
							drug.setTty(document.getFieldValue("tty").toString());
						}else{
							drug.setTty("");
						}


						drug.setSab(document.getFieldValue("sab").toString());

						Float score = Float.parseFloat( document.getFieldValue("score").toString() ) / maxScore;
						drug.setScore( score );
						drug.setFrequency( frequency );


						result.add( drug );
					}
				}


			}catch(SolrServerException e){
				e.printStackTrace();
			}
		}

		return result;

	}

	public List<DrugCandidate> getConcepts(String substance, String field){

		String query;

		if (field == null){
			query = "drug:\"" + substance + "\"";
		}else{
			query = field  + ":\"" + substance + "\"";
		}

		return getConcepts(query);

	}
 




}
