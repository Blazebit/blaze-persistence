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

package com.blazebit.persistence.spi;

import com.blazebit.persistence.CriteriaBuilderFactory;

import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * A configuration for a {@link CriteriaBuilderFactory} which is mostly used in non Java EE environments.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface CriteriaBuilderConfiguration {

    /**
     * Sets the package opener to use for obtaining access to user classes.
     *
     * @param packageOpener The package opener to use to obtain access to user classes
     * @return this for method chaining
     * @since 1.2.0
     */
    public CriteriaBuilderConfiguration withPackageOpener(PackageOpener packageOpener);

    /**
     * Registers the given type under the given name. This makes the type usable for the <code>VALUES</code> clause.
     *
     * @param name The name of the type
     * @param type The type
     * @return this for method chaining
     * @since 1.2.0
     */
    public CriteriaBuilderConfiguration registerNamedType(String name, Class<?> type);

    /**
     * Registers the given jpql function group in the configuration.
     *
     * @param jpqlFunctionGroup The jpql function group
     * @return this for method chaining
     */
    public CriteriaBuilderConfiguration registerFunction(JpqlFunctionGroup jpqlFunctionGroup);

    /**
     * Registers the given jpql macro in the configuration.
     *
     * @param macroName The name of the macro
     * @param jpqlMacro The jpql macro
     * @return this for method chaining
     * @since 1.2.0
     */
    public CriteriaBuilderConfiguration registerMacro(String macroName, JpqlMacro jpqlMacro);
    
    /**
     * Registers the given dialect for the given dbms name.
     * 
     * @param dbms The dbms for which the dialect should be registered
     * @param dialect The dialect which should be registered
     * @return this for method chaining
     * @since 1.1.0
     */
    public CriteriaBuilderConfiguration registerDialect(String dbms, DbmsDialect dialect);

    /**
     * Returns the {@link JpqlFunctionGroup} for registered function with the given name or <code>null</code>.
     *
     * @param name The name of the functino to retrieve
     * @return the registered function or <code>null</code>
     * @since 1.2.0
     */
    public JpqlFunctionGroup getFunction(String name);

    /**
     * Returns the set of registered functions.
     * 
     * @return the set of registered functions
     */
    public Set<String> getFunctionNames();

    /**
     * Returns the set of registered macros.
     *
     * @return the set of registered macros
     */
    public Set<String> getMacroNames();

    /**
     * Returns a map of registered named types.
     *
     * @return a map of the registered named types
     * @since 1.2.0
     */
    public Map<String, Class<?>> getNamedTypes();

    /**
     * Registers the given entity manager enricher in the configuration.
     *
     * @param entityManagerEnricher The enricher that should be added
     * @return this for method chaining
     */
    public CriteriaBuilderConfiguration registerEntityManagerIntegrator(EntityManagerFactoryIntegrator entityManagerEnricher);

    /**
     * Returns a list of registered entity manager enrichers.
     *
     * @return A list of registered entity manager enrichers
     */
    public List<EntityManagerFactoryIntegrator> getEntityManagerIntegrators();

    /**
     * Creates a new {@linkplain CriteriaBuilderFactory} based on this configuration.
     *
     * @param entityManagerFactory The entity manager factory for which the criteria builder factory should be created
     * @return A new {@linkplain CriteriaBuilderFactory}
     */
    public CriteriaBuilderFactory createCriteriaBuilderFactory(EntityManagerFactory entityManagerFactory);

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
    public CriteriaBuilderConfiguration setProperties(Properties properties);

    /**
     * Add the given properties to the properties of the configuration.
     *
     * @param extraProperties The properties to add.
     * @return this for method chaining
     *
     */
    public CriteriaBuilderConfiguration addProperties(Properties extraProperties);

    /**
     * Adds the given properties to the properties of the configuration, without overriding existing values.
     *
     * @param properties The properties to merge
     * @return this for method chaining
     */
    public CriteriaBuilderConfiguration mergeProperties(Properties properties);

    /**
     * Set a property value by name.
     *
     * @param propertyName The name of the property to set
     * @param value The new property value
     * @return this for method chaining
     */
    public CriteriaBuilderConfiguration setProperty(String propertyName, String value);
}
