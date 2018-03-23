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