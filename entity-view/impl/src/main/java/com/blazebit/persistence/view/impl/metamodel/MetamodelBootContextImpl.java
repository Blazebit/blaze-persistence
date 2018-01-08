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

package com.blazebit.persistence.view.impl.metamodel;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MetamodelBootContextImpl implements MetamodelBootContext {

    private final Map<Class<?>, ViewMapping> viewMappings;
    private final Set<String> errors;

    public MetamodelBootContextImpl() {
        this.viewMappings = new HashMap<>();
        this.errors = new LinkedHashSet<>();
    }

    @Override
    public ViewMapping getViewMapping(Class<?> clazz) {
        return viewMappings.get(clazz);
    }

    @Override
    public void addViewMapping(Class<?> clazz, ViewMapping viewMapping) {
        viewMappings.put(clazz, viewMapping);
    }

    @Override
    public Map<Class<?>, ViewMapping> getViewMappingMap() {
        return viewMappings;
    }

    @Override
    public Collection<ViewMapping> getViewMappings() {
        return viewMappings.values();
    }

    @Override
    public Set<Class<?>> getViewClasses() {
        return viewMappings.keySet();
    }

    @Override
    public void addError(String error) {
        errors.add(error);
    }

    @Override
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    @Override
    public Set<String> getErrors() {
        return errors;
    }
}
