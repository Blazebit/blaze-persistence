package com.blazebit.persistence.view.impl.update;

import javax.transaction.Status;
import javax.transaction.Synchronization;

public class ClearDirtySynchronization implements Synchronization {
	
	private final Object[] initialState;
	private final Object[] originalDirtyState;
	private final Object[] dirtyState;
	private final int[] dirtyStateIndexToInitialStateIndexMapping;

	public ClearDirtySynchronization(Object[] initialState, Object[] originalDirtyState, Object[] dirtyState, int[] dirtyStateIndexToInitialStateIndexMapping) {
		this.initialState = initialState;
		this.originalDirtyState = originalDirtyState;
		this.dirtyState = dirtyState;
		this.dirtyStateIndexToInitialStateIndexMapping = dirtyStateIndexToInitialStateIndexMapping;
	}

	@Override
	public void beforeCompletion() {
	}

	@Override
	public void afterCompletion(int status) {
		if (status == Status.STATUS_COMMITTED) {
	        for (int i = 0; i < dirtyState.length; i++) {
	        	if (originalDirtyState[i] == dirtyState[i]) {
		        	initialState[dirtyStateIndexToInitialStateIndexMapping[i]] = dirtyState[i];
		        	dirtyState[i] = null;
	        	}
	        }
		}
	}

}
