package com.blazebit.persistence.criteria.impl;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface TypeConverter<T> {

    public T convert(Object value);

    public String toString(T value);
}