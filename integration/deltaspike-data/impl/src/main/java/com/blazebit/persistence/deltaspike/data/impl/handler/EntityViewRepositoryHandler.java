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

import com.blazebit.persistence.deltaspike.data.api.EntityViewRepository;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.impl.util.JpaMetamodelUtils;
import com.blazebit.persistence.view.EntityViewSetting;
import org.apache.deltaspike.data.impl.property.Property;
import org.apache.deltaspike.data.impl.property.query.NamedPropertyCriteria;
import org.apache.deltaspike.data.impl.property.query.PropertyQueries;

import javax.inject.Inject;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.apache.deltaspike.core.util.ArraysUtils.isEmpty;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class EntityViewRepositoryHandler<E, V, PK extends Serializable>
        implements EntityViewRepository<E, V, PK>, EntityViewDelegateQueryHandler {

    @Inject
    @EntityViewContext
    private EntityViewCdiQueryInvocationContext context;

    @Inject
    private CriteriaBuilderFactory cbf;

    @Override
    public V findBy(PK pk) {
        CriteriaBuilder<E> cb = createCriteriaBuilder();

        List<V> result = context.getEntityViewManager().applySetting(
                createSetting(),
                cb.where(idAttribute()).eq(pk)
        ).getResultList();

        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public List<V> findAll() {
        return context.getEntityViewManager().applySetting(
                createSetting(),
                createCriteriaBuilder()
        ).getResultList();
    }

    @Override
    public List<V> findAll(int start, int max) {
        EntityViewSetting<V, PaginatedCriteriaBuilder<V>> setting = EntityViewSetting.create(viewClass(), start, max);
        return context.getEntityViewManager().applySetting(setting, createCriteriaBuilder())
                .orderByAsc(idAttribute())
                .getResultList();
    }

    @Override
    public List<V> findBy(E e, SingularAttribute<E, ?>... singularAttributes) {
        return executeExampleQuery(e, 0, 0, false, singularAttributes);
    }

    @Override
    public List<V> findBy(E e, int start, int max, SingularAttribute<E, ?>... singularAttributes) {
        return executeExampleQuery(e, start, max, false, singularAttributes);
    }

    @Override
    public List<V> findByLike(E e, SingularAttribute<E, ?>... singularAttributes) {
        return executeExampleQuery(e, 0, 0, true, singularAttributes);
    }

    @Override
    public List<V> findByLike(E e, int start, int max, SingularAttribute<E, ?>... singularAttributes) {
        return executeExampleQuery(e, start, max, true, singularAttributes);
    }

    private CriteriaBuilder<E> createCriteriaBuilder() {
        return cbf.create(context.getEntityManager(), entityClass());
    }

    private Class<V> viewClass() {
        return (Class<V>) context.getEntityViewClass();
    }

    private Class<E> entityClass() {
        return (Class<E>) context.getEntityClass();
    }

    private EntityViewSetting<V, CriteriaBuilder<V>> createSetting() {
        return EntityViewSetting.create((Class<V>) context.getRepositoryMethod().getEntityViewClass());
    }

    private List<String> extractPropertyNames(SingularAttribute<E, ?>... attributes) {
        List<String> result = new ArrayList<String>(attributes.length);
        for (SingularAttribute<E, ?> attribute : attributes) {
            result.add(attribute.getName());
        }
        return result;
    }

    private List<Property<Object>> extractProperties(SingularAttribute<E, ?>... attributes) {
        List<String> names = extractPropertyNames(attributes);
        List<Property<Object>> properties = PropertyQueries.createQuery(context.getEntityClass())
                .addCriteria(new NamedPropertyCriteria(names.toArray(new String[]{}))).getResultList();
        return properties;
    }

    private void prepareWhere(CriteriaBuilder<?> cb, E example, List<Property<Object>> properties, boolean useLikeOperator) {
        Iterator<Property<Object>> iterator = properties.iterator();
        while (iterator.hasNext()) {
            Property<Object> property = iterator.next();
            String name = property.getName();
            if (useLikeOperator && property.getJavaClass().getName().equals(String.class.getName())) {
                cb.where(name).like(false).value(property.getValue(example)).noEscape();
            } else {
                cb.where(name).eq(property.getValue(example));
            }
        }
    }

    private List<V> executeExampleQuery(E example, int start, int max, boolean useLikeOperator,
                                        SingularAttribute<E, ?>... attributes) {
        if (isEmpty(attributes)) {
            return findAll();
        }

        CriteriaBuilder<?> cb = createCriteriaBuilder();
        List<Property<Object>> properties = extractProperties(attributes);
        prepareWhere(cb, example, properties, useLikeOperator);

        EntityViewSetting<V, ?> setting;
        if (start > 0 || max > 0) {
            setting = EntityViewSetting.create(viewClass(), start, max);
        } else {
            setting = EntityViewSetting.create(viewClass());
        }

        return context.getEntityViewManager().applySetting(setting, cb)
                .orderByAsc(idAttribute())
                .getResultList();
    }

    private String idAttribute() {
        Class<?> entityClass = context.getEntityViewManager().getMetamodel().view(viewClass()).getEntityClass();
        EntityType<?> entityType = context.getEntityManager().getMetamodel().entity(entityClass);
        return JpaMetamodelUtils.getIdAttribute(entityType).getName();
    }
}