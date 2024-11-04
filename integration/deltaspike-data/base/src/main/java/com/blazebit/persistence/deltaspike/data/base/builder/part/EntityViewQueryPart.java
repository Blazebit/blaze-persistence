/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.base.builder.part;

import com.blazebit.persistence.deltaspike.data.Specification;
import com.blazebit.persistence.deltaspike.data.base.builder.EntityViewQueryBuilderContext;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.builder.part.QueryPart} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public abstract class EntityViewQueryPart {
    protected List<EntityViewQueryPart> children = new LinkedList<>();

    public abstract EntityViewQueryPart build(String queryPart, String method, Class<?> repositoryClass, Class<?> entityClass);

    protected abstract EntityViewQueryPart buildQuery(EntityViewQueryBuilderContext ctx);

    protected abstract Specification<?> buildSpecification(EntityViewQueryBuilderContext ctx);

    protected void buildQueryForChildren(EntityViewQueryBuilderContext ctx) {
        for (EntityViewQueryPart child : children) {
            child.buildQuery(ctx);
        }
    }

    protected <T> List<Specification<T>> buildSpecificationsForChildren(EntityViewQueryBuilderContext ctx) {
        List<Specification<T>> specifications = new ArrayList<>(children.size());
        for (EntityViewQueryPart child : children) {
            Specification<T> specification = (Specification<T>) child.buildSpecification(ctx);
            if (specification != null) {
                specifications.add(specification);
            }
        }
        return specifications;
    }
}