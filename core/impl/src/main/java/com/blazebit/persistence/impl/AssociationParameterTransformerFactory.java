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

package com.blazebit.persistence.impl;

/**
 * A factory for creating/getting parameter value transformers.
 * When rewriting entity or id expressions in predicates, the opposite side of a predicate
 * could be a parameter. In such a case, the parameter has to be transformed before being passed to the JPA query.
 * This factory provides access to efficient parameter value transformers that can be registered on parameters.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface AssociationParameterTransformerFactory {

    /**
     * Returns a parameter value transformer for transforming ids to the given entity type.
     *
     * @param entityType The entity type
     * @return The transformer
     */
    public ParameterValueTransformer getToEntityTranformer(Class<?> entityType);

    /**
     * Returns a parameter value transformer for transforming entity objects to their ids.
     *
     * @return The transformer
     */
    public ParameterValueTransformer getToIdTransformer();
}
