/*
 * Copyright 2014 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blazebit.persistence;

import com.blazebit.persistence.spi.JpqlMacro;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.persistence.Parameter;
import javax.persistence.TemporalType;
import javax.persistence.metamodel.Metamodel;

/**
 * A base interface for builders that support basic query functionality.
 * This interface is shared between normal query builders and subquery builders.
 *
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @since 1.0
 */
public interface CommonQueryBuilder<X extends CommonQueryBuilder<X>> {

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
     * Returns the service provided by this criteria builder or null if none is available.
     * 
     * @param serviceClass The type of the service
     * @return The service or null
     * @since 1.1.0
     */
    public <T> T getService(Class<T> serviceClass);

    /**
     * Sets the given value as the value for the parameter with the given name.
     *
     * @param name The name of the parameter which should be set
     * @param value The value of the parameter that should be set
     * @return The query builder for chaining calls
     */
    public X setParameter(String name, Object value);

    /**
     * Sets the given {@link Calendar} value as the value for the parameter with the given name.
     *
     * @param name The name of the parameter which should be set
     * @param value The value of the parameter that should be set
     * @param temporalType The temporal type of the value
     * @return The query builder for chaining calls
     */
    public X setParameter(String name, Calendar value, TemporalType temporalType);

    /**
     * Sets the given {@link Date} value as the value for the parameter with the given name.
     *
     * @param name The name of the parameter which should be set
     * @param value The value of the parameter that should be set
     * @param temporalType The temporal type of the value
     * @return The query builder for chaining calls
     */
    public X setParameter(String name, Date value, TemporalType temporalType);

    /**
     * Returns true if a parameter with the given name is registered, otherwise false.
     *
     * @param name The name of the parameter that should be checked
     * @return True if the parameter is registered, otherwise false
     */
    public boolean containsParameter(String name);

    /**
     * Returns true if a parameter with the given name is registered and a value has been set, otherwise false.
     *
     * @param name The name of the parameter that should be checked
     * @return True if the parameter is registered and a value has been set, otherwise false
     */
    public boolean isParameterSet(String name);

    /**
     * Returns the parameter object representing the parameter with the given name if
     * {@link FullQueryBuilder#containsParameter(java.lang.String) } returns true, otherwise null.
     *
     * @param name The name of the parameter that should be returned
     * @return The parameter object if the parameter is registered, otherwise null
     */
    public Parameter<?> getParameter(String name);

    /**
     * Returns a set of all registered parameters.
     *
     * @return The set of registered parameters
     */
    public Set<? extends Parameter<?>> getParameters();

    /**
     * Returns the set value for the parameter with the given name. If no value has been set, or the parameter does not exist, null is
     * returned.
     *
     * @param name The name of the parameter for which the value should be returned
     * @return The value of the parameter or null if no value has been set or the parameter does not exist
     */
    public Object getParameterValue(String name);

    /**
     * Updates the type of the parameter with the given name.
     *
     * @param name The name of the parameter for which the type should be set
     * @param type The value of the parameter that should be set
     * @return The query builder for chaining calls
     * @since 1.2.0
     */
    public X setParameterType(String name, Class<?> type);

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
     * Overwrites the properties with the given set of properties
     * 
     * @param properties The new properties
     * @return The query builder for chaining calls
     * @since 1.1.0
     */
    public X setProperties(Map<String, String> properties);
    
    /**
     * Retrieves all properties
     * 
     * @return A map representing all properties
     * @since 1.1.0
     */
    public Map<String, String> getProperties();
}
