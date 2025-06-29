/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.subview.field.model;

import com.blazebit.persistence.testsuite.entity.IndexedNode2;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.UpdatableEntityView;

/**
 *
 * @author Christian Beikov
 * @since 1.6.16
 */
@CreatableEntityView
@UpdatableEntityView
@EntityView(IndexedNode2.class)
public interface IndexedNode2View {
    
    @IdMapping
    public Integer getId();
    public void setId(Integer id);

    public Integer getIndex();
    public void setIndex(Integer index);

    public Root2View getParent();
    public void setParent(Root2View parent);

}
