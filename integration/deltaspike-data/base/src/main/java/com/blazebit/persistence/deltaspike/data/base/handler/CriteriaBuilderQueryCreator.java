/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.base.handler;

import com.blazebit.persistence.FullQueryBuilder;

import jakarta.persistence.Query;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface CriteriaBuilderQueryCreator {

    Query createQuery(FullQueryBuilder<?, ?> queryBuilder);
}
