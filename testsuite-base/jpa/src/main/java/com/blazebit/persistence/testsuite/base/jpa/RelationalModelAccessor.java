/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.base.jpa;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface RelationalModelAccessor {

    public String tableFromEntity(Class<?> entityClass);

    public String tableFromEntityRelation(Class<?> entityClass, String relationName);
}
