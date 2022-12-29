package com.ibm.example;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.JsonObject;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.v1.Query;
import twitter4j.v1.QueryResult;
import twitter4j.v1.SearchResource;
import twitter4j.v1.Status;

public class RetrieveTweets {
	
	// set up threads for NLU and Cloudant Processing
	private static final ExecutorService nluPool = Executors.newFixedThreadPool(3);
	private static int count = 0;

	public static JsonObject main(JsonObject args) {
		JsonObject out = new JsonObject();
		
		JsonObject twitterConfig = args.getAsJsonObject("TWITTER");
		
		String CONSUMER_KEY_STR = twitterConfig.getAsJsonPrimitive("CONSUMER_KEY_STR").getAsString();
		String CONSUMER_SECRET_STR = twitterConfig.getAsJsonPrimitive("CONSUMER_SECRET_STR").getAsString();
		String ACCESS_TOKEN_STR = twitterConfig.getAsJsonPrimitive("ACCESS_TOKEN_STR").getAsString();
		String ACCESS_TOKEN_SECRET_STR = twitterConfig.getAsJsonPrimitive("ACCESS_TOKEN_SECRET_STR").getAsString();
		String PHRASE = twitterConfig.getAsJsonPrimitive("PHRASE").getAsString();
		String LANGUAGE = twitterConfig.getAsJsonPrimitive("LANGUAGE").getAsString();
		
    	Twitter twitter = Twitter.newBuilder().oAuthConsumer(CONSUMER_KEY_STR, CONSUMER_SECRET_STR)
    			.oAuthAccessToken(ACCESS_TOKEN_STR, ACCESS_TOKEN_SECRET_STR).build();
		
		SearchResource search = twitter.v1().search();

		try {
			
			// limit query to a certain language and per user
            Query query = Query.of(PHRASE + "+exclude:retweets").lang(LANGUAGE).count(1);
            QueryResult result;
            LocalDateTime past = LocalDateTime.now().minusMinutes(5);
            
            do {
                result = search.search(query);
                List<Status> tweets = result.getTweets();
                for (Status tweet : tweets) {
                	count++;
					if (tweet.getCreatedAt().isAfter(past)) {
						nluPool.execute(new NLUProcessing(tweet.getText(), args));
					}
                }
            } while ((query = result.nextQuery()) != null && count < 100);
            
            System.out.println("count : " + count);
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to search tweets: " + te.getMessage());
            System.out.println("count : " + count);
        }
		
        nluPool.shutdown();
		out.addProperty("body","count : " + count);
		return out;
	}
}
