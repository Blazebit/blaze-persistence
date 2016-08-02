package com.blazebit.persistence.criteria.impl.expression;

import java.io.Serializable;

import javax.persistence.TupleElement;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.TypeConverter;
import com.blazebit.persistence.criteria.impl.TypeUtils;

/**
 *
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
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
