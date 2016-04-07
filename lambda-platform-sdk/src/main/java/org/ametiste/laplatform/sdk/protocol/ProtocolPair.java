package org.ametiste.laplatform.sdk.protocol;

/**
 * <p>
 * Represents the pair of {@link Protocol} and {@link ProtocolFactory} which produce instances
 * of the given {@code Protocol}.
 * </p>
 *
 * @param <P> defines a type of protocol
 * @param <F> defines a type of factory bound to this protocol
 *
 * @since 0.1.0
 */
class ProtocolPair<P extends Protocol, F extends ProtocolFactory<P>> {

    public ProtocolPair(Class<P> protocol, F factory) {
        this.protocol = protocol;
        this.factory = factory;
    }

    final Class<P> protocol;

    final F factory;

}