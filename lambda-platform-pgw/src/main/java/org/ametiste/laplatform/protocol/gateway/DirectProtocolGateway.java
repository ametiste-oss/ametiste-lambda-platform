package org.ametiste.laplatform.protocol.gateway;

import org.ametiste.laplatform.protocol.tools.*;
import org.ametiste.laplatform.sdk.protocol.GatewayContext;
import org.ametiste.laplatform.sdk.protocol.Protocol;
import org.ametiste.laplatform.protocol.ProtocolGateway;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @since
 */
public class DirectProtocolGateway implements ProtocolGateway, ProtocolGatewayInstrumentary {

    // NOTE: please use Entry.type as keys for these maps, dont trust in a method incoming types!
    private final Map<Class<? extends Protocol>, ProtocolGatewayService.Entry> protocols;
    private final Map<Class<? extends Protocol>, Protocol> sessions;
    private final Map<Class<? extends Protocol>, SessionStatProxy> sessionProxies;

    // NOTE: this map is the place where incoming type is mapped to internal registered type,
    // if this map does not contain entry for the given protocol type - the protocol type is
    // unregistered under this gateway
    private final Map<Class<? extends Protocol>, ProtocolGatewayService.Entry> sessionEntries;

    private final List<InvocationTimeListener> invocationTimeListeners;
    private final List<InvocationExceptionListener> invocationExceptionListeners;
    private final List<ProtocolConnectionListener> protocolConnectionListeners;
    private final List<ProtocolDisconnectedListener> protocolDisconnectedListenerList;

    private final String client;
    private final GatewayContext gatewayContext;

    public DirectProtocolGateway(String client,
                                 Map<Class<? extends Protocol>,
                                 ProtocolGatewayService.Entry> protocols,
                                 GatewayContext gatewayContext) {
        this.client = client;
        this.gatewayContext = gatewayContext;
        this.protocols = protocols;
        this.sessions = new HashMap<>(5);
        this.sessionProxies = new HashMap<>(2);
        this.sessionEntries = new HashMap<>(5);
        this.invocationTimeListeners = new ArrayList<>(5);
        this.invocationExceptionListeners = new ArrayList<>(5);
        this.protocolConnectionListeners = new ArrayList<>(5);
        this.protocolDisconnectedListenerList = new ArrayList<>(5);
    }

    @Override
    public <T extends Protocol> T session(final Class<T> protocolType, final List<SessionOption> options) {

        T session = protocolOptions(protocolType, options, resolveProtocolSession(protocolType));

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

        final List<Throwable> disconnectErrors = sessions.values().stream()
                .map(this::disconnectSafe)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        sessionEntries.forEach(
            (p, e) -> notifyProtocolDisconnectListeners(e.type, sessions.get(p), e.group, e.name)
        );

        if (!disconnectErrors.isEmpty()) {
            // TODO: add log entriy with all exception listing
            throw new RuntimeException("Disconnection errors. See error log for details.");
        }

    }

    @Override
    public void listenErrors(final InvocationExceptionListener listener) {
        invocationExceptionListeners.add(listener);
    }

    @Override
    public void listenInvocationsTiming(InvocationTimeListener listener) {
        invocationTimeListeners.add(listener);
    }

    @Override
    public void listenProtocolConnection(ProtocolConnectionListener listener) {
        protocolConnectionListeners.add(listener);
    }

    @Override
    public void listenProtocolDisconnected(ProtocolDisconnectedListener listener) {
        protocolDisconnectedListenerList.add(listener);
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

    private <T extends Protocol> T resolveProtocolSession(final Class<T> protocolType) {

        if (!protocols.containsKey(protocolType)) {
            throw new RuntimeException("Gateway has no access to the requested protocol: " + protocolType.getName());
        }

        // NOTE: just use existent session, if any
        if (sessions.containsKey(protocolType)) {
            return protocolType.cast(sessions.get(protocolType));
        }

        final ProtocolGatewayService.Entry entry = resolveEntry(protocolType);
        final Protocol protocol = entry.factory.createProtocol(gatewayContext);

        if (!protocolType.isAssignableFrom(protocol.getClass())) {
            throw new IllegalStateException("Protocol type mismatch, expected: " + protocolType.getName()
                    + " but the gateway has registered type for this protocol: " + protocol.getClass()
                    + " that does not match registered protocol interface.");
        }

        sessions.put(entry.type, protocol);
        sessionEntries.put(entry.type, entry);

        protocol.connect();
        notifyProtocolConnectionListeners(entry.type, protocol, entry.name, entry.group);

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

    private void notifyProtocolConnectionListeners(final Class<? extends Protocol> protocolType,
                                                   final Protocol protocol,
                                                   final String name,
                                                   final String group) {
        protocolConnectionListeners.forEach(l ->
            invokeSafe(() -> l.onProtocolConnection(protocolType, protocol, name, group))
        );
    }

    private void notifyProtocolDisconnectListeners(final Class<? extends Protocol> protocolType,
                                                   final Protocol protocol,
                                                   final String name,
                                                   final String group) {
        protocolDisconnectedListenerList.forEach(l ->
            invokeSafe(() -> l.onProtocolDisconnected(protocolType, protocol, group, name))
        );
    }

    private void notifyInvocExceptionListeners(final String client,
                                               final String name,
                                               final String group,
                                               final String operation,
                                               final Throwable exception) {
        invocationExceptionListeners.forEach(c ->
            invokeSafe(() -> c.handleException(client, name, group, operation, exception))
        );
    }

    private Optional<Throwable> disconnectSafe(Protocol protocol) {
        try {
            protocol.disconnect();
        } catch (Throwable e) {
            return  Optional.of(e);
            // NOTE: just ignore all exceptions on protocol disconnect and return exception if any
        }
        return Optional.empty();
    }

    private void notifyInvocTimingListeners(final String client,
                                            final String name,
                                            final String group,
                                            final String operation,
                                            final long timing) {
        invocationTimeListeners.forEach(c ->
            invokeSafe(() -> c.acceptTiming(client, name, group, operation, timing))
        );
    }

    private void invokeSafe(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            // TODO: add errors log
        }
    }

}
