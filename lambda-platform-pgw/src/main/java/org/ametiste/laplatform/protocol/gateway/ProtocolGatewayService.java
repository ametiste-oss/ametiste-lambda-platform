package org.ametiste.laplatform.protocol.gateway;

import org.ametiste.laplatform.protocol.Protocol;
import org.ametiste.laplatform.protocol.ProtocolFactory;
import org.ametiste.laplatform.protocol.ProtocolGateway;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @since
 */
public class ProtocolGatewayService {

    public static final class Entry {

        final Class<? extends Protocol> protocolType;

        final ProtocolFactory<? extends Protocol> protocolFactory;

        public Entry(Class<? extends Protocol> protocolType, ProtocolFactory<? extends Protocol> protocolFactory) {
            this.protocolType = protocolType;
            this.protocolFactory = protocolFactory;
        }
    }

    private final Map<Class<? extends Protocol>, ProtocolFactory<?>> protocolFactories;

    public ProtocolGatewayService(Map<Class<? extends Protocol>, ProtocolFactory<?>> protocolFactories) {
        this.protocolFactories = protocolFactories;
    }

    public void registerGatewayFactory(Entry entry) {
        this.protocolFactories.put(entry.protocolType, entry.protocolFactory);
    }

    public void registerGatewayFactory(Class<? extends Protocol> protocolType, ProtocolFactory<? extends Protocol> protocolFactory) {
        registerGatewayFactory(new Entry(protocolType, protocolFactory));
    }

    public ProtocolGateway createGateway(String clientId, Map<String, String> gatewayProperties) {
        // NOTE: PoC implementation, just remap factories
        // in real implementation unique map of protocols will be created for each gateway client call
        // I need to design a ProtocolFamily abstraction for it, gateway will receive family instead of map
        return new DirectProtocolGateway(
            protocolFactories, new DirectGatewayContext(clientId, gatewayProperties)
        );
    }

    public List<Class<? extends Protocol>> listRegisteredProtocols() {
        return new ArrayList<>(protocolFactories.keySet());
    }

}
