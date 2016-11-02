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

package com.blazebit.persistence.view.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.persistence.EntityManagerFactory;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class EntityViewConfigurationImpl implements EntityViewConfiguration {

    private final Set<Class<?>> entityViewClasses = new HashSet<Class<?>>();
    private Properties properties = new Properties();

    public EntityViewConfigurationImpl() {
        loadDefaultProperties();
    }
    
    private void loadDefaultProperties() {
        properties.put(ConfigurationProperties.PROXY_EAGER_LOADING, "false");
        properties.put(ConfigurationProperties.PROXY_UNSAFE_ALLOWED, "true");
    }

    @Override
    public EntityViewConfiguration addEntityView(Class<?> clazz) {
        entityViewClasses.add(clazz);
        return this;
    }

    @Override
    public Set<Class<?>> getEntityViews() {
        return entityViewClasses;
    }

    @Override
    public EntityViewManager createEntityViewManager(CriteriaBuilderFactory cbf, EntityManagerFactory emf) {
        return new EntityViewManagerImpl(this, cbf, emf);
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public String getProperty(String propertyName) {
        return properties.getProperty(propertyName);
    }

    @Override
    public EntityViewConfiguration setProperties(Properties properties) {
        this.properties = properties;
        return this;
    }

    @Override
    public EntityViewConfiguration addProperties(Properties extraProperties) {
        this.properties.putAll(extraProperties);
        return this;
    }

    @Override
    public EntityViewConfiguration mergeProperties(Properties properties) {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            if (this.properties.containsKey(entry.getKey())) {
                continue;
            }
            this.properties.setProperty((String) entry.getKey(), (String) entry.getValue());
        }
        return this;
    }

    @Override
    public EntityViewConfiguration setProperty(String propertyName, String value) {
        properties.setProperty(propertyName, value);
        return this;
    }
}
