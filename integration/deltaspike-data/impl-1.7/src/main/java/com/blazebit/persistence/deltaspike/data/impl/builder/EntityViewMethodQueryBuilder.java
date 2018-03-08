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
import com.blazebit.persistence.deltaspike.data.impl.builder.part.EntityViewQueryRoot;
import com.blazebit.persistence.deltaspike.data.impl.handler.EntityViewCdiQueryInvocationContext;
import com.blazebit.persistence.deltaspike.data.impl.param.Parameters;
import com.blazebit.persistence.view.EntityViewSetting;
import org.apache.deltaspike.data.impl.meta.MethodType;
import org.apache.deltaspike.data.impl.meta.QueryInvocation;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.Query;

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

    private Query createJpaQuery(EntityViewCdiQueryInvocationContext context) {
        Parameters params = context.getParams();
        EntityViewQueryRoot root = context.getRepositoryMethod().getEntityViewQueryRoot();
        CriteriaBuilder<?> cb = context.getCriteriaBuilderFactory().create(context.getEntityManager(), context.getEntityClass());
        root.apply(cb);

        cb = context.getEntityViewManager().applySetting(
                EntityViewSetting.create(context.getEntityViewClass()),
                cb
        );
        FullQueryBuilder<? ,?> fullCb;
        if (params.hasFirstResult() || params.hasSizeRestriction()) {
            int firstResult = params.hasFirstResult() ? params.getFirstResult() : 0;
            int maxResults = params.hasSizeRestriction() ? params.getSizeRestriciton() : Integer.MAX_VALUE;
            fullCb = cb.page(firstResult, maxResults);
        } else {
            fullCb = cb;
        }

        fullCb = context.applyCriteriaBuilderPostProcessors(fullCb);
        fullCb = params.applyTo(fullCb);
        return context.applyRestrictions(fullCb.getQuery());
    }
}