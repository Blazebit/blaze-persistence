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

package com.blazebit.persistence.view.metamodel;

import com.blazebit.persistence.view.FetchStrategy;

/**
 * Represents an attribute of a view type.
 *
 * @param <X> The type of the declaring entity view
 * @param <Y> The type of attribute
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface Attribute<X, Y> {

    /**
     * Returns the declaring view type.
     *
     * @return The declaring view type
     */
    public ManagedViewType<X> getDeclaringType();

    /**
     * Returns the java type of the attribute.
     *
     * @return The java type of the attribute
     */
    public Class<Y> getJavaType();

    /**
     * Returns the java type of the converted attribute type or the attribute type.
     *
     * @return The java type of the converted attribute type or the attribute type
     * @since 1.2.0
     */
    public Class<?> getConvertedJavaType();

    /**
     * Returns the type of the attribute member.
     *
     * @return The type of the attribute member.
     * @since 1.2.0
     */
    public MemberType getMemberType();

    /**
     * Returns the type of the attribute.
     *
     * @return The type of the attribute
     * @since 1.2.0
     */
    public AttributeType getAttributeType();

    /**
     * Returns the type of the attribute mapping.
     *
     * @return The type of the attribute mapping
     * @since 1.2.0
     */
    public MappingType getMappingType();

    /**
     * Returns true if this attribute maps to a subquery provider, otherwise false.
     *
     * @return True if this attribute maps to a subquery provider, otherwise false
     */
    public boolean isSubquery();

    /**
     * Returns true if this attribute is a collection, otherwise false.
     *
     * @return True if this attribute is a collection, otherwise false
     */
    public boolean isCollection();

    /**
     * Returns true if this attribute is a subview, otherwise false.
     *
     * @return True if this attribute is a subview, otherwise false
     */
    public boolean isSubview();

    /**
     * Returns true if this attribute is correlated, otherwise false.
     *
     * @return True if this attribute is correlated, otherwise false
     */
    public boolean isCorrelated();

    /**
     * The associations that should be fetched along with the entity mapped by this attribute.
     *
     * @return The association that should be fetched
     * @since 1.2.0
     */
    public String[] getFetches();

    /**
     * Returns the fetch strategy of the attribute.
     *
     * @return The fetch strategy of the attribute
     * @since 1.2.0
     */
    public FetchStrategy getFetchStrategy();

    /**
     * Returns the default batch size of the attribute.
     * If no default batch size is configured, returns -1.
     *
     * @return The default batch size of the attribute
     * @since 1.2.0
     */
    public int getBatchSize();

    /**
     * The different attribute types.
     * @since 1.2.0
     */
    public static enum AttributeType {

        /**
         * Singular attribute type.
         */
        SINGULAR,
        /**
         * Plural attribute type.
         */
        PLURAL;
    }

    /**
     * The different attribute types.
     * @since 1.2.0
     */
    public static enum MemberType {

        /**
         * Method member type.
         */
        METHOD,
        /**
         * Parameter member type.
         */
        PARAMETER;
    }

    /**
     * The different attribute mapping types.
     * @since 1.2.0
     */
    public static enum MappingType {

        /**
         * Basic attribute mapping type.
         */
        BASIC,
        /**
         * Subquery attribute mapping type.
         */
        SUBQUERY,
        /**
         * Parameter attribute mapping type.
         */
        PARAMETER,
        /**
         * Correlated attribute mapping type.
         */
        CORRELATED;
    }
}
