package nl.erasmusmc.biosemantics.eudra.solr.index;

import java.io.IOException;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;;

public class ClearDrugBank {
	private static HttpSolrClient server;
	public static void main(String[] args) throws SolrServerException, IOException, ParseException {
		Options options = new Options().addOption( "s", true, "drug server" );
		String url = new PosixParser().parse( options, args).getOptionValue( "s" );
		if ( url == null ){
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( ClearDrugBank.class.getCanonicalName(), options );
		}
		else {
			server = new HttpSolrClient( url, HttpClientBuilder.create().setUserAgent( "MyAgent" ).setMaxConnPerRoute(4).build() );
			System.out.println("Connecting Solr server...");
			server.deleteByQuery( "*:*" );
			server.commit();
			System.out.println("Done!");
			server.close();
		}
	}
	
}
