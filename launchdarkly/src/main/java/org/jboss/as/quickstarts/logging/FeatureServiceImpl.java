package org.jboss.as.quickstarts.logging;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.launchdarkly.client.LDClient;
import com.launchdarkly.client.LDConfig;
import com.launchdarkly.client.LDUser;

public class FeatureServiceImpl implements FeatureService {
	private static final Logger logger = LoggerFactory.getLogger(FeatureServiceImpl.class);
	private final LDClient ldClient;

	FeatureServiceImpl() {
		String launchDarklyKey = "YOUR-SDK-KEY";

		LDConfig config = new LDConfig.Builder().stream(false).startWaitMillis(20000).build();
//		LDConfig config = new LDConfig.Builder().startWaitMillis(10000).build();
		ldClient = new LDClient(launchDarklyKey, config);

	}

	@Override
	public boolean getToggle(String featureKey, String userEmail) {
		return getToggle(featureKey, userEmail, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean getToggle(String featureKey, String userEmail, Map<String, Object> extra) {
		boolean featureToggle = false;
		LDUser.Builder builder = new LDUser.Builder(userEmail);
		if (StringUtils.isNotBlank(userEmail)) {
			builder.email(userEmail);
		}

//		UserRequestVO userRequest = OauthService.getUserRequest();
//		if (userRequest != null) {
//			builder.anonymous(userRequest.isAnonymousToken());
//			String clientIP = userRequest.getClientIP();
//			if (StringUtils.isNotBlank(clientIP)) {
//				builder.ip(clientIP);
//			}
//		}

		if (extra != null) {
			for (String key : extra.keySet()) {
				Object value = extra.get(key);
				if (value != null) {
					if (value instanceof Boolean) {
						builder.custom(key, (Boolean) value);
					} else if (value instanceof Number) {
						builder.custom(key, (Number) value);
					} else if (value instanceof String) {
						builder.custom(key, (String) value);
					} else if (value instanceof List<?>) {
						try {
							builder.custom(key, (List<String>) value);
						} catch (ClassCastException cce) {
							logger.warn("value for key: " + key + " is not instanceof List<String> and will not be used: " + value, cce);
						}
					} else {
						logger.warn("value for key: " + key + " is supported and will not be used: " + value);
					}
				}
			}
		}

		LDUser user = builder.build();
		featureToggle = ldClient.boolVariation(featureKey, user, false);
		if (logger.isDebugEnabled()) {
			logger.info("featureKey: " + featureKey + " user: " + user + " featureToggle: " + featureToggle);
		}

		return featureToggle;
	}

	@Override
	public void finalize() {
		try {
			if (ldClient != null)
				ldClient.close();
		} catch (Exception ex) {
			logger.warn("Error while closing launchdarkly client", ex);
		}
	}
}
