package org.ametiste.laplatform.protocol.gateway;

import org.ametiste.laplatform.protocol.Protocol;
import org.ametiste.laplatform.protocol.ProtocolFactory;
import org.ametiste.laplatform.protocol.ProtocolGateway;

import java.util.Map;

/**
 *
 * @since
 */
public class ProtocolGatewayService {

    private final Map<Class<? extends Protocol>, ProtocolFactory<?>> protocolFactories;

    public ProtocolGatewayService(Map<Class<? extends Protocol>, ProtocolFactory<?>> protocolFactories) {
        this.protocolFactories = protocolFactories;
    }

    public ProtocolGateway createGateway(String clientId) {
        // NOTE: PoC implementation, just remap factories
        // in real implementation unique map of protocols will be created for each gateway client call
        // I need to design a ProtocolFamily abstraction for it, gateway will receive family instead of map
        return new DirectProtocolGateway(
            protocolFactories, new DirectGatewayContext(clientId)
        );
    }

}
