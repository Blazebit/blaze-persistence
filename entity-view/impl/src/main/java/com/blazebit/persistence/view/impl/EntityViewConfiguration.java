/*
 * Copyright 2014 Blazebit.
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

package com.blazebit.persistence.view.impl;

import com.blazebit.persistence.view.EntityViewManagerFactory;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author cpbec
 */
public class EntityViewConfiguration {
    
    private final Set<Class<?>> entityViewClasses = new HashSet<Class<?>>();

    public void addEntityView(Class<?> clazz) {
        entityViewClasses.add(clazz);
    }
    
    public Set<Class<?>> getEntityViews() {
        return entityViewClasses;
    }

    public EntityViewManagerFactory createEntityViewManagerFactory() {
        return new EntityViewManagerFactoryImpl(this);
    }
}
