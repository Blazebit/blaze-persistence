/*
 * Copyright 2014 - 2019 Blazebit.
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

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.impl.metamodel.AnnotationViewMappingReader;
import com.blazebit.persistence.view.impl.metamodel.MetamodelBootContext;
import com.blazebit.persistence.view.impl.metamodel.MetamodelBootContextImpl;
import com.blazebit.persistence.view.impl.metamodel.ViewMappingReader;
import com.blazebit.persistence.view.impl.type.MutableBasicUserTypeRegistry;
import com.blazebit.persistence.view.spi.type.BasicUserType;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.spi.EntityViewMapping;
import com.blazebit.persistence.view.spi.type.TypeConverter;

import javax.persistence.EntityManagerFactory;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class EntityViewConfigurationImpl implements EntityViewConfiguration {

    private final MutableBasicUserTypeRegistry userTypeRegistry = new MutableBasicUserTypeRegistry();
    private final MetamodelBootContext bootContext = new MetamodelBootContextImpl();
    private final ViewMappingReader annotationViewMappingReader = new AnnotationViewMappingReader(bootContext);
    private final Map<Class<?>, Object> typeTestValues = new HashMap<>();
    private Properties properties = new Properties();

    public EntityViewConfigurationImpl() {
        loadDefaultProperties();
    }

    private void loadDefaultProperties() {
        properties.put(ConfigurationProperties.PROXY_EAGER_LOADING, "false");
        properties.put(ConfigurationProperties.PROXY_UNSAFE_ALLOWED, "true");
        properties.put(ConfigurationProperties.MANAGED_TYPE_VALIDATION_DISABLED, "false");
        properties.put(ConfigurationProperties.UPDATER_DISALLOW_OWNED_UPDATABLE_SUBVIEW, "true");

        typeTestValues.put(boolean.class, true);
        typeTestValues.put(byte.class, Byte.MAX_VALUE);
        typeTestValues.put(short.class, Short.MAX_VALUE);
        typeTestValues.put(char.class, Character.MAX_VALUE);
        typeTestValues.put(int.class, Integer.MAX_VALUE);
        typeTestValues.put(long.class, Long.MAX_VALUE);
        typeTestValues.put(float.class, Float.MAX_VALUE);
        typeTestValues.put(double.class, Double.MAX_VALUE);
        typeTestValues.put(Boolean.class, true);
        typeTestValues.put(Byte.class, Byte.MAX_VALUE);
        typeTestValues.put(Short.class, Short.MAX_VALUE);
        typeTestValues.put(Character.class, Character.MAX_VALUE);
        typeTestValues.put(Integer.class, Integer.MAX_VALUE);
        typeTestValues.put(Long.class, Long.MAX_VALUE);
        typeTestValues.put(Float.class, Float.MAX_VALUE);
        typeTestValues.put(Double.class, Double.MAX_VALUE);
        typeTestValues.put(String.class, "-");
        typeTestValues.put(Date.class, new Date(1));
        typeTestValues.put(java.sql.Date.class, new java.sql.Date(1));
        typeTestValues.put(Time.class, new Time(1000));
        typeTestValues.put(Timestamp.class, new Timestamp(1));
        typeTestValues.put(Calendar.class, Calendar.getInstance());
        typeTestValues.put(GregorianCalendar.class, new GregorianCalendar());
        typeTestValues.put(byte[].class, new byte[] { Byte.MAX_VALUE });
        typeTestValues.put(Byte[].class, new Byte[] { Byte.MAX_VALUE });
        typeTestValues.put(char[].class, new char[] { Character.MAX_VALUE });
        typeTestValues.put(Character[].class, new Character[] { Character.MAX_VALUE });
        typeTestValues.put(BigInteger.class, BigInteger.TEN);
        typeTestValues.put(BigDecimal.class, BigDecimal.TEN);
        typeTestValues.put(Serializable.class, "-");
    }

    @Override
    public EntityViewConfiguration addEntityView(Class<?> clazz) {
        createEntityViewMapping(clazz);
        return this;
    }

    @Override
    public EntityViewMapping createEntityViewMapping(Class<?> clazz) {
        return annotationViewMappingReader.readViewMapping(clazz);
    }

    @Override
    public Set<Class<?>> getEntityViews() {
        return Collections.unmodifiableSet(bootContext.getViewClasses());
    }

    @Override
    public Collection<EntityViewMapping> getEntityViewMappings() {
        return Collections.<EntityViewMapping>unmodifiableCollection(bootContext.getViewMappings());
    }

    @Override
    public <X> EntityViewConfiguration registerBasicUserType(Class<X> clazz, BasicUserType<X> userType) {
        userTypeRegistry.registerBasicUserType(clazz, userType);
        return this;
    }

    @Override
    public <X, Y> EntityViewConfiguration registerTypeConverter(Class<X> underlyingType, Class<Y> viewModelType, TypeConverter<X, Y> converter) {
        userTypeRegistry.registerTypeConverter(underlyingType, viewModelType, converter);
        return this;
    }

    @Override
    public Map<Class<?>, BasicUserType<?>> getBasicUserTypes() {
        return userTypeRegistry.getBasicUserTypes();
    }

    @Override
    public Map<Class<?>, Map<Class<?>, TypeConverter<?, ?>>> getTypeConverters() {
        return userTypeRegistry.getTypeConverters();
    }

    @Override
    public <Y> Map<Class<?>, TypeConverter<?, Y>> getTypeConverters(Class<Y> viewModelType) {
        return userTypeRegistry.getTypeConverter(viewModelType);
    }

    public MutableBasicUserTypeRegistry getUserTypeRegistry() {
        return userTypeRegistry;
    }

    public MetamodelBootContext getBootContext() {
        return bootContext;
    }

    @Override
    public EntityViewManager createEntityViewManager(CriteriaBuilderFactory cbf) {
        return new EntityViewManagerImpl(this, cbf);
    }

    @Override
    public EntityViewManager createEntityViewManager(CriteriaBuilderFactory cbf, EntityManagerFactory emf) {
        return createEntityViewManager(cbf);
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

    @Override
    public Map<Class<?>, Object> getTypeTestValues() {
        return typeTestValues;
    }

    @Override
    public <T> EntityViewConfiguration setTypeTestValue(Class<T> type, T value) {
        typeTestValues.put(type, value);
        return this;
    }
}
