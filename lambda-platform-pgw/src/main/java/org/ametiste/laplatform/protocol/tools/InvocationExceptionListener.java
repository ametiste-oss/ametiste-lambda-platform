package org.ametiste.laplatform.protocol.tools;

/**
 *
 * @since
 */
public interface InvocationExceptionListener {

    void handleException(
            final String client,
            final String group,
            final String protocol,
            final String operation,
            final Throwable exception
    );

}
