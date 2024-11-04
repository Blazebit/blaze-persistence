/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.base.builder.part;

import org.apache.deltaspike.data.impl.builder.MethodExpressionException;
import org.apache.deltaspike.data.impl.property.Property;
import org.apache.deltaspike.data.impl.property.query.NamedPropertyCriteria;
import org.apache.deltaspike.data.impl.property.query.PropertyQueries;
import org.apache.deltaspike.data.impl.property.query.PropertyQuery;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.builder.part.BasePropertyQueryPart} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public abstract class EntityViewBasePropertyQueryPart extends EntityViewQueryPart {
    static final String SEPARATOR = "_";

    void validate(String name, String method , Class<?> repositoryClass, Class<?> entityClass) {
        Class<?> current = entityClass;
        if (name == null) {
            throw new MethodExpressionException(null, repositoryClass, method);
        }
        for (String property : name.split(SEPARATOR)) {
            PropertyQuery<?> query = PropertyQueries.createQuery(current)
                    .addCriteria(new NamedPropertyCriteria(property));
            Property<?> result = query.getFirstResult();
            if (result == null) {
                throw new MethodExpressionException(property, repositoryClass, method);
            }
            current = result.getJavaClass();
        }
    }

    static String rewriteSeparator(String name) {
        if (name.contains("_")) {
            return name.replaceAll(SEPARATOR, ".");
        }
        return name;
    }
}