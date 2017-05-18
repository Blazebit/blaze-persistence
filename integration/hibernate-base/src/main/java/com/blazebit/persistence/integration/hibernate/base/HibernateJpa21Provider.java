/*
 * Copyright 2014 - 2018 Blazebit.
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

import javax.persistence.PersistenceUnitUtil;
import javax.persistence.metamodel.ManagedType;
import java.lang.reflect.Method;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
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

    public HibernateJpa21Provider(PersistenceUnitUtil persistenceUnitUtil, String dbms, Map<String, EntityPersister> entityPersisters, Map<String, CollectionPersister> collectionPersisters, int major, int minor, int fix) {
        super(persistenceUnitUtil, dbms, entityPersisters, collectionPersisters, major, minor, fix);
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
