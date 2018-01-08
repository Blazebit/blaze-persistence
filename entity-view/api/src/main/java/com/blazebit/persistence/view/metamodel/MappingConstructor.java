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

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Represents a constructor of a view type.
 *
 * @param <X> The type of the declaring entity view
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface MappingConstructor<X> {

    /**
     * Returns the name of the constructor.
     *
     * @return The name of the constructor
     */
    public String getName();

    /**
     * Returns the declaring managed view type.
     *
     * @return The declaring managed view type
     */
    public ManagedViewType<X> getDeclaringType();

    /**
     * Returns the java constructor for this mapping constructor.
     *
     * @return The java constructor for this mapping constructor
     */
    public Constructor<X> getJavaConstructor();

    /**
     * Returns the parameter attributes of this mapping constructor.
     *
     * @return The parameter attributes of this mapping constructor
     */
    public List<ParameterAttribute<? super X, ?>> getParameterAttributes();

    /**
     * Returns the parameter attribute of this mapping constructor at the given index if it exists, otherwise null.
     *
     * @param index The index at which the parameter is located
     * @return The parameter attribute of this mapping constructor at the given index if it exists, otherwise null.
     */
    public ParameterAttribute<? super X, ?> getParameterAttribute(int index);
}
