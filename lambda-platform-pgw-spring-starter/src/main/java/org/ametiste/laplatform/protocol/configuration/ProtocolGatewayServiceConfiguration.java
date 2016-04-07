package org.ametiste.laplatform.protocol.configuration;

import org.ametiste.laplatform.sdk.protocol.Protocol;
import org.ametiste.laplatform.sdk.protocol.ProtocolFactory;
import org.ametiste.laplatform.protocol.gateway.ProtocolGatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            protocolsMapping(protocolFactories)
        );
    }

    /**
     * <p>
     *     Creates simple {@link Map} of {@code {Protocol -> ProtocolFactory}}
     *     entries using the given list of {@code ProtocolFactory} instances, where
     *     each {@code ProtocolFactory} instance mapped to the {@code Protocol} type
     *     that this factory can produce.
     * </p>
     *
     * @param protocolFactories factories to be mapped to produced protocol types, can't be null.
     * @return {@code { protocol -> factory }} map, can't be null.
     */
    static Map<Class<? extends Protocol>, ProtocolFactory<?>> protocolsMapping(List<ProtocolFactory<?>> protocolFactories) {
        return protocolFactories.stream()
                .collect(Collectors.toMap(ProtocolFactory::protocolType, p -> p));
    }

}
