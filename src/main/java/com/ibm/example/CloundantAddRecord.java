package com.ibm.example;

import java.time.LocalDateTime;

import com.google.gson.JsonObject;
import com.ibm.cloud.cloudant.v1.Cloudant;
import com.ibm.cloud.cloudant.v1.model.Document;
import com.ibm.cloud.cloudant.v1.model.PostDocumentOptions;
import com.ibm.cloud.sdk.core.http.HttpConfigOptions;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;

public class CloundantAddRecord implements Runnable {

	private JsonObject args;

	private String tweet;
	private String sentiment;
	
	public CloundantAddRecord (String tweet, String sentiment, JsonObject args) {
		this.tweet = tweet;
		this.sentiment = sentiment;
		this.args = args;
	}
	
	@Override
	public void run() {
		JsonObject cloudantConfig = args.getAsJsonObject("CLOUDANT");
		JsonObject twitterConfig = args.getAsJsonObject("TWITTER");
		String phrase = twitterConfig.getAsJsonPrimitive("PHRASE").getAsString();
		
		String IAMAPIKEY = cloudantConfig.getAsJsonPrimitive("IAMAPIKEY").getAsString();
		String URL = cloudantConfig.getAsJsonPrimitive("URL").getAsString();
		String SERVICENAME = cloudantConfig.getAsJsonPrimitive("SERVICENAME").getAsString();
		String DBNAME = cloudantConfig.getAsJsonPrimitive("DBNAME").getAsString();

		IamAuthenticator authenticator = new IamAuthenticator.Builder().apikey(IAMAPIKEY).build();

		Cloudant service = new Cloudant(SERVICENAME, authenticator);
		service.setServiceUrl(URL);
		
		HttpConfigOptions options =  new HttpConfigOptions.Builder().build();
		service.configureClient(options);

		Document document = new Document();
		document.put("tweet", tweet);
		document.put("sentiment", sentiment);
		document.put("phrase", phrase);
		document.put("date", LocalDateTime.now());
	
		PostDocumentOptions docOptions = new PostDocumentOptions.Builder().db(DBNAME).document(document).build();
		service.postDocument(docOptions).execute().getResult();
	}

}
