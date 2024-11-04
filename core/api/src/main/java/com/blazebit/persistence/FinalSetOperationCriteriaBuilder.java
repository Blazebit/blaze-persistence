/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * An interface for builders that support set operators.
 *
 * @param <T> The builder result type
 * @author Christian Beikov
 * @since 1.1.0
 */
public interface FinalSetOperationCriteriaBuilder<T> extends CommonQueryBuilder<FinalSetOperationCriteriaBuilder<T>>, Queryable<T, FinalSetOperationCriteriaBuilder<T>>, BaseFinalSetOperationBuilder<T, FinalSetOperationCriteriaBuilder<T>> {

}
