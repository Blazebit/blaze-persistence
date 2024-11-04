/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.entity;

import com.blazebit.persistence.view.impl.change.DirtyChecker;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface EntityToEntityMapper extends ElementToEntityMapper {

    public EntityLoaderFetchGraphNode<?> getFullGraphNode();

    public EntityLoaderFetchGraphNode<?> getFetchGraph(String[] dirtyProperties);

    public DirtyChecker<?> getDirtyChecker();
}
