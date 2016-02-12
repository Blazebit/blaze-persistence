package com.blazebit.persistence.view.impl.proxy;

public interface UpdatableProxy {
    
    public Class<?> $$_getEntityViewClass();

    public Object $$_getId();
    
//    public Object $$_getVersion();

    /**
     * Null if not partially updatable.
     * The order is the same as the metamodel attribute order of updatable attributes.
     * 
     * @return
     */
    public Object[] $$_getInitialState();
    
    /**
     * Never null, contains the current object state.
     * The order is the same as the metamodel attribute order of updatable attributes.
     * 
     * @return
     */
    public Object[] $$_getDirtyState();
    
    // TODO: actually i need to know to which transaction this object is bound to so we can prevent multiple usages
}
