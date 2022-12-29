package com.ibm.example;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.JsonObject;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.natural_language_understanding.v1.model.Features;
import com.ibm.watson.natural_language_understanding.v1.model.SentimentOptions;

public class NLUProcessing implements Runnable {
	private static final ExecutorService pool = Executors.newFixedThreadPool(3);

	private String tweet;
	private JsonObject args;
	
	public NLUProcessing(String tweet, JsonObject args) {
		this.tweet = tweet;
		this.args = args;
	}
	
	@Override
	public void run() {
		JsonObject nluConfig = args.getAsJsonObject("NLU");
		String IAMAPIKEY = nluConfig.getAsJsonPrimitive("IAMAPIKEY").getAsString();
		String URL = nluConfig.getAsJsonPrimitive("URL").getAsString();

    	IamAuthenticator authenticator = new IamAuthenticator.Builder().apikey(IAMAPIKEY).build();
    	NaturalLanguageUnderstanding naturalLanguageUnderstanding = new NaturalLanguageUnderstanding("2022-04-07", authenticator); 
    	naturalLanguageUnderstanding.setServiceUrl(URL);
    	
    	SentimentOptions options = new SentimentOptions.Builder().build();
    	Features features = new Features.Builder().sentiment(options).build();
    	AnalyzeOptions parameters = new AnalyzeOptions.Builder().text(tweet).features(features).build();
    	AnalysisResults response = naturalLanguageUnderstanding.analyze(parameters).execute().getResult();
    	String sentiment = response.getSentiment().getDocument().getLabel();
    	System.out.println(sentiment);
    	pool.execute(new CloundantAddRecord(tweet, sentiment, args));
	}
}
