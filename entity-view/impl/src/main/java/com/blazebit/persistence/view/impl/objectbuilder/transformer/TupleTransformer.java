/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.transformer;


import com.blazebit.persistence.view.impl.objectbuilder.transformator.UpdatableViewMap;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface TupleTransformer {

    public int getConsumeStartIndex();

    public int getConsumeEndIndex();

    public Object[] transform(Object[] tuple, UpdatableViewMap updatableViewMap);
}
