/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria;

import jakarta.persistence.criteria.CommonAbstractCriteria;

/**
 * An extended version of {@link CommonAbstractCriteria}.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface BlazeCommonAbstractCriteria extends CommonAbstractCriteria {

    /* Covariant overrides */

    @Override
    <U> BlazeSubquery<U> subquery(Class<U> type);
}
