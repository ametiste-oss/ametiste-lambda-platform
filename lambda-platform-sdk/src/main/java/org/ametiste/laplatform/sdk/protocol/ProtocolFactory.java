package org.ametiste.laplatform.sdk.protocol;

import net.jodah.typetools.TypeResolver;

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
        return protocolFactoryPair(this).protocol;
    }

    /**
     * <p>
     *     Creates {@link ProtocolUtils.Pair} ({@link Protocol}, {@link ProtocolFactory}) using the given {@code ProtocolFactory}
     *     instance.
     * </p>
     *
     * <p>
     *     {@code ProtocolPair} is based on {@code ProtocolFactory} generic types and is intended to programmable usage for
     *     configuration or dispatching purposes.
     * </p>
     *
     * <p>
     *     Note, this is static method to implement interface default behavior.
     * </p>
     *
     * @param pf {@code ProtocolFactory} to be paired with the {@code Protocol} type
     * @return {@code ProtocolPair} of {@code Protocol} and {@code ProtocolFactory}
     */
    static <T extends Protocol> ProtocolPair<T, ProtocolFactory<T>> protocolFactoryPair(ProtocolFactory<T> pf) {
        return new ProtocolPair<>(lookupProtocolInterface(pf), pf);
    }

    static <T extends Protocol> Class<T> lookupProtocolInterface(ProtocolFactory<T> pf) {
        return (Class<T>) TypeResolver.resolveRawArgument(ProtocolFactory.class, pf.getClass());
    }

}
