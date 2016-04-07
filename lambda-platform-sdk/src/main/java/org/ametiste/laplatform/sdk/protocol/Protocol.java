package org.ametiste.laplatform.sdk.protocol;

/**
 * <p>
 *     Marker interface to define the root of protocols multiplicity.
 * </p>
 *
 * <p>
 *     Any {@code Protocol} interface must extend this interface to be compatible with the
 *     protocol gateway subsystem.
 * </p>
 *
 * @since 0.1.0
 */
public interface Protocol {

    /**
     * This method will be called after protocol instance creation, allowing to execute
     * protocol connection logic.
     *
     */
    default void connect() { };

    /**
     * This method will be called after protocol usage finished by the client, allowing to execute
     * protocol discconection logic.
     *
     */
    default void disconnect() { };

}
