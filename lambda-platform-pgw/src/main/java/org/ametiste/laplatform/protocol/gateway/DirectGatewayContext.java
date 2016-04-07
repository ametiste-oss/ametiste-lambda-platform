package org.ametiste.laplatform.protocol.gateway;

import org.ametiste.laplatform.sdk.protocol.GatewayContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @since
 */
public class DirectGatewayContext implements GatewayContext {

    private final Map<String, String> attributes = new HashMap<>();
    private final Map<String, String> contextProperties;

    public DirectGatewayContext(String clientId, Map<String, String> contextProperties) {
        this.contextProperties = Collections.unmodifiableMap(new HashMap<>(contextProperties));
        attributes.put("clientId", clientId);
    }

    @Override
    public String lookupAttribute(String name) {
        return lookupString(name);
    }

    @Override
    public String lookupString(final String name) {
        return attributes.get(name);
    }

    @Override
    public Map<String, String> lookupMap(final String name) {
        return contextProperties;
    }

}
