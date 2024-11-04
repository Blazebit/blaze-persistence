/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.impl.builder;

import com.blazebit.persistence.deltaspike.data.impl.handler.EntityViewCdiQueryInvocationContext;
import com.blazebit.persistence.deltaspike.data.impl.meta.EntityViewRepositoryMethod;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.data.api.QueryResult;
import org.apache.deltaspike.data.impl.meta.MethodType;
import org.apache.deltaspike.data.impl.meta.QueryInvocationLiteral;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;

import static org.apache.deltaspike.data.impl.meta.MethodType.ANNOTATED;
import static org.apache.deltaspike.data.impl.meta.MethodType.DELEGATE;
import static org.apache.deltaspike.data.impl.meta.MethodType.PARSE;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.builder.QueryBuilderFactory} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
@ApplicationScoped
public class EntityViewAwareQueryBuilderFactory {

    private static final Map<MethodType, QueryInvocationLiteral> LITERALS =
        new HashMap<MethodType, QueryInvocationLiteral>() {
            private static final long serialVersionUID = 1L;

            {
                put(ANNOTATED, new QueryInvocationLiteral(ANNOTATED));
                put(DELEGATE, new QueryInvocationLiteral(DELEGATE));
                put(PARSE, new QueryInvocationLiteral(PARSE));
            }
        };

    public EntityViewQueryBuilder build(EntityViewRepositoryMethod method, EntityViewCdiQueryInvocationContext context) {
        EntityViewQueryBuilder builder = BeanProvider.getContextualReference(EntityViewQueryBuilder.class, LITERALS.get(method.getMethodType()));
        if (method.returns(QueryResult.class)) {
            return new WrappedEntityViewQueryBuilder(builder);
        }
        return builder;
    }
}