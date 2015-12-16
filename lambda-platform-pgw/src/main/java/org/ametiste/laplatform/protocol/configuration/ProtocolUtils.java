package org.ametiste.laplatform.protocol.configuration;

import net.jodah.typetools.TypeResolver;
import org.ametiste.laplatform.protocol.Protocol;
import org.ametiste.laplatform.protocol.ProtocolFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @since
 */
public class ProtocolUtils {

    /**
     * <p>
     *     Represents the pair of {@link Protocol} and {@link ProtocolFactory} which produce instances
     *     of the given {@code Protocol}.
     * </p>
     *
     * @param <P> defines a type of protocol
     * @param <F> defines a type of factory bound to this protocol
     *
     * @see ProtocolUtils#protocolFactoryPair(ProtocolFactory)
     * @since 0.1.0
     */
    public static class Pair<P extends Protocol, F extends ProtocolFactory<P>> {

        private Pair(Class<P> protocol, F factory) {
            this.protocol = protocol;
            this.factory = factory;
        }

        public final Class<P> protocol;

        public final F factory;

    }

    /**
     * <p>
     *     Creates {@link Pair} ({@link Protocol}, {@link ProtocolFactory}) using the given {@code ProtocolFactory}
     *     instance.
     * </p>
     *
     * <p>
     *     {@code Pair} is based on {@code ProtocolFactory} generic types and is intended to programmable usage for
     *     configuration or dispatching purposes.
     * </p>
     *
     * @param pf {@code ProtocolFactory} to be paired with the {@code Protocol} type
     * @return {@code Pair} of {@code Protocol} and {@code ProtocolFactory}
     */
    public static <T extends Protocol> Pair<T, ProtocolFactory<T>> protocolFactoryPair(ProtocolFactory<T> pf) {
        return new Pair<>(lookupProtocolInterface(pf), pf);
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
    public static Map<Class<? extends Protocol>, ProtocolFactory<?>> protocolsMapping(List<ProtocolFactory<?>> protocolFactories) {
        return protocolFactories.stream()
                .collect(Collectors.toMap(p -> p.protocolType(), p -> p));
    }

    /**
     * <p>
     *     Just extracts generic type from the generic interface of the given protocol factory.
     * </p>
     */
    private static <T extends Protocol> Class<T> lookupProtocolInterface(ProtocolFactory<T> pf) {
        return (Class<T>) TypeResolver.resolveRawArgument(ProtocolFactory.class, pf.getClass());
    }

}
