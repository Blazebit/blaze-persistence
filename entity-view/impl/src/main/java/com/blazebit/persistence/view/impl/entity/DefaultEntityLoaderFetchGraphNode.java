/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.entity;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.update.flush.FetchGraphNode;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.metamodel.EntityType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class DefaultEntityLoaderFetchGraphNode extends AbstractEntityLoader implements EntityLoaderFetchGraphNode<DefaultEntityLoaderFetchGraphNode> {

    private static final String[] EMPTY = new String[0];

    private final String attributeName;
    private final Map<String, Map<?, ?>> fetchGraph;
    private final String queryString;
    private final String queryStringMultiple;

    public DefaultEntityLoaderFetchGraphNode(EntityViewManagerImpl evm, String attributeName, EntityType<?> entityType, Map<String, Map<?, ?>> fetchGraph) {
        // ViewIdMapper is not necessary because this is only for entity types
        super(evm, entityType.getJavaType(), JpaMetamodelUtils.getSingleIdAttribute(entityType), null, null, evm.getEntityIdAccessor());
        this.attributeName = attributeName;
        this.fetchGraph = fetchGraph;
        this.queryString = createQueryString(evm, entityType, fetchGraph, false);
        this.queryStringMultiple = createQueryString(evm, entityType, fetchGraph, true);
    }

    @Override
    public String getAttributeName() {
        return attributeName;
    }

    @Override
    public String getMapping() {
        return attributeName;
    }

    @Override
    public void appendFetchJoinQueryFragment(String base, StringBuilder sb) {
        if (fetchGraph != null) {
            applyFetchGraph(sb, base, fetchGraph);
        }
    }

    @Override
    public FetchGraphNode<?> mergeWith(List<DefaultEntityLoaderFetchGraphNode> fetchGraphNodes) {
        for (int i = 0; i < fetchGraphNodes.size(); i++) {
            DefaultEntityLoaderFetchGraphNode flusher = fetchGraphNodes.get(i);
            if (flusher.fetchGraph != this.fetchGraph) {
                if (this.fetchGraph == null) {
                    return flusher;
                } else {
                    return this;
                }
            }
        }

        return this;
    }

    @SuppressWarnings("unchecked")
    private void applyFetchGraph(StringBuilder sb, String base, Map<String, Map<?, ?>> fetchGraph) {
        for (Map.Entry<String, Map<?, ?>> entry : fetchGraph.entrySet()) {
            String newBase = base.replace('.', '_') + "_" + entry.getKey();
            sb.append(" LEFT JOIN FETCH ").append(base).append('.').append(entry.getKey());
            sb.append(' ').append(newBase);
            applyFetchGraph(sb, newBase, (Map<String, Map<?, ?>>) entry.getValue());
        }
    }

    private String createQueryString(EntityViewManagerImpl evm, EntityType<?> entityType, Map<String, Map<?, ?>> fetchGraph, boolean multiple) {
        CriteriaBuilderFactory cbf = evm.getCriteriaBuilderFactory();
        String[] paths = flatten(fetchGraph);
        if (paths.length == 0) {
            if (multiple) {
                return cbf.create(null, entityClass)
                    .where(JpaMetamodelUtils.getSingleIdAttribute(entityType).getName()).inExpressions(":entityIds")
                    .getQueryString();
            }
            return null;
        } else {
            CriteriaBuilder<?> criteriaBuilder = cbf.create(null, entityClass).fetch(paths);
            if (multiple) {
                criteriaBuilder.where(JpaMetamodelUtils.getSingleIdAttribute(entityType).getName()).inExpressions(":entityIds");
            } else {
                criteriaBuilder.where(JpaMetamodelUtils.getSingleIdAttribute(entityType).getName()).eqExpression(":id");
            }
            return criteriaBuilder.getQueryString();
        }
    }

    private String[] flatten(Map<String, Map<?, ?>> fetchGraph) {
        if (fetchGraph == null || fetchGraph.isEmpty()) {
            return EMPTY;
        }

        List<String> paths = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Map<?, ?>> entry : fetchGraph.entrySet()) {
            sb.append(entry.getKey());
            flatten(paths, sb, (Map<String, Map<?, ?>>) entry.getValue());
            sb.setLength(0);
        }
        return paths.toArray(new String[paths.size()]);
    }

    private void flatten(List<String> paths, StringBuilder sb, Map<String, Map<?, ?>> fetchGraph) {
        if (fetchGraph == null || fetchGraph.isEmpty()) {
            paths.add(sb.toString());
        } else {
            int length = sb.length();
            for (Map.Entry<String, Map<?, ?>> entry : fetchGraph.entrySet()) {
                sb.append('.');
                sb.append(entry.getKey());
                flatten(paths, sb, (Map<String, Map<?, ?>>) entry.getValue());
                sb.setLength(length);
            }
        }
    }

    @Override
    public Object toEntity(UpdateContext context, Object view, Object id) {
        if (id == null || queryString == null) {
            return createEntity();
        }

        return getReferenceOrLoad(context, view, id);
    }

    @Override
    public void toEntities(UpdateContext context, List<Object> views, List<Object> ids) {
        if (queryString == null) {
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
