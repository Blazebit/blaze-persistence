package com.blazebit.persistence.view.impl.proxy;

import java.util.Map;


public interface UpdateableProxy {
    
    public Class<?> getEntityClass();

    public Object getId();
    
    public Object getVersion();
    
    public Map<String, Object> getState();
    
    public Map<String, Object> getDirtyState();
}
