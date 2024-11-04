/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.metamodel;

import com.blazebit.persistence.LimitBuilder;
import com.blazebit.persistence.OrderByBuilder;
import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.view.FetchStrategy;

import java.util.List;
import java.util.Map;

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
     * Returns the order by items for the limit expression.
     *
     * @return The order by items for the limit expression
     * @since 1.5.0
     */
    public List<OrderByItem> getOrderByItems();

    /**
     * Returns the limit expression.
     *
     * @return The limit expression
     * @since 1.5.0
     */
    public String getLimitExpression();

    /**
     * Returns the offset expression.
     *
     * @return The offset expression
     * @since 1.5.0
     */
    public String getOffsetExpression();

    /**
     * Renders the limit mapping for the given parent expression to the given query builder.
     *
     * @param parent The parent expression
     * @param parameterHolder The parameter holder
     * @param optionalParameters The optional parameters
     * @param builder The query builder
     * @param <T> The query builder type
     * @since 1.6.0
     */
    public <T extends LimitBuilder<?> & OrderByBuilder<?>> void renderLimit(String parent, ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, T builder);

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
