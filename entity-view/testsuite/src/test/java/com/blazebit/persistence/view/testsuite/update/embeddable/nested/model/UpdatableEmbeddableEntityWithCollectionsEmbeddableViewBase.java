/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.embeddable.nested.model;

import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityEmbeddable;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@UpdatableEntityView
@EntityView(EmbeddableTestEntityEmbeddable.class)
public interface UpdatableEmbeddableEntityWithCollectionsEmbeddableViewBase {

    public Set<SimpleEmbeddableEntityView> getOneToMany2();

    public void setOneToMany2(Set<SimpleEmbeddableEntityView> oneToMany2);

}
