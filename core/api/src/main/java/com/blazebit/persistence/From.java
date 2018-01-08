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

/**
 * CAREFUL, this is an experimental API and will change!
 *
 * TODO: documentation
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface From {

    /**
     * The alias of the from node.
     *
     * @return The alias
     */
    public String getAlias();

    /**
     * The type of the from node.
     *
     * @return The type
     */
    public Class<?> getType();

    // TODO: add access to join nodes

}
