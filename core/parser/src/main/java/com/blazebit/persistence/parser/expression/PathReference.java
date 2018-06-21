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

package com.blazebit.persistence.parser.expression;

import javax.persistence.metamodel.Type;

/**
 * TODO: documentation
 * 
 * @author Christian Beikov
 * @since 1.1.0
 *
 */
public interface PathReference {

    // Although this node will always be a JoinNode we will use casting at use site to be able to reuse the parser
    public BaseNode getBaseNode();
    
    public String getField();

    /**
     * Returns the type of the path reference.
     *
     * @return The type of the path
     * @since 1.2.0
     */
    public Type<?> getType();
}
