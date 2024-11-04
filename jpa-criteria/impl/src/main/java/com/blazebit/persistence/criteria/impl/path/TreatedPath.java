/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl.path;

import com.blazebit.persistence.criteria.BlazePath;

import javax.persistence.metamodel.EntityType;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface TreatedPath<X> extends BlazePath<X> {

    public EntityType<X> getTreatType();

    public AbstractPath<? super X> getTreatedPath();

}
