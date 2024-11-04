/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data;

import org.apache.deltaspike.data.api.criteria.CriteriaSupport;

import java.io.Serializable;

/**
 * Interface that supports {@link EntityViewRepository} and {@link CriteriaSupport} capabilities.
 *
 * @param <E> Entity type.
 * @param <V> Entity view type.
 * @param <PK> Id type.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public interface FullEntityViewRepository<E, V, PK extends Serializable> extends EntityViewRepository<E, V, PK>, CriteriaSupport<E> {
}