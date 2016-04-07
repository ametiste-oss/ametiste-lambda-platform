package org.ametiste.laplatform.sdk.protocol;

import java.util.Map;

/**
 *
 * @since
 */
public interface GatewayContext {

    /**
     * @deprecated use {@link #lookupString(String)} instead.
     */
    @Deprecated
    String lookupAttribute(String name);

    String lookupString(String name);

    Map<String, String> lookupMap(String name);

}
