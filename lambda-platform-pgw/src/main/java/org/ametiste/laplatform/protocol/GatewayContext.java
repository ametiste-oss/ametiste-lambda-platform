package org.ametiste.laplatform.protocol;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

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
