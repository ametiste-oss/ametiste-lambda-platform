package org.ametiste.laplatform.protocol.gateway;

import org.ametiste.laplatform.protocol.stats.InvocationTimeListener;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;

/**
 *
 * @since
 */
public class SessionStatProxy implements InvocationHandler {

    private final Object obj;
    private final ProtocolGatewayService.Entry sessionDescriptor;
    private final InvocationTimeListener producer;

    private long lastMessageTimeTaken;

    public SessionStatProxy(final Object obj,
                            final ProtocolGatewayService.Entry sessionDescriptor,
                            final InvocationTimeListener producer) {
        this.obj = obj;
        this.sessionDescriptor = sessionDescriptor;
        this.producer = producer;
    }

    public long lastInvocationTimeTaken() {
        return lastMessageTimeTaken;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        Object result;
        final long startTime = System.currentTimeMillis();
        result = method.invoke(obj, args);
        final long endTime = System.currentTimeMillis();
        this.lastMessageTimeTaken = endTime - startTime;

        // TODO: may I have this at gw or service gw?
        final String describedName =
                sessionDescriptor.operationsMapping.get(method.getName());

        this.producer.acceptTiming(sessionDescriptor.name,
                sessionDescriptor.group, describedName, lastMessageTimeTaken);

        return result;
    }

}
