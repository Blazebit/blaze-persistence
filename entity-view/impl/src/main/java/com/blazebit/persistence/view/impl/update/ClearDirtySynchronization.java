package com.blazebit.persistence.view.impl.update;

import javax.transaction.Status;
import javax.transaction.Synchronization;

import com.blazebit.persistence.view.impl.collection.RecordingCollection;
import com.blazebit.persistence.view.impl.collection.RecordingMap;

public class ClearDirtySynchronization implements Synchronization {
	
	private final Object[] initialState;
	private final Object[] originalDirtyState;
	private final Object[] dirtyState;

	public ClearDirtySynchronization(Object[] initialState, Object[] originalDirtyState, Object[] dirtyState) {
		this.initialState = initialState;
		this.originalDirtyState = originalDirtyState;
		this.dirtyState = dirtyState;
	}

	@Override
	public void beforeCompletion() {
	}

	@Override
	public void afterCompletion(int status) {
		if (status == Status.STATUS_COMMITTED) {
	        for (int i = 0; i < dirtyState.length; i++) {
	        	if (originalDirtyState[i] == dirtyState[i]) {
		        	initialState[i] = dirtyState[i];
		        	
		        	// TODO: what happens when new actions happen?
		        	if (dirtyState[i] instanceof RecordingCollection<?, ?>) {
		        	    ((RecordingCollection<?, ?>) dirtyState[i]).clearActions();
		        	} else if (dirtyState[i] instanceof RecordingMap<?, ?, ?>) {
                        ((RecordingMap<?, ?, ?>) dirtyState[i]).clearActions();
		        	}
	        	}
	        }
		}
	}

}
