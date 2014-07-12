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

package com.blazebit.persistence.view.metamodel;

import java.util.Set;

/**
 *
 * @author cpbec
 */
public interface ViewType<X> {
    
    public String getName();
    
    public Class<X> getJavaType();
    
    public Class<?> getEntityClass();
    
    public Set<MethodAttribute<? super X, ?>> getAttributes();
    
    public MethodAttribute<? super X, ?> getAttribute(String name);
    
    public Set<MappingConstructor<X>> getConstructors();
    
    public MappingConstructor<X> getConstructor(Class<?>... parameterTypes);
    
    public Set<String> getConstructorNames();
    
    public MappingConstructor<X> getConstructor(String name);
}
