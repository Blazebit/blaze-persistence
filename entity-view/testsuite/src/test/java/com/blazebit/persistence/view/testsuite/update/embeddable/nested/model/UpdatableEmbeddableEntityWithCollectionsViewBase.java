/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.embeddable.nested.model;

import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntity;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityId;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.UpdatableEntityView;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@UpdatableEntityView
@EntityView(EmbeddableTestEntity.class)
public interface UpdatableEmbeddableEntityWithCollectionsViewBase extends SimpleEmbeddableEntityView {

    public Long getVersion();

    public UpdatableEmbeddableEntityWithCollectionsEmbeddableViewBase getEmbeddable();

    public void setEmbeddable(UpdatableEmbeddableEntityWithCollectionsEmbeddableViewBase embeddable);

}
