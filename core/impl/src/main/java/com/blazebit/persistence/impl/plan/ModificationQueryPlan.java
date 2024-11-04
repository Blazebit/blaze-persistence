/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.plan;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface ModificationQueryPlan {

    public int executeUpdate();

}
