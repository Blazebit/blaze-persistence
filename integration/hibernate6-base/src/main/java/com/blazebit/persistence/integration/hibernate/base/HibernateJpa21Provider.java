/*
 * Copyright 2014 - 2020 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.integration.hibernate.base;

import org.hibernate.engine.spi.CascadingAction;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.tuple.entity.EntityMetamodel;
import org.hibernate.type.ComponentType;
import org.hibernate.type.Type;

import javax.persistence.PersistenceUnitUtil;
import javax.persistence.metamodel.ManagedType;
import java.lang.reflect.Method;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class HibernateJpa21Provider extends HibernateJpaProvider {

    private static final Method HAS_ORPHAN_DELETE_METHOD;
    private static final Method DO_CASCADE_METHOD;
    private static final CascadingAction DELETE_CASCADE;
    static {
        try {
            Class<?> cascadeStyleClass = Class.forName("org.hibernate.engine.spi.CascadeStyle");
            HAS_ORPHAN_DELETE_METHOD = cascadeStyleClass.getMethod("hasOrphanDelete");
            DO_CASCADE_METHOD = cascadeStyleClass.getMethod("doCascade", CascadingAction.class);
            DELETE_CASCADE = (CascadingAction) Class.forName("org.hibernate.engine.spi.CascadingActions").getField("DELETE").get(null);
        } catch (Exception ex) {
            throw new RuntimeException("Could not access cascading information. Please report your version of hibernate so we can provide support for it!", ex);
        }
    }

    public HibernateJpa21Provider(PersistenceUnitUtil persistenceUnitUtil, String dbms, Map<String, EntityPersister> entityPersisters, Map<String, CollectionPersister> collectionPersisters) {
        super(persistenceUnitUtil, dbms, entityPersisters, collectionPersisters);
    }

    @Override
    public boolean isOrphanRemoval(ManagedType<?> ownerType, String attributeName) {
        AbstractEntityPersister entityPersister = getEntityPersister(ownerType);
        if (entityPersister != null) {
            EntityMetamodel entityMetamodel = entityPersister.getEntityMetamodel();
            Integer index = entityMetamodel.getPropertyIndexOrNull(attributeName);
            if (index != null) {
                try {
                    return (boolean) HAS_ORPHAN_DELETE_METHOD.invoke(entityMetamodel.getCascadeStyles()[index]);
                } catch (Exception ex) {
                    throw new RuntimeException("Could not access orphan removal information. Please report your version of hibernate so we can provide support for it!", ex);
                }
            }
        }

        return false;
    }

    @Override
    public boolean isOrphanRemoval(ManagedType<?> ownerType, String elementCollectionPath, String attributeName) {
        Type elementType = getCollectionPersister(ownerType, elementCollectionPath).getElementType();
        if (!(elementType instanceof ComponentType)) {
            // This can only happen for collection/join table target attributes, where it is irrelevant
            return false;
        }
        ComponentType componentType = (ComponentType) elementType;
        String subAttribute = attributeName.substring(elementCollectionPath.length() + 1);
        // Component types only store direct properties, so we have to go deeper
        String[] propertyParts = subAttribute.split("\\.");
        int propertyIndex = 0;
        for (; propertyIndex < propertyParts.length - 1; propertyIndex++) {
            int index = componentType.getPropertyIndex(propertyParts[propertyIndex]);
            Type propertyType = componentType.getSubtypes()[index];
            if (propertyType instanceof ComponentType) {
                componentType = (ComponentType) propertyType;
            } else {
                // The association property is just as good as the id property of the association for our purposes
                // So we stop here and query the association property instead
                break;
            }
        }

        try {
            return (boolean) HAS_ORPHAN_DELETE_METHOD.invoke(componentType.getCascadeStyle(propertyIndex));
        } catch (Exception ex) {
            throw new RuntimeException("Could not access orphan removal information. Please report your version of hibernate so we can provide support for it!", ex);
        }
    }

    @Override
    public boolean isDeleteCascaded(ManagedType<?> ownerType, String attributeName) {
        AbstractEntityPersister entityPersister = getEntityPersister(ownerType);
        if (entityPersister != null) {
            EntityMetamodel entityMetamodel = entityPersister.getEntityMetamodel();
            Integer index = entityMetamodel.getPropertyIndexOrNull(attributeName);
            if (index != null) {
                try {
                    return (boolean) DO_CASCADE_METHOD.invoke(entityMetamodel.getCascadeStyles()[index], DELETE_CASCADE);
                } catch (Exception ex) {
                    throw new RuntimeException("Could not access orphan removal information. Please report your version of hibernate so we can provide support for it!", ex);
                }
            }
        }

        return false;
    }

    @Override
    public boolean isDeleteCascaded(ManagedType<?> ownerType, String elementCollectionPath, String attributeName) {
        Type elementType = getCollectionPersister(ownerType, elementCollectionPath).getElementType();
        if (!(elementType instanceof ComponentType)) {
            // This can only happen for collection/join table target attributes, where it is irrelevant
            return false;
        }
        ComponentType componentType = (ComponentType) elementType;
        String subAttribute = attributeName.substring(elementCollectionPath.length() + 1);
        // Component types only store direct properties, so we have to go deeper
        String[] propertyParts = subAttribute.split("\\.");
        int propertyIndex = 0;
        for (; propertyIndex < propertyParts.length - 1; propertyIndex++) {
            int index = componentType.getPropertyIndex(propertyParts[propertyIndex]);
            Type propertyType = componentType.getSubtypes()[index];
            if (propertyType instanceof ComponentType) {
                componentType = (ComponentType) propertyType;
            } else {
                // The association property is just as good as the id property of the association for our purposes
                // So we stop here and query the association property instead
                break;
            }
        }
        try {
            return (boolean) DO_CASCADE_METHOD.invoke(componentType.getCascadeStyle(propertyIndex), DELETE_CASCADE);
        } catch (Exception ex) {
            throw new RuntimeException("Could not access orphan removal information. Please report your version of hibernate so we can provide support for it!", ex);
        }
    }

    @Override
    public boolean supportsJpa21() {
        return true;
    }

    @Override
    public String getOnClause() {
        return "ON";
    }

    @Override
    public boolean supportsTreatJoin() {
        return true;
    }

}
