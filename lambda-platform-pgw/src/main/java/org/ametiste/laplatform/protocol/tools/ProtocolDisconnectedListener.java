package org.ametiste.laplatform.protocol.tools;

import org.ametiste.laplatform.sdk.protocol.Protocol;

/**
 *
 * @since
 */
public interface ProtocolDisconnectedListener {

    void onProtocolDisconnected(final Class<? extends Protocol> protocolType,
                                final Protocol protocol,
                                final String group,
                                final String name);

}
