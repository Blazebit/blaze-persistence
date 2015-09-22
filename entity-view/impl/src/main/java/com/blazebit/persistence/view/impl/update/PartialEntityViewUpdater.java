package com.blazebit.persistence.view.impl.update;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.blazebit.persistence.view.impl.proxy.UpdateableProxy;
import com.blazebit.persistence.view.impl.tx.TransactionHelper;
import com.blazebit.persistence.view.impl.tx.TransactionSynchronizationStrategy;
import com.blazebit.persistence.view.metamodel.MappingAttribute;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ViewType;

public class PartialEntityViewUpdater implements EntityViewUpdater {

	private final String idAttributeName;
	private final String[] dirtyStateFieldUpdates;
	private final String[] dirtyStateAttributeNames;
	private final int[] dirtyStateIndexToInitialStateIndexMapping;
	private final String updateStart;
	private final String updateEnd;
	private final int bufferSize;

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public PartialEntityViewUpdater(ViewType<?> viewType) {
        Class<?> entityClass = viewType.getEntityClass();
		Set<MethodAttribute<?, ?>> attributes = (Set<MethodAttribute<?, ?>>) (Set) viewType.getAttributes();
        MethodAttribute<?, ?> idAttribute = viewType.getIdAttribute();
        List<String> attributeUpdateList = new ArrayList<String>(attributes.size());
        List<String> attributeList = new ArrayList<String>(attributes.size());
        List<Integer> indexMapping = new ArrayList<Integer>(attributes.size());
        StringBuilder sb = new StringBuilder(100);
        int length = 0;
        
        int i = 1;
        for (MethodAttribute<?, ?> attribute : attributes) {
        	if (attribute == idAttribute) {
        		continue;
        	}
        	
        	if (attribute.isUpdateable()) {
        		sb.setLength(0);
	            sb.append(((MappingAttribute<?, ?>) viewType.getAttribute(attribute.getName())).getMapping());
	            sb.append(" = :");
        		sb.append(attribute.getName());
        		attributeUpdateList.add(sb.toString());
        		attributeList.add(attribute.getName());
        		indexMapping.add(i);
        		length += sb.length() + 2;
        	}
        	
        	i++;
        }
        
        dirtyStateFieldUpdates = attributeUpdateList.toArray(new String[attributeUpdateList.size()]);
        dirtyStateAttributeNames = attributeList.toArray(new String[attributeList.size()]);
        dirtyStateIndexToInitialStateIndexMapping = new int[indexMapping.size()];
        
        for (i = 0; i < dirtyStateIndexToInitialStateIndexMapping.length; i++) {
        	dirtyStateIndexToInitialStateIndexMapping[i] = indexMapping.get(i);
        }

        String idName = idAttribute.getName();
        idAttributeName = idName;
        updateStart = "UPDATE " + entityClass.getName() + " SET ";
        updateEnd = " WHERE " + ((MappingAttribute<?, ?>) viewType.getAttribute(idName)).getMapping() + " = :" + idName;
        bufferSize = updateStart.length() + updateEnd.length() + length;
	}
	
    @Override
	public void executeUpdate(EntityManager em, UpdateableProxy updateableProxy) {
        TransactionSynchronizationStrategy synchronizationStrategy = TransactionHelper.getSynchronizationStrategy(em);
        
        if (!synchronizationStrategy.isActive()) {
			throw new IllegalStateException("Transaction is not active!");
        }
		
        Object id = updateableProxy.$$_getId();
        Object[] initialState = updateableProxy.$$_getInitialState();
        Object[] originalDirtyState = updateableProxy.$$_getDirtyState();
        
        StringBuilder sb = new StringBuilder(bufferSize);
		sb.append(updateStart);

		int i;
        boolean first = true;
        for (i = 0; i < originalDirtyState.length; i++) {
        	if (originalDirtyState[i] != null) {
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
        
		sb.append(updateEnd);

        // Copy the dirtyState because until transaction commit, there might still happen some changes
        Object[] dirtyState = originalDirtyState.clone();
		String queryString = sb.toString();
        Query query = em.createQuery(queryString);
        query.setParameter(idAttributeName, id);

        for (i = 0; i < dirtyState.length; i++) {
        	if (dirtyState[i] != null) {
        		query.setParameter(dirtyStateAttributeNames[i], dirtyState[i]);
        	}
        }
        
        int updatedCount = query.executeUpdate();
        
        if (updatedCount != 1) {
            throw new RuntimeException("Update did not work! Expected to update 1 row but was: " + updatedCount);
        }
        
        synchronizationStrategy.registerSynchronization(new ClearDirtySynchronization(initialState, originalDirtyState, dirtyState, dirtyStateIndexToInitialStateIndexMapping));
	}
}
