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

package com.blazebit.persistence.deltaspike.data.impl.handler;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.deltaspike.data.base.handler.AbstractEntityViewAwareRepositoryHandler;
import com.blazebit.persistence.deltaspike.data.base.handler.EntityViewContext;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.view.EntityViewSetting;

import javax.inject.Inject;
import javax.persistence.metamodel.EntityType;
import java.io.Serializable;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class EntityViewRepositoryHandler<E, V, PK extends Serializable> extends AbstractEntityViewAwareRepositoryHandler<E, V, PK> {

    @Inject
    @EntityViewContext
    private EntityViewCdiQueryInvocationContext context;

    @Override
    protected  <T, Q extends FullQueryBuilder<T, Q>> Q applySetting(EntityViewSetting<T, Q> setting, CriteriaBuilder<?> criteriaBuilder) {
        return context.getEntityViewManager().applySetting(setting, criteriaBuilder);
    }

    @Override
    protected CriteriaBuilder<E> createCriteriaBuilder() {
        return cbf.create(context.getEntityManager(), entityClass());
    }

    @Override
    protected Class<V> viewClass() {
        return (Class<V>) context.getEntityViewClass();
    }

    @Override
    protected Class<E> entityClass() {
        return (Class<E>) context.getEntityClass();
    }

    @Override
    protected String idAttribute() {
        Class<?> entityClass = context.getEntityViewManager().getMetamodel().view(viewClass()).getEntityClass();
        EntityType<?> entityType = context.getEntityManager().getMetamodel().entity(entityClass);
        return JpaMetamodelUtils.getIdAttribute(entityType).getName();
    }
}