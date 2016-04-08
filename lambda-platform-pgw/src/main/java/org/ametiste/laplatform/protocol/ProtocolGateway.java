package org.ametiste.laplatform.protocol;

import org.ametiste.laplatform.protocol.gateway.SessionOption;
import org.ametiste.laplatform.protocol.gateway.OptionDescriptor;
import org.ametiste.laplatform.protocol.stats.ProtocolStats;
import org.ametiste.laplatform.sdk.protocol.Protocol;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @since
 */
public interface ProtocolGateway extends ProtocolStats {

    <T extends Protocol> T session(Class<T> protocolType, List<SessionOption> options);

    default <T extends Protocol> T session(Class<T> protocolType) {
        return session(protocolType, Collections.emptyList());
    }

    default <T extends Protocol> T session(Class<T> protocolType, SessionOption... options) {
        return session(protocolType, Arrays.asList(options));
    }

    OptionDescriptor sessionOption(Class<?> protocolType, SessionOption option);

    /**
     * Releases this gateway instance, must be called by the gateway server after
     * gateway at the end of gateway cycle.
     */
    void release();

}
