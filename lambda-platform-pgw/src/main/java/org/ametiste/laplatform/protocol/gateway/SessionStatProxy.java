package org.ametiste.laplatform.protocol.gateway;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * @since
 */
public class SessionStatProxy implements InvocationHandler {

    private final Object obj;

    private long lastMessageTimeTaken;

    public SessionStatProxy(final Object obj) {
        this.obj = obj;
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
        return result;
    }

}
