package com.blazebit.persistence.impl.util;

import java.util.Collection;
import java.util.Map;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;

import com.blazebit.reflection.ReflectionUtils;

public class MetamodelUtils {

	public static ManagedType<?> resolveManagedTargetType(Metamodel metamodel, Class<?> startType, String path) {
    	String[] pathElements = path.split("\\.");
    	ManagedType<?> currentType = metamodel.entity(startType);
        for (String property : pathElements) {
    		Attribute<?, ?> attribute = currentType.getAttribute(property);
    		Class<?> type = attribute.getJavaType();
    		
    		if (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)) {
            	Class<?>[] typeArguments = ReflectionUtils.getResolvedMethodReturnTypeArguments(currentType.getJavaType(), ReflectionUtils.getGetter(currentType.getJavaType(), property));
            	type = typeArguments[typeArguments.length - 1];
            }
    		
    		if (attribute.getPersistentAttributeType() == PersistentAttributeType.BASIC) {
    			throw new RuntimeException("Path [" + path.toString() + "] contains BASIC path element");
    		} else if (attribute.getPersistentAttributeType() == PersistentAttributeType.EMBEDDED) {
    			currentType = metamodel.embeddable(type);
    		} else {
    			currentType = metamodel.entity(type);
    		}
        }
        
        return currentType;
    }
	
}
