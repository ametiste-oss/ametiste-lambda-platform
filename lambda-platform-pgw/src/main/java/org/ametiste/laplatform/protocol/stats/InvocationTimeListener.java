package org.ametiste.laplatform.protocol.stats;

/**
 *
 * @since
 */
public interface InvocationTimeListener {

    void acceptTiming(
            final String client,
            final String group,
            final String protocol,
            final String operation,
            final long timing
    );

}
