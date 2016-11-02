package com.blazebit.persistence.view.impl.update;

import javax.persistence.EntityManager;

import com.blazebit.persistence.view.impl.proxy.UpdatableProxy;

public interface EntityViewUpdater {

    public void executeUpdate(EntityManager em, UpdatableProxy updatableProxy);
    
}
