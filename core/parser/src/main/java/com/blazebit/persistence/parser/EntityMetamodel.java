/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * This is a wrapper around the JPA {@link javax.persistence.metamodel.Metamodel} that allows additionally efficient access by other attributes than a Class.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface EntityMetamodel extends Metamodel {

    public EntityType<?> getEntity(String name);

    public Set<EntityType<?>> getEntitySubtypes(EntityType<?> entityType);

    public ManagedType<?> getManagedType(String name);

    public ManagedType<?> managedType(String name);

    public <X> EntityType<X> getEntity(Class<X> cls);

    public <X> ManagedType<X> getManagedType(Class<X> cls);

    public Collection<Type<?>> getBasicTypes();

    public Map<String, Class<Enum<?>>> getEnumTypes();

    public Map<String, Class<Enum<?>>> getEnumTypesForLiterals();

    public <X> Type<X> type(Class<X> cls);

    public <T> T getManagedType(Class<T> cls, ManagedType<?> managedType);

    public <T> T getManagedType(Class<T> cls, Class<?> managedType);

    public <T> T getManagedType(Class<T> cls, String managedTypeName);
}
