package org.ametiste.laplatform.protocol.stats;

import java.util.function.BiConsumer;

/**
 *
 * @since
 */
public interface ProtocolStats {

    void onInvocationTiming(InvocationTimeListener listener);

}
