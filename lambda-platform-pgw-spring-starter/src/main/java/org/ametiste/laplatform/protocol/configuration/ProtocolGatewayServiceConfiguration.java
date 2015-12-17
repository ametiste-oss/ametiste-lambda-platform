package org.ametiste.laplatform.protocol.configuration;

import org.ametiste.laplatform.protocol.ProtocolFactory;import org.ametiste.laplatform.protocol.gateway.ProtocolGatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 *
 * @since
 */
@Configuration
public class ProtocolGatewayServiceConfiguration {

    @Autowired
    private List<ProtocolFactory<?>> protocolFactories;

    @Bean
    public ProtocolGatewayService protocolGatewayService() {
        return new ProtocolGatewayService(
            ProtocolUtils.protocolsMapping(protocolFactories)
        );
    }

}
