/*
 * Copyright 2014 - 2017 Blazebit.
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

package com.blazebit.persistence.view.impl.metamodel;

import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This is a wrapper around the JPA {@link javax.persistence.metamodel.Metamodel} allows additionally efficient access by other attributes than a Class.
 *
 * @author Christian Beikov
 * @since 1.2
 */
public class EntityMetamodel implements Metamodel {

    private final Metamodel delegate;
    private final Map<String, EntityType<?>> entityNameMap;
    private final Map<Class<?>, ManagedType<?>> classMap;

    public EntityMetamodel(Metamodel delegate) {
        this.delegate = delegate;
        Set<ManagedType<?>> managedTypes = delegate.getManagedTypes();
        Map<String, EntityType<?>> nameToType = new HashMap<String, EntityType<?>>(managedTypes.size());
        Map<Class<?>, ManagedType<?>> classToType = new HashMap<Class<?>, ManagedType<?>>(managedTypes.size());

        for (ManagedType<?> t : managedTypes) {
            if (t instanceof EntityType<?>) {
                EntityType<?> e = (EntityType<?>) t;
                nameToType.put(e.getName(), e);
            }
            classToType.put(t.getJavaType(), t);
        }

        this.entityNameMap = Collections.unmodifiableMap(nameToType);
        this.classMap = Collections.unmodifiableMap(classToType);
    }

    @Override
    public <X> EntityType<X> entity(Class<X> cls) {
        return delegate.entity(cls);
    }

    public EntityType<?> getEntity(String name) {
        return entityNameMap.get(name);
    }

    @Override
    public <X> ManagedType<X> managedType(Class<X> cls) {
        return delegate.managedType(cls);
    }

    @SuppressWarnings({ "unchecked" })
    public <X> ManagedType<X> getManagedType(Class<X> cls) {
        return (ManagedType<X>) classMap.get(cls);
    }

    @Override
    public <X> EmbeddableType<X> embeddable(Class<X> cls) {
        return delegate.embeddable(cls);
    }

    @Override
    public Set<ManagedType<?>> getManagedTypes() {
        return delegate.getManagedTypes();
    }

    @Override
    public Set<EntityType<?>> getEntities() {
        return delegate.getEntities();
    }

    @Override
    public Set<EmbeddableType<?>> getEmbeddables() {
        return delegate.getEmbeddables();
    }
}
