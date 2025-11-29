/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

import jakarta.persistence.Parameter;
import jakarta.persistence.TemporalType;
import jakarta.persistence.criteria.ParameterExpression;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

/**
 * A base interface for builders that can hold parameters.
 *
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface ParameterHolder<X extends ParameterHolder<X>> {

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
     * {@link FullQueryBuilder#containsParameter(String) } returns true, otherwise null.
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
     * @deprecated Use {@link #registerCriteriaParameter(String, ParameterExpression)} instead
     */
    @Deprecated
    public X setParameterType(String name, Class<?> type);

    /**
     * Registers the given JPA Criteria parameter under the given name.
     *
     * @param name The name of the parameter for which the expression should be registered
     * @param parameter The JPA Criteria parameter that should be registered
     * @return The query builder for chaining calls
     * @since 1.6.3
     */
    public X registerCriteriaParameter(String name, ParameterExpression<?> parameter);
    
}
