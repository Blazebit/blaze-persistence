package com.blazebit.persistence.view.impl.proxy;

public interface UpdateableProxy {
    
    public Class<?> $$_getEntityViewClass();

    public Object $$_getId();
    
//    public Object $$_getVersion();
    
    public Object[] $$_getInitialState();
    
    public Object[] $$_getDirtyState();
    
    // TODO: actually i need to know to which transaction this object is bound to so we can prevent multiple usages
}
