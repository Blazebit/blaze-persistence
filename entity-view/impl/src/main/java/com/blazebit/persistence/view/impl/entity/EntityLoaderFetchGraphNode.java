/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.entity;

import com.blazebit.persistence.view.impl.update.flush.FetchGraphNode;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface EntityLoaderFetchGraphNode<T extends EntityLoaderFetchGraphNode<T>> extends FetchGraphNode<T>, EntityLoader {


}
