package com.ibm.example;

import java.util.Collections;
import java.util.Map;

import com.google.gson.JsonObject;
import com.ibm.cloud.cloudant.v1.Cloudant;
import com.ibm.cloud.cloudant.v1.model.FindResult;
import com.ibm.cloud.cloudant.v1.model.PostFindOptions;
import com.ibm.cloud.sdk.core.http.HttpConfigOptions;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;

public class MetricsData {
	public static JsonObject main(JsonObject args) {
		JsonObject result = new JsonObject();
		
		JsonObject cloudantConfig = args.getAsJsonObject("CLOUDANT");
		String IAMAPIKEY = cloudantConfig.getAsJsonPrimitive("IAMAPIKEY").getAsString();
		String URL = cloudantConfig.getAsJsonPrimitive("URL").getAsString();
		String SERVICENAME = cloudantConfig.getAsJsonPrimitive("SERVICENAME").getAsString();
		String DBNAME = cloudantConfig.getAsJsonPrimitive("DBNAME").getAsString();

		IamAuthenticator authenticator = new IamAuthenticator.Builder().apikey(IAMAPIKEY).build();

		Cloudant service = new Cloudant(SERVICENAME, authenticator);
		service.setServiceUrl(URL);
		
		HttpConfigOptions options =  new HttpConfigOptions.Builder().build();
		service.configureClient(options);
		
		Map<String, Object> selector = Collections.singletonMap("sentiment","positive");
		PostFindOptions pfo = new PostFindOptions.Builder().db(DBNAME).selector(selector).build();
		int positive = service.postFind(pfo).execute().getResult().getDocs().size();
		
		selector = Collections.singletonMap("sentiment","negative");
		pfo = new PostFindOptions.Builder().db(DBNAME).selector(selector).build();
		int negative = service.postFind(pfo).execute().getResult().getDocs().size();

		selector = Collections.singletonMap("sentiment","neutral");
		pfo = new PostFindOptions.Builder().db(DBNAME).selector(selector).build();
		int neutral = service.postFind(pfo).execute().getResult().getDocs().size();
		
		int total = positive + negative + neutral;
		
		double positivePercent = (Double.valueOf(positive) / Double.valueOf(total)) * 100;
		double negativePercent = (Double.valueOf(negative) / Double.valueOf(total)) * 100;
		double neutralPercent =  (Double.valueOf(neutral)  / Double.valueOf(total)) * 100;

		String s = "# TYPE total_tweets counter\n" +
				"total_tweets\t" + total + "\n" +
				"# TYPE sentiment counter\n" +
				"sentiment_positive\t" + positive + "\n" +
				"sentiment_negative\t" + negative + "\n" +
				"sentiment_neutral\t" + neutral + "\n"  +
				"# TYPE sentiment_percent gauge\n" +
				"sentiment_percent_positive\t" + positivePercent + "\n" +
				"sentiment_percent_negative\t" + negativePercent + "\n" +
				"sentiment_percent_neutral\t" + neutralPercent + "\n";
		
		result.addProperty("body", s);
		return result;
	}
}
