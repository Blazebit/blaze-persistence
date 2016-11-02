package com.blazebit.persistence.impl.util;

import java.util.Collection;
import java.util.Map;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;

import com.blazebit.reflection.ReflectionUtils;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class MetamodelUtils {

    private MetamodelUtils() {
    }

    public static ManagedType<?> resolveManagedTargetType(Metamodel metamodel, Class<?> entityClass, String path) {
        String[] pathElements = path.split("\\.");
        ManagedType<?> currentType = metamodel.entity(entityClass);
        for (String property : pathElements) {
            Attribute<?, ?> attribute = currentType.getAttribute(property);
            Class<?> type = attribute.getJavaType();
            
            if (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)) {
                Class<?>[] typeArguments = ReflectionUtils.getResolvedMethodReturnTypeArguments(currentType.getJavaType(), ReflectionUtils.getGetter(currentType.getJavaType(), property));
                type = typeArguments[typeArguments.length - 1];
            }
            
            if (attribute.getPersistentAttributeType() == PersistentAttributeType.BASIC) {
                throw new RuntimeException("Path [" + path.toString() + "] contains BASIC path element");
            } else  {
                currentType = metamodel.managedType(type);
            }
        }
        
        return currentType;
    }

    public static Attribute<?, ?> resolveTargetAttribute(Metamodel metamodel, Class<?> entityClass, String path) {
        String[] pathElements = path.split("\\.");
        ManagedType<?> managedType;
        if (pathElements.length > 1) {
            String subpath = path.substring(0, path.lastIndexOf('.'));
            managedType = MetamodelUtils.resolveManagedTargetType(metamodel, entityClass, subpath);
        } else {
            managedType = metamodel.entity(entityClass);
        }

        return managedType.getAttribute(pathElements[pathElements.length - 1]);
    }

}
