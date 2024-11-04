/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

import com.blazebit.persistence.spi.ConfigurationSource;
import com.blazebit.persistence.spi.JpqlMacro;
import com.blazebit.persistence.spi.ServiceProvider;

import jakarta.persistence.metamodel.Metamodel;
import java.util.Map;

/**
 * A base interface for builders that support basic query functionality.
 * This interface is shared between normal query builders and subquery builders.
 *
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface CommonQueryBuilder<X extends CommonQueryBuilder<X>> extends ServiceProvider, ConfigurationSource, ParameterHolder<X> {

    /**
     * Returns the JPA {@link Metamodel} of the persistence unit which is used by this query builder.
     *
     * @return The JPA metamodel
     */
    public Metamodel getMetamodel();

    /**
     * The criteria builder factory that created this or it's parent builder.
     * 
     * @return The criteria builder factory
     * @since 1.0.5
     */
    public CriteriaBuilderFactory getCriteriaBuilderFactory();

    /**
     * Registers the given jpql macro for this query builder.
     *
     * @param macroName The name of the macro
     * @param jpqlMacro The jpql macro
     * @return The query builder for chaining calls
     * @since 1.2.0
     */
    public X registerMacro(String macroName, JpqlMacro jpqlMacro);
    
    /**
     * Sets a configuration property with the given propertyName to the given propertyValue.
     * If a property with this name does not exist, it is added.
     * 
     * @param propertyName The name of the property
     * @param propertyValue The value of the property
     * @return The query builder for chaining calls
     * @since 1.1.0
     */
    public X setProperty(String propertyName, String propertyValue);
    
    /**
     * Overwrites the properties with the given set of properties.
     * 
     * @param properties The new properties
     * @return The query builder for chaining calls
     * @since 1.1.0
     */
    public X setProperties(Map<String, String> properties);

    /**
     * Configures whether the query result should be cached.
     *
     * @param cacheable Whether the query result should be cached
     * @return The query builder for chaining calls
     * @since 1.2.0
     */
    public X setCacheable(boolean cacheable);

    /**
     * Returns whether the query result should be cached.
     *
     * @return Whether the query result should be cached
     * @since 1.2.0
     */
    public boolean isCacheable();
    
}
