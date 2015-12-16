package org.ametiste.laplatform.protocol;

/**
 *
 * @since
 */
public interface ProtocolFamily {

    ProtocolGateway createGateway(String protoName /*TODO: add context holder supplier */);

}
