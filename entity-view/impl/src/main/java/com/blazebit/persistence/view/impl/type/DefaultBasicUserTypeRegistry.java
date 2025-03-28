/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.view.spi.type.BasicUserType;
import com.blazebit.persistence.view.spi.type.MutableBasicUserType;
import com.blazebit.persistence.view.spi.type.TypeConverter;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class DefaultBasicUserTypeRegistry implements BasicUserTypeRegistry {

    private final Map<Class<?>, BasicUserType<?>> basicUserTypes;
    private final Map<Class<?>, Map<Class<?>, TypeConverter<?, ?>>> converters;
    private final EntityMetamodel entityMetamodel;
    private final BasicUserType<Object> entityBasicUserType;

    public DefaultBasicUserTypeRegistry(BasicUserTypeRegistry original, CriteriaBuilderFactory cbf) {
        this.entityMetamodel = cbf.getService(EntityMetamodel.class);
        this.entityBasicUserType = new EntityBasicUserType(cbf.getService(JpaProvider.class));
        Map<Class<?>, BasicUserType<?>> basicUserTypes = new HashMap<>(original.getBasicUserTypes());
        Map<Class<?>, Map<Class<?>, TypeConverter<?, ?>>> converters = new HashMap<>(original.getTypeConverters());

        handleLobType(basicUserTypes, converters, Blob.class, BlobBasicUserType.INSTANCE, BlobTypeConverter.INSTANCE);
        handleLobType(basicUserTypes, converters, Clob.class, ClobBasicUserType.INSTANCE, ClobTypeConverter.INSTANCE);
        handleLobType(basicUserTypes, converters, NClob.class, NClobBasicUserType.INSTANCE, NClobTypeConverter.INSTANCE);

        // Make nested maps unmodifiable
        for (Map.Entry<Class<?>, Map<Class<?>, TypeConverter<?, ?>>> entry : converters.entrySet()) {
            entry.setValue(Collections.unmodifiableMap(entry.getValue()));
        }

        this.basicUserTypes = Collections.unmodifiableMap(basicUserTypes);
        this.converters = Collections.unmodifiableMap(converters);
    }

    private void handleLobType(Map<Class<?>, BasicUserType<?>> basicUserTypes, Map<Class<?>, Map<Class<?>, TypeConverter<?, ?>>> converters, Class<?> lobType, BasicUserType<?> expectedType, TypeConverter<?, ?> expectedConverter) {
        if (basicUserTypes.get(lobType) == expectedType) {
            // Remove the type if the converter isn't the expected wrapping converter
            Map<Class<?>, TypeConverter<?, ?>> converterMap = converters.get(lobType);
            if (converterMap == null || converterMap.get(lobType) != expectedConverter) {
                basicUserTypes.remove(lobType);
            }
        } else {
            // Remove the wrapping converter if the user has a custom type provided
            Map<Class<?>, TypeConverter<?, ?>> converterMap = converters.get(lobType);
            if (converterMap != null) {
                converterMap.remove(lobType);
                if (converterMap.isEmpty()) {
                    converters.remove(lobType);
                }
            }
        }
    }

    @Override
    public <X> void registerBasicUserType(Class<X> clazz, BasicUserType<X> userType) {
        throw new IllegalArgumentException("Can't register type after building the configuration!");
    }

    @Override
    public <X, Y> void registerTypeConverter(Class<X> entityModelType, Class<Y> viewModelType, TypeConverter<X, Y> converter) {
        throw new IllegalArgumentException("Can't register type converter after building the configuration!");
    }

    @Override
    public Map<Class<?>, Map<Class<?>, TypeConverter<?, ?>>> getTypeConverters() {
        return converters;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Y> Map<Class<?>, TypeConverter<?, Y>> getTypeConverter(Class<Y> clazz) {
        Map<Class<?>, TypeConverter<?, Y>> map = (Map<Class<?>, TypeConverter<?, Y>>) (Map<?, ?>) converters.get(clazz);
        if (map == null) {
            return Collections.emptyMap();
        }
        return map;
    }

    @Override
    public Map<Class<?>, BasicUserType<?>> getBasicUserTypes() {
        return basicUserTypes;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <X> BasicUserType<X> getBasicUserType(Class<X> clazz) {
        BasicUserType<?> userType = basicUserTypes.get(clazz);
        if (userType == null) {
            if (clazz.isEnum()) {
                //noinspection rawtypes
                userType = new EnumBasicUserType<>((Class<Enum>) clazz);
            } else if (entityMetamodel.getEntity(clazz) != null) {
                userType = entityBasicUserType;
            } else if (java.sql.Date.class == clazz) {
                userType = JavaSqlDateBasicUserType.INSTANCE;
            } else if (Time.class == clazz) {
                userType = TimeBasicUserType.INSTANCE;
            } else if (Timestamp.class == clazz) {
                userType = TimestampBasicUserType.INSTANCE;
            } else if (Date.class.isAssignableFrom(clazz)) {
                userType = DateBasicUserType.INSTANCE;
            } else if (Calendar.class.isAssignableFrom(clazz)) {
                userType = CalendarBasicUserType.INSTANCE;
            } else {
                userType = MutableBasicUserType.INSTANCE;
            }
        }

        return (BasicUserType<X>) userType;
    }
}
