package org.jboss.as.quickstarts.logging;

import java.util.Map;

public interface FeatureService {
	boolean getToggle(String featureKey, String userEmail);
	boolean getToggle(String featureKey, String userEmail, Map<String, Object> extra);
}
