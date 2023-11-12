/*
 * Copyright 2014 - 2023 Blazebit.
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
import com.blazebit.persistence.view.ConfigurationProperties;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.impl.metamodel.AnnotationMappingReader;
import com.blazebit.persistence.view.impl.metamodel.MetamodelBootContext;
import com.blazebit.persistence.view.impl.metamodel.MetamodelBootContextImpl;
import com.blazebit.persistence.view.impl.metamodel.MappingReader;
import com.blazebit.persistence.view.impl.type.MutableBasicUserTypeRegistry;
import com.blazebit.persistence.view.spi.TransactionSupport;
import com.blazebit.persistence.view.spi.type.BasicUserType;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.spi.EntityViewMapping;
import com.blazebit.persistence.view.spi.type.TypeConverter;

import javax.persistence.EntityManagerFactory;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class EntityViewConfigurationImpl implements EntityViewConfiguration {

    private final MutableBasicUserTypeRegistry userTypeRegistry = new MutableBasicUserTypeRegistry();
    private final MetamodelBootContext bootContext = new MetamodelBootContextImpl();
    private final MappingReader annotationMappingReader = new AnnotationMappingReader(bootContext);
    private final Map<Class<?>, Object> typeTestValues = new HashMap<>();
    private Properties properties = new Properties();
    private TransactionSupport transactionSupport;
    private Map<String, Object> optionalParameters = new HashMap<>();

    public EntityViewConfigurationImpl() {
        loadDefaultProperties();
    }

    private void loadDefaultProperties() {
        properties.put(ConfigurationProperties.PROXY_EAGER_LOADING, "false");
        properties.put(ConfigurationProperties.PROXY_UNSAFE_ALLOWED, "true");
        properties.put(ConfigurationProperties.MANAGED_TYPE_VALIDATION_DISABLED, "false");
        properties.put(ConfigurationProperties.UPDATER_DISALLOW_OWNED_UPDATABLE_SUBVIEW, "true");
        properties.put(ConfigurationProperties.UPDATER_STRICT_CASCADING_CHECK, "true");
        properties.put(ConfigurationProperties.UPDATER_ERROR_ON_INVALID_PLURAL_SETTER, "false");
        properties.put(ConfigurationProperties.STATIC_BUILDER_SCANNING_DISABLED, "false");
        properties.put(ConfigurationProperties.STATIC_IMPLEMENTATION_SCANNING_DISABLED, "false");
        properties.put(ConfigurationProperties.STATIC_METAMODEL_SCANNING_DISABLED, "false");
        properties.put(ConfigurationProperties.CREATE_EMPTY_FLAT_VIEWS, "true");

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
        typeTestValues.put(TimeZone.class, TimeZone.getTimeZone("Universal"));
        typeTestValues.put(byte[].class, new byte[] { Byte.MAX_VALUE });
        typeTestValues.put(Byte[].class, new Byte[] { Byte.MAX_VALUE });
        typeTestValues.put(char[].class, new char[] { Character.MAX_VALUE });
        typeTestValues.put(Character[].class, new Character[] { Character.MAX_VALUE });
        typeTestValues.put(BigInteger.class, BigInteger.TEN);
        typeTestValues.put(BigDecimal.class, BigDecimal.TEN);
        typeTestValues.put(Serializable.class, "-");

        typeTestValues.put(Class.class, EntityViewConfigurationImpl.class);
        typeTestValues.put(Currency.class, Currency.getInstance("XXX"));
        typeTestValues.put(Locale.class, new Locale("", "", ""));
        typeTestValues.put(UUID.class, UUID.randomUUID());
        try {
            typeTestValues.put(URL.class, new URL("https://blazebit.com"));
        } catch (MalformedURLException e) {
            // Ignore
        }

        // Java 8 time types
        try {
            Class<?> localDate = Class.forName("java.time.LocalDate");
            typeTestValues.put(localDate, localDate.getMethod("now").invoke(null));
            Class<?> localTime = Class.forName("java.time.LocalTime");
            typeTestValues.put(localTime, localTime.getMethod("now").invoke(null));
            Class<?> localDateTime = Class.forName("java.time.LocalDateTime");
            typeTestValues.put(localDateTime, localDateTime.getMethod("now").invoke(null));
            Class<?> offsetTime = Class.forName("java.time.OffsetTime");
            typeTestValues.put(offsetTime, offsetTime.getMethod("now").invoke(null));
            Class<?> offsetDateTime = Class.forName("java.time.OffsetDateTime");
            typeTestValues.put(offsetDateTime, offsetDateTime.getMethod("now").invoke(null));
            Class<?> zonedDateTime = Class.forName("java.time.ZonedDateTime");
            typeTestValues.put(zonedDateTime, zonedDateTime.getMethod("now").invoke(null));
            Class<?> duration = Class.forName("java.time.Duration");
            typeTestValues.put(duration, duration.getMethod("ofNanos", long.class).invoke(null, 1L));
            Class<?> instant = Class.forName("java.time.Instant");
            typeTestValues.put(instant, instant.getMethod("now").invoke(null));
            Class<?> monthDay = Class.forName("java.time.MonthDay");
            typeTestValues.put(monthDay, monthDay.getMethod("now").invoke(null));
            Class<?> year = Class.forName("java.time.Year");
            typeTestValues.put(year, year.getMethod("now").invoke(null));
            Class<?> yearMonth = Class.forName("java.time.YearMonth");
            typeTestValues.put(yearMonth, yearMonth.getMethod("now").invoke(null));
            Class<?> period = Class.forName("java.time.Period");
            typeTestValues.put(period, period.getMethod("ofDays", int.class).invoke(null, 1));
            Class<?> zoneId = Class.forName("java.time.ZoneId");
            typeTestValues.put(zoneId, zoneId.getMethod("of", String.class).invoke(null, "Universal"));
            Class<?> zoneOffset = Class.forName("java.time.ZoneOffset");
            typeTestValues.put(zoneOffset, zoneOffset.getMethod("of", String.class).invoke(null, "+17:59:59"));
        } catch (Exception ex) {
            // If they aren't found, we ignore them
        }
    }

    @Override
    public EntityViewConfiguration addEntityView(Class<?> clazz) {
        createEntityViewMapping(clazz);
        return this;
    }

    @Override
    public EntityViewMapping createEntityViewMapping(Class<?> clazz) {
        return annotationMappingReader.readViewMapping(clazz);
    }

    @Override
    public EntityViewConfiguration addEntityViewListener(Class<?> entityViewListenerClass) {
        for (EntityViewListenerFactory<?> viewListenerFactory : bootContext.createViewListenerFactories(entityViewListenerClass)) {
            annotationMappingReader.readViewListenerMapping(entityViewListenerClass, viewListenerFactory);
        }

        return this;
    }

    @Override
    public EntityViewConfiguration addEntityViewListener(Class<?> entityViewClass, Class<?> entityViewListenerClass) {
        bootContext.addEntityViewListener(entityViewClass, entityViewListenerClass);
        return this;
    }

    @Override
    public EntityViewConfiguration addEntityViewListener(Class<?> entityViewClass, Class<?> entityClass, Class<?> entityViewListenerClass) {
        bootContext.addEntityViewListener(entityViewClass, entityClass, entityViewListenerClass);
        return this;
    }

    @Override
    public Set<Class<?>> getEntityViewListeners() {
        return bootContext.getViewListenerClasses();
    }

    @Override
    public Set<Class<?>> getEntityViewListeners(Class<?> entityViewClass) {
        return bootContext.getViewListenerClasses(entityViewClass);
    }

    @Override
    public Set<Class<?>> getEntityViewListeners(Class<?> entityViewClass, Class<?> entityClass) {
        return bootContext.getViewListenerClasses(entityViewClass, entityClass);
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

    @Override
    public TransactionSupport getTransactionSupport() {
        return transactionSupport;
    }

    @Override
    public EntityViewConfiguration setTransactionSupport(TransactionSupport transactionSupport) {
        this.transactionSupport = transactionSupport;
        return this;
    }

    @Override
    public Map<String, Object> getOptionalParameters() {
        return optionalParameters;
    }

    @Override
    public Object getOptionalParameter(String name) {
        return optionalParameters.get(name);
    }

    @Override
    public EntityViewConfiguration setOptionalParameter(String name, Object value) {
        optionalParameters.put(name, value);
        return this;
    }

    @Override
    public EntityViewConfiguration setOptionalParameters(Map<String, Object> optionalParameters) {
        this.optionalParameters = optionalParameters;
        return this;
    }

    @Override
    public EntityViewConfiguration addOptionalParameters(Map<String, Object> optionalParameters) {
        this.optionalParameters.putAll(optionalParameters);
        return this;
    }
}
