/*
 * Copyright 2014 - 2016 Blazebit.
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

package com.blazebit.persistence.view.impl.update;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.blazebit.persistence.view.impl.collection.RecordingCollection;
import com.blazebit.persistence.view.impl.collection.RecordingMap;
import com.blazebit.persistence.view.impl.proxy.UpdatableProxy;
import com.blazebit.persistence.view.impl.tx.TransactionHelper;
import com.blazebit.persistence.view.impl.tx.TransactionSynchronizationStrategy;
import com.blazebit.persistence.view.impl.update.flush.BasicAttributeFlusher;
import com.blazebit.persistence.view.impl.update.flush.CollectionAttributeFlusher;
import com.blazebit.persistence.view.impl.update.flush.DirtyAttributeFlusher;
import com.blazebit.persistence.view.impl.update.flush.MapAttributeFlusher;
import com.blazebit.persistence.view.metamodel.MapAttribute;
import com.blazebit.persistence.view.metamodel.MappingAttribute;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.reflection.ExpressionUtils;
import com.blazebit.reflection.PropertyPathExpression;

public class FullEntityViewUpdater implements EntityViewUpdater {

    private final Class<?> entityClass;
    private final String idAttributeName;
    private final DirtyAttributeFlusher<Object, Object>[] dirtyAttributeFlushers;
    private final boolean useQueryFlush;
    private final String updateQuery;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public FullEntityViewUpdater(ViewType<?> viewType) {
        this.entityClass = viewType.getEntityClass();
        Set<MethodAttribute<?, ?>> attributes = (Set<MethodAttribute<?, ?>>) (Set) viewType.getAttributes();
        MethodAttribute<?, ?> idAttribute = viewType.getIdAttribute();
        List<DirtyAttributeFlusher<? extends Object, ? extends Object>> flushers = new ArrayList<DirtyAttributeFlusher<? extends Object, ? extends Object>>(attributes.size());
        StringBuilder sb = new StringBuilder(100);

        sb.append("UPDATE " + entityClass.getName() + " SET ");
        
        boolean first = true;
        boolean supportsQueryFlush = true;
        for (MethodAttribute<?, ?> attribute : attributes) {
            if (attribute == idAttribute) {
                continue;
            }
            
            if (attribute.isUpdatable()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                
                String attributeName = attribute.getName();
                String attributeMapping = ((MappingAttribute<?, ?>) viewType.getAttribute(attributeName)).getMapping();
                DirtyAttributeFlusher<? extends Object, ? extends Object> flusher;
                
                if (attribute.isCollection()) {
                    if (attribute instanceof MapAttribute<?, ?, ?>) {
                        flusher = new MapAttributeFlusher<Object, RecordingMap<Map<?, ?>, ?, ?>>((PropertyPathExpression<Object, Map<?, ?>>) (PropertyPathExpression) ExpressionUtils.getExpression(entityClass, attributeMapping));
                    } else {
                        flusher = new CollectionAttributeFlusher<Object, RecordingCollection<Collection<?>, ?>>((PropertyPathExpression<Object, Collection<?>>) (PropertyPathExpression) ExpressionUtils.getExpression(entityClass, attributeMapping));
                    }
                } else {
                    flusher = new BasicAttributeFlusher<Object, Object>(attributeName, (PropertyPathExpression<Object, Object>) ExpressionUtils.getExpression(entityClass, attributeMapping));
                }

                supportsQueryFlush = supportsQueryFlush && flusher.supportsQueryFlush();
                flushers.add(flusher);
                
                sb.append(attributeMapping);
                sb.append(" = :");
                sb.append(attributeName);
            }
        }
        
        dirtyAttributeFlushers = flushers.toArray(new DirtyAttributeFlusher[flushers.size()]);

        String idName = idAttribute.getName();
        sb.append(" WHERE ").append(((MappingAttribute<?, ?>) viewType.getAttribute(idName)).getMapping()).append(" = :").append(idName);
        idAttributeName = idName;
        useQueryFlush = supportsQueryFlush;
        updateQuery = sb.toString();
    }

    @Override
    public void executeUpdate(EntityManager em, UpdatableProxy updatableProxy) {
        TransactionSynchronizationStrategy synchronizationStrategy = TransactionHelper.getSynchronizationStrategy(em);
        
        if (!synchronizationStrategy.isActive()) {
            throw new IllegalStateException("Transaction is not active!");
        }
        
        Object id = updatableProxy.$$_getId();
        Object[] dirtyState = updatableProxy.$$_getDirtyState();

        if (useQueryFlush) {
            Query query = em.createQuery(updateQuery);
            query.setParameter(idAttributeName, id);
            
            for (int i = 0; i < dirtyState.length; i++) {
                dirtyAttributeFlushers[i].flushQuery(query, dirtyState[i]);
            }
            
            int updatedCount = query.executeUpdate();
            
            if (updatedCount != 1) {
                throw new RuntimeException("Update did not work! Expected to update 1 row but was: " + updatedCount);
            }
        } else {
            Object entity = em.getReference(entityClass, id);
            
            for (int i = 0; i < dirtyState.length; i++) {
                dirtyAttributeFlushers[i].flushEntity(entity, dirtyState[i]);
            }
        }
    }
}
