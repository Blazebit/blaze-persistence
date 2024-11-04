/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.plan;

import java.util.List;
import java.util.stream.Stream;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface SelectQueryPlan<T> {

    public Stream<T> getResultStream();

    public List<T> getResultList();

    public T getSingleResult();

}
