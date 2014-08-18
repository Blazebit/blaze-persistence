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

import com.blazebit.persistence.view.AttributeFilterProvider;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * Represents an attribute of a view type specified by a getter.
 *
 * @param <X> The type of the declaring entity view
 * @param <Y> The type of attribute
 * @author Christian Beikov
 * @since 1.0
 */
public interface MethodAttribute<X, Y> extends Attribute<X, Y> {

    /**
     * Returns the name of this attribute.
     *
     * @return The name of this attribute
     */
    public String getName();

    /**
     * Returns the getter java method of this attribute.
     *
     * @return The getter java method of this attribute
     */
    public Method getJavaMethod();
    
    /**
     * Returns the attribute filter mapping of this attribute with the given name.
     * 
     * @param filterName The name of the attribute filter mapping which should be returned
     * @return The attribute filter mapping of this attribute with the given name
     */
    public AttributeFilterMapping getFilter(String filterName);
    
    /**
     * Returns the attribute filter mappings of this attribute.
     * 
     * @return The attribute filter mappings of this attribute
     */
    public Set<AttributeFilterMapping> getFilters();
}
