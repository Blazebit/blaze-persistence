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

package com.blazebit.persistence;

import javax.persistence.metamodel.Type;

/**
 * CAREFUL, this is an experimental API and will change!
 *
 * A resolved path expression.
 *
 * @author Christian Beikov
 * @since 1.2.1
 */
public interface Path {

    /**
     * The from node on which this path is based.
     *
     * @return The from node
     */
    public From getFrom();

    /**
     * The qualified path as string.
     *
     * @return The qualified path
     */
    public String getPath();

    /**
     * The type of the path.
     *
     * @return The type
     */
    public Type<?> getType();

    /**
     * The java type of the path.
     *
     * @return The type
     */
    public Class<?> getJavaType();

}
