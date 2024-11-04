/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl.expression;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.parser.util.TypeConverter;
import com.blazebit.persistence.parser.util.TypeUtils;

import javax.persistence.TupleElement;
import java.io.Serializable;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class AbstractTupleElement<X> implements TupleElement<X>, Serializable {

    private static final long serialVersionUID = 1L;

    protected final BlazeCriteriaBuilderImpl criteriaBuilder;
    private Class<X> javaType;
    private String alias;
    private TypeConverter<X> converter;

    protected AbstractTupleElement(BlazeCriteriaBuilderImpl criteriaBuilder, Class<X> javaType) {
        this.criteriaBuilder = criteriaBuilder;
        this.javaType = javaType;
    }

    @Override
    public Class<X> getJavaType() {
        return javaType;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void setJavaType(Class targetType) {
        this.javaType = targetType;
        this.converter = TypeUtils.getConverter(javaType, criteriaBuilder.getEntityMetamodel().getEnumTypes().keySet());
    }

    protected void setConverter(TypeConverter<X> converter) {
        this.converter = converter;
    }

    public TypeConverter<X> getConverter() {
        return converter;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    protected void setAlias(String alias) {
        this.alias = alias;
    }
}
