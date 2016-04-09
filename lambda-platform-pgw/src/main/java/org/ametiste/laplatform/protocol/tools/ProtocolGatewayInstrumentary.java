package org.ametiste.laplatform.protocol.tools;

/**
 *
 * @since
 */
public interface ProtocolGatewayInstrumentary {

    void listenErrors(InvocationExceptionListener listener);

    void listenInvocationsTiming(InvocationTimeListener listener);

    void listenProtocolConnection(ProtocolConnectionListener listener);

    void listenProtocolDisconnected(ProtocolDisconnectedListener listener);

}
