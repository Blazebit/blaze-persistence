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
        this.converter = TypeUtils.getConverter(javaType);
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
