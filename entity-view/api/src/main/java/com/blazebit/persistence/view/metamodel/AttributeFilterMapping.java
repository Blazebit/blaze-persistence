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

package com.blazebit.persistence.view.metamodel;

import com.blazebit.persistence.view.AttributeFilterProvider;

/**
 * Represents the mapping of a named filter on an entity view attribute.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface AttributeFilterMapping extends FilterMapping<AttributeFilterProvider> {
    
    /**
     * Returns the declaring attribute.
     *
     * @return The declaring attribute
     */
    public MethodAttribute<?, ?> getDeclaringAttribute();
    
}
