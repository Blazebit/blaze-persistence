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