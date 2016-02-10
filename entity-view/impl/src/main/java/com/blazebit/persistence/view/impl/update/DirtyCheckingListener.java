package com.blazebit.persistence.view.impl.update;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

public class DirtyCheckingListener {

	@PrePersist
	public void prePersist(Object o) {
		System.out.println("PrePersist: " + o);
	}
	
	@PostPersist
	public void postPersist(Object o) {
		System.out.println("PostPersist: " + o);
	}
	
	@PreUpdate
	public void preUpdate(Object o) {
		System.out.println("PreUpdate: " + o);
	}
	
	@PreUpdate
	public void postUpdate(Object o) {
		System.out.println("PostUpdate: " + o);
	}
	
	@PreRemove
	public void preRemove(Object o) {
		System.out.println("PreRemove: " + o);
	}
	
	@PostRemove
	public void postRemove(Object o) {
		System.out.println("PostRemove: " + o);
	}
	
}
