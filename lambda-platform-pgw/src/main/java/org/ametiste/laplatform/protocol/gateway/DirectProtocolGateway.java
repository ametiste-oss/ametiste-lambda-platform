package org.ametiste.laplatform.protocol.gateway;

import org.ametiste.laplatform.sdk.protocol.GatewayContext;
import org.ametiste.laplatform.sdk.protocol.Protocol;
import org.ametiste.laplatform.sdk.protocol.ProtocolFactory;
import org.ametiste.laplatform.protocol.ProtocolGateway;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @since
 */
public class DirectProtocolGateway implements ProtocolGateway {

    private final Map<Class<? extends Protocol>, ProtocolFactory<?>> protocols;

    private final Map<Class<? extends Protocol>, Protocol> sessions;

    private final Map<Class<? extends Protocol>, SessionStatProxy> sessionProxies;

    private final GatewayContext gc;

    public DirectProtocolGateway(Map<Class<? extends Protocol>, ProtocolFactory<?>> protocols, GatewayContext gc) {
        this.gc = gc;
        this.protocols = protocols;
        this.sessions = new HashMap<>();
        this.sessionProxies = new HashMap<>();
    }

    @Override
    public <T extends Protocol> T session(final Class<T> protocolType, final List<SessionOption> options) {

        Protocol session = protocolOptions(protocolType, options, protocolSession(protocolType));

        try {
            return protocolType.cast(session);
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

    private <T extends Protocol> Protocol protocolOptions(final Class<T> protocolType, final List<SessionOption> options, Protocol proxiedSession) {
        // NOTE: just for start, need to rework it to support custom options
        if (options.contains(SessionOption.STATS)) {
            final SessionStatProxy sessionProxy = new SessionStatProxy(proxiedSession);
            proxiedSession = proxySession(
                    proxiedSession, sessionProxy, protocolType
            );
            sessionProxies.put(protocolType, sessionProxy);
        }
        return proxiedSession;
    }

    private <T extends Protocol> Protocol protocolSession(final Class<T> protocolType) {

        if (!protocols.containsKey(protocolType)) {
            throw new RuntimeException("Gateway has no access to the requested protocol: " + protocolType.getName());
        }

        final Protocol protocol = protocols.get(protocolType).createProtocol(gc);

        if (!protocolType.isAssignableFrom(protocol.getClass())) {
            throw new IllegalStateException("Gateway has no access to " +
                    "protocol of the given type: " + protocolType.getName());
        }

        protocol.connect();
        sessions.put(protocolType, protocol);

        return protocol;
    }

    private Protocol proxySession(Protocol obj, SessionStatProxy sessionStatProxy, Class<? extends Protocol> protocolType) {
        Protocol session = protocolType.cast(java.lang.reflect.Proxy.newProxyInstance(
                obj.getClass().getClassLoader(),
                obj.getClass().getInterfaces(),
                sessionStatProxy
        ));
        return session;
    }

}
