package org.ametiste.laplatform.protocol.stats;

/**
 *
 * @since
 */
public interface InvocationTimeListener {

    void acceptTiming(final String group, final String name, final String operation, final long timing);

}
