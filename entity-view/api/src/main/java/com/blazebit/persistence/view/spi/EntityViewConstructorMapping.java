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

package com.blazebit.persistence.view.spi;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Mapping of an entity view constructor.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface EntityViewConstructorMapping {

    /**
     * Returns the mapping of the view declaring this constructor.
     *
     * @return The declaring view mapping
     */
    public EntityViewMapping getDeclaringView();

    /**
     * Returns the name of the view constructor.
     *
     * @return The view constructor name
     */
    public String getName();

    /**
     * Returns the constructor object of the declaring view java type represented by this mapping.
     *
     * @return The constructor represented by this mapping
     */
    public Constructor<?> getConstructor();

    /**
     * Returns the parameter mappings of this constructor mapping.
     *
     * @return The parameter mappings of this constructor mapping
     */
    List<EntityViewParameterMapping> getParameters();
}
