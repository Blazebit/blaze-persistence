/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.embeddable.nested.graph.model;

import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityEmbeddable;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.model.SimpleEmbeddableEntityView;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@UpdatableEntityView
@EntityView(EmbeddableTestEntityEmbeddable.class)
public interface UpdatableEmbeddableEntityWithMultipleCollectionsEmbeddableViewBase extends com.blazebit.persistence.view.testsuite.update.embeddable.nested.model.UpdatableEmbeddableEntityWithCollectionsEmbeddableViewBase {

    public Set<SimpleEmbeddableEntityView> getOneToMany();

    public void setOneToMany(Set<SimpleEmbeddableEntityView> oneToMany);

}
