/*
 * Copyright 2014 - 2016 Blazebit.
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

package com.blazebit.persistence.view.spi;

import java.util.Properties;
import java.util.Set;

import javax.persistence.EntityManagerFactory;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.view.EntityViewManager;

/**
 * This class is used to configure the entity view manager that it creates.
 *
 * @author Christian Beikov
 * @since 1.0
 */
public interface EntityViewConfiguration {

    /**
     * Adds the given class to the set of known entity views.
     *
     * @param clazz The class to be added
     * @return this for method chaining
     */
    public EntityViewConfiguration addEntityView(Class<?> clazz);

    /**
     * Creates a new entity view manager from this configuration.
     *
     * @param criteriaBuilderFactory The criteria builder factory for which the entity view manager should be created
     * @param entityManagerFactory The entity manager factory for which the entity view manager should be created
     * @return A new entity view manager
     */
    public EntityViewManager createEntityViewManager(CriteriaBuilderFactory criteriaBuilderFactory, EntityManagerFactory entityManagerFactory);

    /**
     * Returns the currently known entity views.
     *
     * @return The currently known entity views
     */
    public Set<Class<?>> getEntityViews();

    /**
     * Returns all properties.
     *
     * @return All properties
     */
    public Properties getProperties();

    /**
     * Returns a property value by name.
     *
     * @param propertyName The name of the property
     * @return The value currently associated with that property name; may be null.
     */
    public String getProperty(String propertyName);

    /**
     * Replace the properties of the configuration with the given properties.
     *
     * @param properties The new set of properties
     * @return this for method chaining
     */
    public EntityViewConfiguration setProperties(Properties properties);

    /**
     * Add the given properties to the properties of the configuration.
     *
     * @param extraProperties The properties to add.
     * @return this for method chaining
     *
     */
    public EntityViewConfiguration addProperties(Properties extraProperties);

    /**
     * Adds the given properties to the properties of the configuration, without overriding existing values.
     *
     * @param properties The properties to merge
     * @return this for method chaining
     */
    public EntityViewConfiguration mergeProperties(Properties properties);

    /**
     * Set a property value by name.
     *
     * @param propertyName The name of the property to set
     * @param value        The new property value
     * @return this for method chaining
     */
    public EntityViewConfiguration setProperty(String propertyName, String value);

}
