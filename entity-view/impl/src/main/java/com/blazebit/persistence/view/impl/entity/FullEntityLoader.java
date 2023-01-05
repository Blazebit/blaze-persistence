/*
 * Copyright 2014 - 2023 Blazebit.
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

package com.blazebit.persistence.view.impl.entity;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.metamodel.Attribute;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MappingAttribute;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.metamodel.EntityType;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class FullEntityLoader extends AbstractEntityLoader {

    private final String queryString;
    private final String queryStringMultiple;

    public FullEntityLoader(EntityViewManagerImpl evm, ManagedViewType<?> subviewType) {
        // ViewIdMapper is not necessary because this will always be invoked with a null id
        super(evm, subviewType.getEntityClass(), jpaIdOf(evm, subviewType), viewIdMappingOf(evm, subviewType), null, evm.getEntityIdAccessor());
        this.queryString = createQueryString(evm, subviewType, false);
        this.queryStringMultiple = createQueryString(evm, subviewType, true);
    }

    private String createQueryString(EntityViewManagerImpl evm, ManagedViewType<?> subviewType, boolean multiple) {
        EntityMetamodel entityMetamodel = evm.getMetamodel().getEntityMetamodel();
        EntityType<?> entityType = entityMetamodel.getEntity(entityClass);

        // We can't query embeddables
        if (entityType == null) {
            return null;
        }

        Set<String> fetchJoinableRelations = new HashSet<>(subviewType.getAttributes().size());

        addFetchJoinableRelations(fetchJoinableRelations, "", subviewType);

        CriteriaBuilderFactory cbf = evm.getCriteriaBuilderFactory();
        if (fetchJoinableRelations.isEmpty()) {
            return null;
        } else {
            CriteriaBuilder<?> criteriaBuilder = cbf.create(null, entityClass).fetch(fetchJoinableRelations.toArray(new String[fetchJoinableRelations.size()]));
            if (multiple) {
                criteriaBuilder.where(JpaMetamodelUtils.getSingleIdAttribute(entityType).getName()).inExpressions(":entityIds");
            } else {
                criteriaBuilder.where(JpaMetamodelUtils.getSingleIdAttribute(entityType).getName()).eqExpression(":id");
            }
            return criteriaBuilder.getQueryString();
        }
    }

    private void addFetchJoinableRelations(Set<String> fetchJoinableRelations, String prefix, ManagedViewType<?> subviewType) {
        @SuppressWarnings("unchecked")
        Set<MethodAttribute<?, ?>> attributes = (Set<MethodAttribute<?, ?>>) (Set) subviewType.getAttributes();
        for (MethodAttribute<?, ?> attribute : attributes) {
            if (attribute.getMappingType() == Attribute.MappingType.BASIC) {
                if (attribute.isUpdatable()) {
                    String mapping = getMapping(prefix, attribute);
                    fetchJoinableRelations.add(mapping);
                }
                if (attribute.isMutable() && attribute.isSubview()) {
                    String mapping = getMapping(prefix, attribute);
                    fetchJoinableRelations.add(mapping);
                    ManagedViewType<?> type;
                    if (attribute instanceof SingularAttribute<?, ?>) {
                        type = (ManagedViewType<?>) ((SingularAttribute<?, ?>) attribute).getType();
                    } else if (attribute instanceof PluralAttribute<?, ?, ?>) {
                        type = (ManagedViewType<?>) ((PluralAttribute<?, ?, ?>) attribute).getElementType();
                    } else {
                        throw new RuntimeException("Unknown attribute type: " + attribute);
                    }
                    addFetchJoinableRelations(fetchJoinableRelations, mapping + ".", type);
                }
            }
        }
    }

    private String getMapping(String prefix, MethodAttribute<?, ?> attribute) {
        // Remove all array expressions
        String mapping = ((MappingAttribute<?, ?>) attribute).getMapping();
        return prefix + mapping.replaceAll("\\[[^\\]]\\]", "");
    }

    @Override
    public Object toEntity(UpdateContext context, Object view, Object id) {
        if (id == null || entityIdAccessor == null) {
            return createEntity();
        }

        return getReferenceOrLoad(context, view, id);
    }

    @Override
    public void toEntities(UpdateContext context, List<Object> views, List<Object> ids) {
        if (entityIdAccessor == null) {
            for (int i = 0; i < views.size(); i++) {
                views.set(i, createEntity());
            }
        } else {
            getReferencesLoadOrCreate(context, views, ids);
        }
    }

    @Override
    protected Object queryEntity(EntityManager em, Object id) {
        if (queryString == null) {
            return em.find(entityClass, id);
        }

        @SuppressWarnings("unchecked")
        List<Object> list = em.createQuery(queryString)
                .setParameter("id", id)
                .getResultList();
        if (list.isEmpty()) {
            throw new EntityNotFoundException("Required entity '" + entityClass.getName() + "' with id '" + id + "' couldn't be found!");
        }

        return list.get(0);
    }

    @Override
    protected List<Object> queryEntities(EntityManager em, List<Object> ids) {
        List<Object> list = em.createQuery(queryStringMultiple)
            .setParameter("entityIds", ids)
            .getResultList();
        if (list.size() != ids.size()) {
            throw new EntityNotFoundException("Required entities '" + entityClass.getName() + "' with ids '" + ids + "' couldn't all be found!");
        }

        return list;
    }
}
