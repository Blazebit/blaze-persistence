/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
