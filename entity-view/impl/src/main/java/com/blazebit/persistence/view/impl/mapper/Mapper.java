/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.mapper;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface Mapper<S, T> {

    public void map(S source, T target);

}
