/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence;

/**
 * An interface for correlation query builders.
 *
 * @param <X> The concrete builder type
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface CorrelationQueryBuilder<X extends CorrelationQueryBuilder<X>> extends FromBuilder<X>, ParameterHolder<X> {

}
