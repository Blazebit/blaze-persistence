/*
 * Copyright 2014 - 2018 Blazebit.
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

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.persistence.EntityManagerFactory;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.spi.type.BasicUserType;
import com.blazebit.persistence.view.spi.type.TypeConverter;

/**
 * This class is used to configure the entity view manager that it creates.
 *
 * @author Christian Beikov
 * @since 1.0.0
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
     * Creates an entity view mapping based on the given annotated class
     * that can be further refined and finally added.
     *
     * @param clazz The annotated class to base the mapping on
     * @return the entity view mapping
     * @since 1.2.0
     */
    public EntityViewMapping createEntityViewMapping(Class<?> clazz);

    /**
     * Registers the given user type for the given class.
     *
     * @param clazz The class for which to register the user type
     * @param userType The user type implementation
     * @param <X> The type of the class
     * @return this for method chaining
     * @since 1.2.0
     */
    public <X> EntityViewConfiguration registerBasicUserType(Class<X> clazz, BasicUserType<X> userType);

    /**
     * Registers the given converter for the given types.
     *
     * @param underlyingType The underlying type supported by the entity view type system
     * @param viewModelType The entity view model type
     * @param converter The type converter
     * @param <X> The underlying type
     * @param <Y> The entity view model type
     * @return this for method chaining
     * @since 1.2.0
     */
    public <X, Y> EntityViewConfiguration registerTypeConverter(Class<X> underlyingType, Class<Y> viewModelType, TypeConverter<X, Y> converter);

    /**
     * Creates a new entity view manager from this configuration.
     *
     * @param criteriaBuilderFactory The criteria builder factory for which the entity view manager should be created
     * @return A new entity view manager
     * @since 1.2.0
     */
    public EntityViewManager createEntityViewManager(CriteriaBuilderFactory criteriaBuilderFactory);

    /**
     * Creates a new entity view manager from this configuration.
     *
     * @param criteriaBuilderFactory The criteria builder factory for which the entity view manager should be created
     * @param entityManagerFactory The entity manager factory for which the entity view manager should be created
     * @return A new entity view manager
     * @deprecated Will be removed. Use {@link #createEntityViewManager(CriteriaBuilderFactory)} instead.
     */
    @Deprecated
    public EntityViewManager createEntityViewManager(CriteriaBuilderFactory criteriaBuilderFactory, EntityManagerFactory entityManagerFactory);

    /**
     * Returns the currently known entity views.
     *
     * @return The currently known entity views
     */
    public Set<Class<?>> getEntityViews();

    /**
     * Returns the currently registered entity view mappings.
     *
     * @return The currently registered entity view mappings.
     * @since 1.2.0
     */
    public Collection<EntityViewMapping> getEntityViewMappings();

    /**
     * Returns the currently registered basic user types.
     *
     * @return The currently registered basic user types.
     * @since 1.2.0
     */
    public Map<Class<?>, BasicUserType<?>> getBasicUserTypes();

    /**
     * Returns the currently registered type converters.
     *
     * @return The currently registered type converters.
     * @since 1.2.0
     */
    public Map<Class<?>, Map<Class<?>, TypeConverter<?, ?>>> getTypeConverters();

    /**
     * Returns the currently registered type converters for the given view model type.
     *
     * @param viewModelType The view model type
     * @param <Y> The entity view model type
     * @return The currently registered type converters for the given view model type.
     * @since 1.2.0
     */
    public <Y> Map<Class<?>, TypeConverter<?, Y>> getTypeConverters(Class<Y> viewModelType);

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
