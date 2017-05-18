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

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.criteria.BlazeCriteriaBuilder;
import com.blazebit.persistence.criteria.BlazeCriteriaQuery;
import com.blazebit.persistence.criteria.BlazeCriteria;
import com.blazebit.persistence.deltaspike.data.Pageable;
import com.blazebit.persistence.deltaspike.data.Specification;
import com.blazebit.persistence.deltaspike.data.base.builder.QueryBuilderUtils;
import com.blazebit.persistence.deltaspike.data.impl.builder.part.EntityViewQueryRoot;
import com.blazebit.persistence.deltaspike.data.impl.handler.EntityViewCdiQueryInvocationContext;
import com.blazebit.persistence.deltaspike.data.impl.param.ExtendedParameters;
import org.apache.deltaspike.data.impl.meta.MethodType;
import org.apache.deltaspike.data.impl.meta.QueryInvocation;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.Query;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.builder.MethodQueryBuilder} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
@QueryInvocation(MethodType.PARSE)
@ApplicationScoped
public class EntityViewMethodQueryBuilder extends EntityViewQueryBuilder {

    @Override
    public Object execute(EntityViewCdiQueryInvocationContext context) {
        Query jpaQuery = createJpaQuery(context);
        return context.executeQuery(jpaQuery);
    }

    private <V> Query createJpaQuery(EntityViewCdiQueryInvocationContext context) {
        ExtendedParameters params = context.getParams();
        EntityViewQueryRoot root = context.getRepositoryMethod().getEntityViewQueryRoot();
        Pageable pageable = params.getPageable();
        CriteriaBuilder<?> cb;

        Specification<?> specification = params.getSpecification();
        if (specification == null) {
            cb = context.getCriteriaBuilderFactory().create(context.getEntityManager(), context.getEntityClass());
            root.apply(cb);
        } else {
            BlazeCriteriaBuilder blazeCriteriaBuilder = BlazeCriteria.get(context.getCriteriaBuilderFactory());
            BlazeCriteriaQuery<?> query = blazeCriteriaBuilder.createQuery(context.getEntityClass());
            Root queryRoot = query.from(context.getEntityClass());
            root.apply(queryRoot, query, blazeCriteriaBuilder);
            Predicate predicate = specification.toPredicate(queryRoot, query, blazeCriteriaBuilder);
            if (predicate != null) {
                if (query.getRestriction() == null) {
                    query.where(predicate);
                } else {
                    query.where(query.getRestriction(), predicate);
                }
            }
            cb = query.createCriteriaBuilder(context.getEntityManager());
        }

        Class<V> entityViewClass = (Class<V>) context.getEntityViewClass();
        boolean keysetExtraction = false; // TODO: depending on return type we might need to do keyset extraction
        FullQueryBuilder<? ,?> fullCb = QueryBuilderUtils.getFullQueryBuilder(cb, pageable, context.getEntityViewManager(), entityViewClass, keysetExtraction);

        fullCb = context.applyCriteriaBuilderPostProcessors(fullCb);
        Query q = params.applyTo(context.createQuery(fullCb));
        return context.applyRestrictions(q);
    }
}