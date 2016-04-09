package org.ametiste.laplatform.protocol.gateway;

import org.ametiste.laplatform.protocol.stats.InvocationExceptionListener;
import org.ametiste.laplatform.protocol.stats.InvocationTimeListener;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 *
 * @since
 */
public class SessionStatProxy implements InvocationHandler {

    private final String client;
    private final Object obj;
    private final ProtocolGatewayService.Entry sessionDescriptor;
    private final InvocationTimeListener timingListener;
    private final InvocationExceptionListener exceptionListener;

    private long lastMessageTimeTaken;

    public SessionStatProxy(final String client,
                            final Object obj,
                            final ProtocolGatewayService.Entry sessionDescriptor,
                            final InvocationTimeListener timingListener,
                            final InvocationExceptionListener exceptionListener) {
        this.client = client;
        this.obj = obj;
        this.sessionDescriptor = sessionDescriptor;
        this.timingListener = timingListener;
        this.exceptionListener = exceptionListener;
    }

    public long lastInvocationTimeTaken() {
        return lastMessageTimeTaken;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

        final String describedName =
                sessionDescriptor.operationsMapping.get(method.getName());

        final Object result;
        final long startTime = System.currentTimeMillis();

        try {
            result = method.invoke(obj, args);
        } catch (Throwable e) {
            exceptionListener.handleException(client,
                    sessionDescriptor.name, sessionDescriptor.group, describedName, e);
            throw e;
        } finally {
            final long endTime = System.currentTimeMillis();
            this.lastMessageTimeTaken = endTime - startTime;

            timingListener.acceptTiming(client, sessionDescriptor.name,
                    sessionDescriptor.group, describedName, lastMessageTimeTaken);
        }

        return result;
    }

}
