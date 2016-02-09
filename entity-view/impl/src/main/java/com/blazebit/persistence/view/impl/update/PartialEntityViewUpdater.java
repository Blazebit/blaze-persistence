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

public class PartialEntityViewUpdater implements EntityViewUpdater {

    private final Class<?> entityClass;
	private final String idAttributeName;
	private final String[] dirtyStateFieldUpdates;
	private final DirtyAttributeFlusher<Object, Object>[] dirtyAttributeFlushers;
	private final String updateStart;
	private final String updateEnd;
	private final int bufferSize;

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public PartialEntityViewUpdater(ViewType<?> viewType) {
        this.entityClass = viewType.getEntityClass();
		Set<MethodAttribute<?, ?>> attributes = (Set<MethodAttribute<?, ?>>) (Set) viewType.getAttributes();
        MethodAttribute<?, ?> idAttribute = viewType.getIdAttribute();
        List<DirtyAttributeFlusher<? extends Object, ? extends Object>> flushers = new ArrayList<DirtyAttributeFlusher<? extends Object, ? extends Object>>(attributes.size());
        List<String> attributeUpdateList = new ArrayList<String>(attributes.size());
        StringBuilder sb = new StringBuilder(100);
        int length = 0;
        
        for (MethodAttribute<?, ?> attribute : attributes) {
        	if (attribute == idAttribute) {
        		continue;
        	}
        	
        	if (attribute.isUpdatable()) {
        	    String attributeName = attribute.getName();
        	    String attributeMapping = ((MappingAttribute<?, ?>) viewType.getAttribute(attributeName)).getMapping();
        	    
        		sb.setLength(0);
	            sb.append(attributeMapping);
	            sb.append(" = :");
        		sb.append(attributeName);
        		
        		if (attribute.isCollection()) {
        		    if (attribute instanceof MapAttribute<?, ?, ?>) {
                        flushers.add(new MapAttributeFlusher<Object, RecordingMap<Map<?, ?>, ?, ?>>((PropertyPathExpression<Object, ? extends Map<?, ?>>) ExpressionUtils.getExpression(entityClass, attributeMapping)));
        		    } else {
                        flushers.add(new CollectionAttributeFlusher<Object, RecordingCollection<Collection<?>, ?>>((PropertyPathExpression<Object, ? extends Collection<?>>) ExpressionUtils.getExpression(entityClass, attributeMapping)));
        		    }
        		} else {
        		    flushers.add(new BasicAttributeFlusher<Object, Object>(attributeName, (PropertyPathExpression<Object, Object>) ExpressionUtils.getExpression(entityClass, attributeMapping)));
        		}
        		
        		attributeUpdateList.add(sb.toString());
        		length += sb.length() + 2;
        	}
        }
        
        dirtyStateFieldUpdates = attributeUpdateList.toArray(new String[attributeUpdateList.size()]);
        dirtyAttributeFlushers = flushers.toArray(new DirtyAttributeFlusher[flushers.size()]);

        String idName = idAttribute.getName();
        idAttributeName = idName;
        updateStart = "UPDATE " + entityClass.getName() + " SET ";
        updateEnd = " WHERE " + ((MappingAttribute<?, ?>) viewType.getAttribute(idName)).getMapping() + " = :" + idName;
        bufferSize = updateStart.length() + updateEnd.length() + length;
	}
	
    @Override
	public void executeUpdate(EntityManager em, UpdatableProxy updatableProxy) {
        TransactionSynchronizationStrategy synchronizationStrategy = TransactionHelper.getSynchronizationStrategy(em);
        
        if (!synchronizationStrategy.isActive()) {
			throw new IllegalStateException("Transaction is not active!");
        }
		
        Object id = updatableProxy.$$_getId();
        Object[] initialState = updatableProxy.$$_getInitialState();
        Object[] originalDirtyState = updatableProxy.$$_getDirtyState();
        boolean[] dirty = new boolean[originalDirtyState.length];
        
        StringBuilder sb = new StringBuilder(bufferSize);
		sb.append(updateStart);

        boolean first = true;
        boolean supportsQueryFlush = true;
        for (int i = 0; i < originalDirtyState.length; i++) {
        	if (isDirty(initialState[i], originalDirtyState[i])) {
        	    dirty[i] = true;
        	    supportsQueryFlush = supportsQueryFlush && dirtyAttributeFlushers[i].supportsQueryFlush();
        	    
	            if (first) {
	                first = false;
	            } else {
	                sb.append(", ");
	            }
	            
	            sb.append(dirtyStateFieldUpdates[i]);
        	}
        }
        
        // If nothing is dirty, we don't have to do anything
        if (first) {
        	return;
        }

        // Copy the dirtyState because until transaction commit, there might still happen some changes
        Object[] dirtyState = originalDirtyState.clone();
        
        if (supportsQueryFlush) {
    		sb.append(updateEnd);
    
    		String queryString = sb.toString();
            Query query = em.createQuery(queryString);
            query.setParameter(idAttributeName, id);
    
            for (int i = 0; i < dirtyState.length; i++) {
            	if (dirty[i]) {
            	    dirtyAttributeFlushers[i].flushQuery(query, dirtyState[i]);
            	}
            }
            
            int updatedCount = query.executeUpdate();
            
            if (updatedCount != 1) {
                throw new RuntimeException("Update did not work! Expected to update 1 row but was: " + updatedCount);
            }
        } else {
            Object entity = em.getReference(entityClass, id);
            
            for (int i = 0; i < dirtyState.length; i++) {
                if (dirty[i]) {
                    dirtyAttributeFlushers[i].flushEntity(entity, dirtyState[i]);
                }
            }
        }
        
        synchronizationStrategy.registerSynchronization(new ClearDirtySynchronization(initialState, originalDirtyState, dirtyState));
	}
    
    private boolean isDirty(Object initial, Object current) {
        if (initial != current && (initial == null || !initial.equals(current))) {
            return true;
        }
        
        if (current instanceof RecordingCollection<?, ?>) {
            return ((RecordingCollection<?, ?>) current).hasActions();
        } else if (current instanceof RecordingMap<?, ?, ?>) {
            return ((RecordingMap<?, ?, ?>) current).hasActions();
        }
        
        return false;
    }
}
