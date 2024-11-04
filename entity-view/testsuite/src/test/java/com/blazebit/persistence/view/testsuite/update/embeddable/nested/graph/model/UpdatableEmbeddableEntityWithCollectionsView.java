/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.embeddable.nested.graph.model;

import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntity;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.model.SimpleEmbeddableEntityView;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.model.UpdatableEmbeddableEntityWithCollectionsEmbeddableViewBase;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.model.UpdatableEmbeddableEntityWithCollectionsViewBase;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@UpdatableEntityView
@EntityView(EmbeddableTestEntity.class)
public abstract class UpdatableEmbeddableEntityWithCollectionsView implements UpdatableEmbeddableEntityWithCollectionsViewBase {

    // The duplication of result rows because of multiple objects on this level causes #628
    @Mapping("embeddable.nestedEmbeddable.nestedOneToMany")
    public abstract Set<SimpleEmbeddableEntityView> getNestedOneToManys();

    public abstract UpdatableEmbeddableEntityWithMultipleCollectionsEmbeddableViewBase getEmbeddable();

    public abstract void setEmbeddable(UpdatableEmbeddableEntityWithMultipleCollectionsEmbeddableViewBase embeddable);

    public void setEmbeddable(UpdatableEmbeddableEntityWithCollectionsEmbeddableViewBase embeddable) {
        setEmbeddable((UpdatableEmbeddableEntityWithMultipleCollectionsEmbeddableViewBase) embeddable);
    }

}
