/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.impl.builder;

import com.blazebit.persistence.deltaspike.data.impl.handler.EntityViewCdiQueryInvocationContext;
import org.apache.deltaspike.data.api.QueryResult;
import org.apache.deltaspike.data.api.mapping.QueryInOutMapper;

import javax.persistence.Query;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.builder.QueryBuilder} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public abstract class EntityViewQueryBuilder {

    @SuppressWarnings("unchecked")
    public Object executeQuery(EntityViewCdiQueryInvocationContext context) {
        Object result = execute(context);
        if (!isUnmappableResult(result) && context.hasQueryInOutMapper()) {
            QueryInOutMapper<Object> mapper = (QueryInOutMapper<Object>)
                    context.getQueryInOutMapper();
            if (result instanceof List) {
                return mapper.mapResultList((List<Object>) result);
            }
            return mapper.mapResult(result);
        }
        return result;
    }

    protected abstract Object execute(EntityViewCdiQueryInvocationContext ctx);

    protected boolean returnsList(Method method) {
        return method.getReturnType().isAssignableFrom(List.class);
    }

    private boolean isUnmappableResult(Object result) {
        return result instanceof QueryResult ||
                result instanceof Query;
    }
}