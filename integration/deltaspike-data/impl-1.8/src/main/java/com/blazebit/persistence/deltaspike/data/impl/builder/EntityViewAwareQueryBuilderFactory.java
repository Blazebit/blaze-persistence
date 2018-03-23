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