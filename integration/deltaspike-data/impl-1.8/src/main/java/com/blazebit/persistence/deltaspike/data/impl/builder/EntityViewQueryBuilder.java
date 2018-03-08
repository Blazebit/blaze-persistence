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
import org.apache.deltaspike.data.api.mapping.QueryInOutMapper;

import javax.persistence.Query;
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

    private boolean isUnmappableResult(Object result) {
        return result instanceof QueryResult ||
                result instanceof Query;
    }
}