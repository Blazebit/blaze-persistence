/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.flatview.nested.updatableonly.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.CascadeType;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.UpdatableMapping;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@UpdatableEntityView
@EntityView(Document.class)
public interface UpdatableDocumentView<E extends UpdatableNameObjectView, T extends UpdatableNameObjectContainerView> {
    
    @IdMapping
    public Long getId();

    @UpdatableMapping(updatable = true, cascade = { CascadeType.PERSIST, CascadeType.UPDATE })
    public T getNameContainer();

    public void setNameContainer(T nameContainer);
}
