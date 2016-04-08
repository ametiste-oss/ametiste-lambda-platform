package org.ametiste.laplatform.protocol.gateway;

import org.ametiste.laplatform.protocol.stats.InvocationTimeListener;
import org.ametiste.laplatform.protocol.stats.ProtocolStats;
import org.ametiste.laplatform.sdk.protocol.GatewayContext;
import org.ametiste.laplatform.sdk.protocol.Protocol;
import org.ametiste.laplatform.sdk.protocol.ProtocolFactory;
import org.ametiste.laplatform.protocol.ProtocolGateway;

import java.util.*;
import java.util.function.BiConsumer;

/**
 *
 * @since
 */
public class DirectProtocolGateway implements ProtocolGateway {

    private final Map<Class<? extends Protocol>, ProtocolGatewayService.Entry> protocols;

    private final Map<Class<? extends Protocol>, Protocol> sessions;

    private final Map<Class<? extends Protocol>, SessionStatProxy> sessionProxies;

    private final List<InvocationTimeListener> invocationTimeListeners;

    private final GatewayContext gc;

    public DirectProtocolGateway(Map<Class<? extends Protocol>, ProtocolGatewayService.Entry> protocols, GatewayContext gc) {
        this.gc = gc;
        this.protocols = protocols;
        this.sessions = new HashMap<>(5);
        this.sessionProxies = new HashMap<>(2);
        this.invocationTimeListeners = new ArrayList<>(5);
    }

    @Override
    public <T extends Protocol> T session(final Class<T> protocolType, final List<SessionOption> options) {

        T session = protocolOptions(protocolType, options, protocolSession(protocolType));

        try {
            return session;
        } catch (ClassCastException e) {
            throw new IllegalStateException("Gateway has error during access to " +
                    "protocol of the given type: " + protocolType.getName(), e);
        }
    }

    @Override
    public OptionDescriptor sessionOption(final Class<?> protocolType, SessionOption option) {
        // NOTE: just for start, need to rework it to support custom options
        if (option.equals(SessionOption.STATS)) {

            if (!sessionProxies.containsKey(protocolType)) {
                throw new RuntimeException("Gateway has no access " +
                        "to the session stats, try enable stats tracking for this session: " + protocolType.getName());
            }

            final SessionStatProxy sessionStatProxy = sessionProxies.get(protocolType);

            return new SimpleOptionDescriptor(
                    Collections.singletonMap("session.last.invocation.time", sessionStatProxy.lastInvocationTimeTaken())
            );
        } else {
            throw new IllegalArgumentException("Unsupported session option: " + option);
        }
    }

    @Override
    public void release() {
        sessions.values().forEach(Protocol::disconnect);
    }

    @Override
    public void onInvocationTiming(InvocationTimeListener listener) {
        invocationTimeListeners.add(listener::acceptTiming);
    }

    private <T extends Protocol> T protocolOptions(final Class<T> protocolType, final List<SessionOption> options, Protocol proxiedSession) {

        final ProtocolGatewayService.Entry entry = resolveEntry(protocolType);

        // NOTE: see ProtocolGatewayService constructor for details on entry.isProduceTimingEvents usage
        final boolean isWantToProduce = ((!invocationTimeListeners.isEmpty()) && entry.isProduceTimingEvents);

        // NOTE: just for start, need to rework it to support custom options
        if (isWantToProduce || options.contains(SessionOption.STATS)) {
            // TODO: Can the gateway or other option object be listener? I want to extractr results storing from the SessionStatProxy object

            final SessionStatProxy proxyObject;

            if(isWantToProduce) {
                proxyObject = new SessionStatProxy(proxiedSession, entry, this::notifyInvocTimingListeners);
            } else {
                // NOTE: just to support the clients that uses legacy protocol creation model
                // NOTE: this proxy have empty producer passed, so it will not produce any event
                // NOTE: so the server will no aggregate metrics, etc.
                // NOTE: it will be removed after refactoring
                proxyObject = new SessionStatProxy(proxiedSession, entry, (n, g, o, t) -> {});
            }

            proxiedSession = proxySession(proxiedSession, proxyObject, protocolType);
            sessionProxies.put(protocolType, proxyObject);
        }

        return protocolType.cast(proxiedSession);
    }

    private <T extends Protocol> T protocolSession(final Class<T> protocolType) {

        if (!protocols.containsKey(protocolType)) {
            throw new RuntimeException("Gateway has no access to the requested protocol: " + protocolType.getName());
        }

        final Protocol protocol = resolveEntry(protocolType).factory.createProtocol(gc);

        if (!protocolType.isAssignableFrom(protocol.getClass())) {
            throw new IllegalStateException("Gateway has no access to " +
                    "protocol of the given type: " + protocolType.getName());
        }

        protocol.connect();
        sessions.put(protocolType, protocol);

        return protocolType.cast(protocol);
    }

    private <T extends Protocol> ProtocolGatewayService.Entry resolveEntry(final Class<T> protocolType) {
        return protocols.get(protocolType);
    }

    private <T extends Protocol> T proxySession(Protocol obj, SessionStatProxy sessionStatProxy, Class<T> protocolType) {
        T session = protocolType.cast(java.lang.reflect.Proxy.newProxyInstance(
                obj.getClass().getClassLoader(),
                obj.getClass().getInterfaces(),
                sessionStatProxy
        ));
        return session;
    }

    private void notifyInvocTimingListeners(final String name,
                                            final String group,
                                            final String operation,
                                            final long timing) {
        invocationTimeListeners.forEach(
                c -> c.acceptTiming(name, group, operation, timing)
        );
    }

}
