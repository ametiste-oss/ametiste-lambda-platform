package org.ametiste.laplatform.protocol;

import org.ametiste.laplatform.protocol.gateway.SessionOption;
import org.ametiste.laplatform.protocol.gateway.OptionDescriptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @since
 */
public interface ProtocolGateway {

    <T extends Protocol> T session(Class<T> protocolType, List<SessionOption> options);

    default <T extends Protocol> T session(Class<T> protocolType) {
        return session(protocolType, Collections.emptyList());
    }

    default <T extends Protocol> T session(Class<T> protocolType, SessionOption... options) {
        return session(protocolType, Arrays.asList(options));
    }

    OptionDescriptor sessionOption(Class<?> protocolType, SessionOption option);

}
