package org.ametiste.laplatform.protocol.gateway;

import org.ametiste.laplatform.protocol.stats.InvocationExceptionListener;
import org.ametiste.laplatform.protocol.stats.InvocationTimeListener;
import org.ametiste.laplatform.protocol.stats.ProtocolGatewayInstrumentary;
import org.ametiste.laplatform.sdk.protocol.GatewayContext;
import org.ametiste.laplatform.sdk.protocol.Protocol;
import org.ametiste.laplatform.protocol.ProtocolGateway;

import java.util.*;

/**
 *
 * @since
 */
public class DirectProtocolGateway implements ProtocolGateway, ProtocolGatewayInstrumentary {

    private final Map<Class<? extends Protocol>, ProtocolGatewayService.Entry> protocols;

    private final Map<Class<? extends Protocol>, Protocol> sessions;

    private final Map<Class<? extends Protocol>, SessionStatProxy> sessionProxies;

    private final List<InvocationTimeListener> invocationTimeListeners;

    private final List<InvocationExceptionListener> invocationExceptionListeners;

    private final String client;
    private final GatewayContext gc;

    public DirectProtocolGateway(String client, Map<Class<? extends Protocol>, ProtocolGatewayService.Entry> protocols, GatewayContext gc) {
        this.client = client;
        this.gc = gc;
        this.protocols = protocols;
        this.sessions = new HashMap<>(5);
        this.sessionProxies = new HashMap<>(2);
        this.invocationTimeListeners = new ArrayList<>(5);
        this.invocationExceptionListeners = new ArrayList<>(5);
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
    public void listenErrors(final InvocationExceptionListener listener) {
        invocationExceptionListeners.add(listener);
    }

    @Override
    public void listenInvocationsTiming(InvocationTimeListener listener) {
        invocationTimeListeners.add(listener);
    }

    private <T extends Protocol> T protocolOptions(final Class<T> protocolType, final List<SessionOption> options, Protocol proxiedSession) {

        final ProtocolGatewayService.Entry entry = resolveEntry(protocolType);

        // NOTE: see ProtocolGatewayService constructor for details on entry.isProduceEvents usage
        final boolean isWantToProduce = ((!invocationTimeListeners.isEmpty()) && entry.isProduceEvents);

        // NOTE: just for start, need to rework it to support custom options
        if (isWantToProduce || options.contains(SessionOption.STATS)) {
            // TODO: Can the gateway or other option object be listener? I want to extractr results storing from the SessionStatProxy object

            final SessionStatProxy proxyObject;

            if(isWantToProduce) {
                proxyObject = new SessionStatProxy(
                        client,
                        proxiedSession,
                        entry,
                        this::notifyInvocTimingListeners,
                        this::notifyInvocExceptionListeners
                );
            } else {
                // NOTE: just to support the clients that uses legacy protocol creation model
                // NOTE: this proxy have empty producer passed, so it will not produce any event
                // NOTE: so the server will no aggregate metrics, etc.
                // NOTE: it will be removed after refactoring
                proxyObject = new SessionStatProxy(
                        client,
                        proxiedSession,
                        entry,
                        (c, n, g, o, t) -> {},
                        (c, n, g, o, e) -> {});
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

    private void notifyInvocExceptionListeners(final String client,
                                            final String name,
                                            final String group,
                                            final String operation,
                                            final Throwable exception) {
        invocationExceptionListeners.forEach(
                c -> c.handleException(client, name, group, operation, exception)
        );
    }

    private void notifyInvocTimingListeners(final String client,
                                            final String name,
                                            final String group,
                                            final String operation,
                                            final long timing) {
        invocationTimeListeners.forEach(
                c -> c.acceptTiming(client, name, group, operation, timing)
        );
    }

}
