package org.ametiste.laplatform.protocol.gateway;

import org.ametiste.laplatform.protocol.stats.ProtocolGatewayInstrumentary;

/**
 *
 * @since
 */
public interface ProtocolGatewayTool {

    void apply(ProtocolGatewayInstrumentary gateway);

}
