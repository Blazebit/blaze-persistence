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

package com.blazebit.persistence.view.impl.entity;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.impl.update.flush.FetchGraphNode;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.metamodel.EntityType;
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

    public DefaultEntityLoaderFetchGraphNode(EntityViewManagerImpl evm, String attributeName, EntityType<?> entityType, Map<String, Map<?, ?>> fetchGraph) {
        // TODO: view id mapper?!
        super(entityType.getJavaType(), JpaMetamodelUtils.getSingleIdAttribute(entityType), null, evm.getEntityIdAccessor());
        this.attributeName = attributeName;
        this.fetchGraph = fetchGraph;
        this.queryString = createQueryString(evm, entityType, fetchGraph);
    }

    @Override
    public String getAttributeName() {
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

    private String createQueryString(EntityViewManagerImpl evm, EntityType<?> entityType, Map<String, Map<?, ?>> fetchGraph) {
        CriteriaBuilderFactory cbf = evm.getCriteriaBuilderFactory();
        EntityManagerFactory emf = cbf.getService(EntityManagerFactory.class);
        EntityManager em = emf.createEntityManager();

        try {
            String[] paths = flatten(fetchGraph);
            if (paths.length == 0) {
                return null;
            } else {
                return cbf.create(em, entityClass)
                        .fetch(paths)
                        .where(JpaMetamodelUtils.getSingleIdAttribute(entityType).getName()).eqExpression(":id")
                        .getQueryString();
            }
        } finally {
            em.close();
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
    public Object toEntity(UpdateContext context, Object id) {
        if (id == null || queryString == null) {
            return createEntity();
        }

        return getReferenceOrLoad(context, id);
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
}
