package org.ametiste.laplatform.protocol.gateway;

import org.ametiste.laplatform.protocol.tools.ProtocolGatewayTool;
import org.ametiste.laplatform.sdk.protocol.Protocol;
import org.ametiste.laplatform.sdk.protocol.ProtocolFactory;
import org.ametiste.laplatform.protocol.ProtocolGateway;

import java.util.*;

/**
 *
 * @since
 */
public class ProtocolGatewayService {

    public static final class Entry {

        public final String name;

        public final String group;

        public final Class<? extends Protocol> type;

        public final Map<String, String> operationsMapping;

        public final ProtocolFactory<? extends Protocol> factory;

        public final boolean isProduceEvents;

        public Entry(final String name,
                     final String group,
                     final Map<String, String> operationsMapping,
                     final Class<? extends Protocol> type,
                     final ProtocolFactory<? extends Protocol> factory) {
            this(name, group, operationsMapping, type, factory, true);
        }

        public Entry(final String name,
                     final String group,
                     final Map<String, String> operationsMapping,
                     final Class<? extends Protocol> type,
                     final ProtocolFactory<? extends Protocol> factory,
                     final boolean isProduceEvents) {
            this.name = name;
            this.group = group;
            this.operationsMapping = Collections.unmodifiableMap(new HashMap<>(operationsMapping));
            this.type = type;
            this.factory = factory;
            this.isProduceEvents = isProduceEvents;
        }

    }

    private final List<ProtocolGatewayTool> gatewayTools;
    private final Map<Class<? extends Protocol>, ProtocolFactory<?>> protocolFactories;

    private final Map<Class<? extends Protocol>, Entry> protocolEntries;

    public ProtocolGatewayService(List<ProtocolGatewayTool> gatewayTools,
                                  Map<Class<? extends Protocol>, ProtocolFactory<?>> protocolFactories) {
        this.gatewayTools = gatewayTools;
        this.protocolFactories = protocolFactories;
        this.protocolEntries = new HashMap<>();

        // NOTE: at the moment of 0.2.2, to provide backward compatibility, simple factories
        // registered as protocols that does not enables STAT option by default.
        //
        // It means that these protocols will not produce invocation timing events, so that
        // gateway servers will not receive and produce metrics for these protocols automaticaly.
        //
        // Basically it means, that the old fashion protocols still responsible to define
        // their own metrics interface.

        this.protocolFactories.forEach((t, f) -> {
            registerProtocol(new Entry(splitCamelCase(t.getSimpleName()),
                    "legacy-0.1", Collections.emptyMap(), t, f, false)
            );
        });
    }

    public void registerProtocol(Entry entry) {
        this.protocolFactories.put(entry.type, entry.factory);
        this.protocolEntries.put(entry.type, entry);
    }

    public void registerProtocol(String protocolName, String protocolGroup,
                                 Map<String, String> operationsMapping, Class<? extends Protocol> protocolType,
                                 ProtocolFactory<? extends Protocol> protocolFactory) {
        registerProtocol(new Entry(protocolName, protocolGroup, operationsMapping, protocolType, protocolFactory));
    }

    public ProtocolGateway createGateway(String clientId,
                                         Map<String, String> gatewayProperties,
                                         List<ProtocolFactory<?>> additionalProtocols) {
        // NOTE: PoC implementation, just remap factories
        // in real implementation unique map of protocols will be created for each gateway client call
        // I need to design a ProtocolFamily abstraction for it, gateway will receive family instead of map

        // TODO: I want to store all created gateways somehow.

        final HashMap<Class<? extends Protocol>, Entry> effectiveEntries = new HashMap<>(
                protocolEntries
        );

        // NOTE: at the moment there is no way to define meta data mapping for additional protocols,
        // such operation names, so these protocols registered as protocols that not produce any events
        additionalProtocols.forEach(
            factory -> effectiveEntries.put(
                factory.protocolType(),
                new Entry(factory.getClass().getName(), "runtime", Collections.emptyMap(), factory.protocolType(), factory, false)
            )
        );

        final DirectProtocolGateway protocolGateway = new DirectProtocolGateway(
                clientId, effectiveEntries, new DirectGatewayContext(clientId, gatewayProperties)
        );

        gatewayTools.forEach(
                tool -> tool.apply(protocolGateway)
        );

        return protocolGateway;
    }

    public ProtocolGateway createGateway(String clientId, Map<String, String> gatewayProperties) {
        return createGateway(clientId, gatewayProperties, Collections.emptyList());
    }

    public List<Entry> listRegisteredProtocols() {
        return new ArrayList<>(protocolEntries.values());
    }

    // TODO: move to ame-lang project
    private static String splitCamelCase(String s) {
        String regex = "([a-z])([A-Z]+)";
        String replacement = "$1-$2";
        return s.replaceAll(regex, replacement)
                .toLowerCase();
    }

}
