package org.ametiste.laplatform.protocol.tools;

import org.ametiste.laplatform.sdk.protocol.Protocol;

/**
 *
 * @since
 */
public interface ProtocolConnectionListener {

    void onProtocolConnection(final Class<? extends Protocol> protocolType,
                              final Protocol protocol,
                              final String name,
                              final String group);

}
