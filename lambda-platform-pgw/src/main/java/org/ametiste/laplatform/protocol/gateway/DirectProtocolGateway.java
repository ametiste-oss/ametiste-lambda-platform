package org.ametiste.laplatform.protocol.gateway;

import org.ametiste.laplatform.protocol.GatewayContext;
import org.ametiste.laplatform.protocol.Protocol;
import org.ametiste.laplatform.protocol.ProtocolFactory;
import org.ametiste.laplatform.protocol.ProtocolGateway;

import java.util.Map;

/**
 *
 * @since
 */
public class DirectProtocolGateway implements ProtocolGateway {

    private final Map<Class<? extends Protocol>, ProtocolFactory<?>> protocols;

    private final GatewayContext gc;

    public DirectProtocolGateway(Map<Class<? extends Protocol>, ProtocolFactory<?>> protocols, GatewayContext gc) {
        this.gc = gc;
        this.protocols = protocols;
    }

    @Override
    public <T extends Protocol> T session(Class<T> protocolType) {

        if (!protocols.containsKey(protocolType)) {
             throw new RuntimeException("Gateway has no access to the requested protocol: " + protocolType.getName());
        }

        final Protocol protocol = protocols
                .get(protocolType).createProtocol(gc);

        if (!protocolType.isAssignableFrom(protocol.getClass())) {
             throw new IllegalStateException("Gateway has no access to " +
                     "protocol of the given type: " + protocolType.getName());
        }

        try {
            return protocolType.cast(protocol);
        } catch (ClassCastException e) {
            throw new IllegalStateException("Gateway has error during access to " +
                    "protocol of the given type: " + protocolType.getName(), e);
        }

    }

}
