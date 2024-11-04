/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.metamodel;

import com.blazebit.persistence.view.spi.type.TypeConverter;

/**
 * Represents the mapping type of a view or attribute.
 *
 * @param <X> The type of the view or attribute
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface Type<X> {

    /**
     * Returns the java class of the type.
     *
     * @return the java class
     */
    public Class<X> getJavaType();

    /**
     * The declared type that is converted by the converter, or <code>null</code> if no converter exists.
     *
     * @return The type that is converted
     */
    public java.lang.reflect.Type getConvertedType();

    /**
     * The converter for converting objects between the converted type and the actual entity view model type.
     *
     * @return The type converter
     */
    public TypeConverter<X, ?> getConverter();

    /**
     * Returns the mapping type.
     *
     * @return The mapping type
     */
    public MappingType getMappingType();

    /**
     * The different mapping types.
     */
    public static enum MappingType {

        /**
         * Basic type.
         */
        BASIC,
        /**
         * Flat view type.
         */
        FLAT_VIEW,
        /**
         * View type.
         */
        VIEW;
    }
}
