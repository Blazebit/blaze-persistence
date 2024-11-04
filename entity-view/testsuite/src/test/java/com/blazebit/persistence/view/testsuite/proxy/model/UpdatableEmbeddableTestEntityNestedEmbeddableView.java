/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.proxy.model;

import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityNestedEmbeddable;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@UpdatableEntityView
@EntityView(EmbeddableTestEntityNestedEmbeddable.class)
public interface UpdatableEmbeddableTestEntityNestedEmbeddableView extends EmbeddableTestEntityNestedEmbeddableView {

    void setNestedOneToMany(Set<EmbeddableTestEntityView> nestedOneToMany);
}
