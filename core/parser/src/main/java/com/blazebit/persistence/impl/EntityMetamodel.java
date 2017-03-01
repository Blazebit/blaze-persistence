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

package com.blazebit.persistence.impl;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;

/**
 * This is a wrapper around the JPA {@link javax.persistence.metamodel.Metamodel} that allows additionally efficient access by other attributes than a Class.
 *
 * @author Christian Beikov
 * @since 1.2
 */
public interface EntityMetamodel extends Metamodel {

    public EntityType<?> getEntity(String name);

    public ManagedType<?> getManagedType(String name);

    public ManagedType<?> managedType(String name);

    public <X> EntityType<X> getEntity(Class<X> cls);

    public <X> ManagedType<X> getManagedType(Class<X> cls);

}
