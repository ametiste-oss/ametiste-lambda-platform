package org.ametiste.laplatform.protocol.stats;

import java.util.function.BiConsumer;

/**
 *
 * @since
 */
public interface ProtocolGatewayInstrumentary {

    void listenErrors(InvocationExceptionListener listener);

    void listenInvocationsTiming(InvocationTimeListener listener);

}
