package com.salience;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterManager {

	private static Twitter twitter = null;

	public static Twitter getTwitterInstance() throws TwitterException {
		if (twitter == null) {
			final ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setDebugEnabled(true);
			cb.setApplicationOnlyAuthEnabled(true);

			final OAuth2Token token = getOAuth2Token();
			cb.setOAuth2TokenType(token.getTokenType());
			cb.setOAuth2AccessToken(token.getAccessToken());

			final TwitterFactory tf = new TwitterFactory(cb.build());
			twitter = tf.getInstance();
		}

		return twitter;
	}

	private static OAuth2Token getOAuth2Token() throws TwitterException {
		OAuth2Token token = null;
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setApplicationOnlyAuthEnabled(true);
		token = new TwitterFactory(cb.build()).getInstance().getOAuth2Token();
		return token;
	}

}
