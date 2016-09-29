package com.blazebit.persistence.spi;

import java.util.Map;

/**
 * Provides access to configuration parameters.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface ConfigurationSource {

    /**
     * Returns all properties.
     *
     * @return All properties
     */
    public Map<String, String> getProperties();

    /**
     * Returns a property value by name.
     *
     * @param propertyName The name of the property
     * @return The value currently associated with that property name; may be null.
     */
    public String getProperty(String propertyName);

}
