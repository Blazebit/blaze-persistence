/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.impl.builder;

import com.blazebit.persistence.deltaspike.data.impl.handler.EntityViewCdiQueryInvocationContext;
import org.apache.deltaspike.data.api.QueryResult;
import org.apache.deltaspike.data.impl.builder.QueryBuilder;
import org.apache.deltaspike.data.impl.meta.RepositoryMethodMetadata;
import org.apache.deltaspike.data.impl.meta.RepositoryMethodType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.builder.QueryBuilderFactory} but was modified to
 * work with entity views.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@ApplicationScoped
public class EntityViewAwareQueryBuilderFactory {

    @Inject
    private EntityViewMethodQueryBuilder methodQueryBuilder;
    @Inject
    private EntityViewDelegateQueryBuilder delegateQueryBuilder;
    @Inject
    private EntityViewAnnotatedQueryBuilder annotatedQueryBuilder;

    protected EntityViewQueryBuilder getEntityViewQueryBuilder(RepositoryMethodType repositoryMethodType) {
        switch (repositoryMethodType) {
            case ANNOTATED:
                return annotatedQueryBuilder;
            case PARSE:
                return methodQueryBuilder;
            case DELEGATE:
                return delegateQueryBuilder;
            default:
                throw new RuntimeException(
                        "No " + QueryBuilder.class.getName() + " available for type: " + repositoryMethodType);
        }
    }

    public EntityViewQueryBuilder build(RepositoryMethodMetadata methodMetadata, EntityViewCdiQueryInvocationContext context) {
        EntityViewQueryBuilder builder = getEntityViewQueryBuilder(context.getRepositoryMethodMetadata().getMethodType());
        if (QueryResult.class.equals(methodMetadata.getMethod().getReturnType())) {
            return new WrappedEntityViewQueryBuilder(builder);
        }
        return builder;
    }
}