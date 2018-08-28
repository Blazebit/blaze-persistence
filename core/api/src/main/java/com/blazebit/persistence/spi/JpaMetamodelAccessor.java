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

package com.blazebit.persistence.spi;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.3.0
 */
public interface JpaMetamodelAccessor {

    /**
     * Construct an {@code AttributePath} for a particular attribute in type.
     * @param metamodel JPA metamodel
     * @param type Owning type
     * @param attributePath The attribute path
     * @return The created attribute path
     */
    AttributePath getAttributePath(Metamodel metamodel, ManagedType<?> type, String attributePath);

    /**
     * Construct an {@code AttributePath} for a particular basic attribute in type.
     * @param metamodel JPA metamodel
     * @param type Owning type
     * @param attributePath The attribute path
     * @return The created attribute path
     */
    AttributePath getBasicAttributePath(Metamodel metamodel, ManagedType<?> type, String attributePath);

    /**
     * Construct an {@code AttributePath} for a particular collection attribute in type.
     * @param metamodel JPA metamodel
     * @param type Owning type
     * @param attributePath The attribute path
     * @param collectionName The name of the collection
     * @return The created attribute path
     */
    AttributePath getJoinTableCollectionAttributePath(Metamodel metamodel, EntityType<?> type, String attributePath, String collectionName);

    /**
     * Returns true if the attribute is joinable (i.e. association).
     * @param attr The attribute
     * @return Whether the attribute is joinable
     */
    boolean isJoinable(Attribute<?, ?> attr);

    /**
     * Returns true if the attribute is composite (i.e. embeddable).
     * @param attr The attribute
     * @return Whether the attribute is composite
     */
    boolean isCompositeNode(Attribute<?, ?> attr);
}
