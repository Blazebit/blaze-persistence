package com.blazebit.persistence.view.impl.update;

import javax.persistence.EntityManager;

import com.blazebit.persistence.view.impl.proxy.UpdateableProxy;

public interface EntityViewUpdater {

	public void executeUpdate(EntityManager em, UpdateableProxy updateableProxy);
	
}
