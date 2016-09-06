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
