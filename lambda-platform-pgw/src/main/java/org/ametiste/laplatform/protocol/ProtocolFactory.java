package org.ametiste.laplatform.protocol;

import org.ametiste.laplatform.protocol.configuration.ProtocolUtils;

/**
 * <p>
 *     Interface of a factory that creates instances of a protocols of the given factory.
 * </p>
 *
 * @param <T> factory of protocol produced by this factory. Note, main reason to have this general factory
 *           is autoconfiguration capabilities, see {@link ProtocolUtils#protocolFactoryPair(ProtocolFactory)}
 *           for technical details of configuration process.
 *
 * @since 0.1.0
 */
public interface ProtocolFactory<T extends Protocol> {

    T createProtocol(GatewayContext gatewayContext);

    /**
     * <p>
     *     Provides a type of protocol that this factory can produce.
     * </p>
     *
     * <p>
     *     Note, if concrete {@code ProtocolFactory} decides to implement this method, contract to
     *     return only types that the factory can produce is strictly required to apply.
     * </p>
     *
     * @return type of protocol, can't be null.
     */
    default Class<T> protocolType() {
        return ProtocolUtils.protocolFactoryPair(this).protocol;
    }

}
