/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl.support;

import jakarta.persistence.criteria.CommonAbstractCriteria;
import jakarta.persistence.criteria.Subquery;

/**
 * An interface necessary to be able to compile against JPA 2.0 but let users use JPA 2.1 APIs.
 *
 * @param <T> The type of the selection item
 * @author Christian Beikov
 * @since 1.3.0
 */
public interface SubquerySupport<T> extends Subquery<T> {

    public CommonAbstractCriteria getContainingQuery();

}
