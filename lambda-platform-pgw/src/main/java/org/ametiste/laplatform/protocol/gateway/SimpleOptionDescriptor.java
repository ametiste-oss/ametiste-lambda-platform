package org.ametiste.laplatform.protocol.gateway;

import java.util.Map;

/**
 *
 * @since
 */
public class SimpleOptionDescriptor implements OptionDescriptor {

    private final Map<String, Object> stats;

    public SimpleOptionDescriptor(Map<String, Object> stats) {
        this.stats = stats;
    }

    @Override
    public long queryLong(final String statId) {
        return (long) stats.get(statId);
    }

}
