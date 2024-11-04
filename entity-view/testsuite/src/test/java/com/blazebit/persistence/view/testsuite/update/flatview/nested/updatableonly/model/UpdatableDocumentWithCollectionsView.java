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

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@UpdatableEntityView
@EntityView(Document.class)
public interface UpdatableDocumentWithCollectionsView<T extends UpdatableNameObjectContainerView<? extends UpdatableNameObjectView>> {
    
    @IdMapping
    public Long getId();

    public String getName();

    public void setName(String name);

    @UpdatableMapping(updatable = true, cascade = { CascadeType.PERSIST, CascadeType.UPDATE })
    public List<T> getNameContainers();

    public void setNameContainers(List<T> nameContainers);

}
