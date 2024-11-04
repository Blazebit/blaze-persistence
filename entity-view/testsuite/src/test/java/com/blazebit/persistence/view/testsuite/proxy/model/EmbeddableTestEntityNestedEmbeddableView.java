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
 * @since 1.4.0
 */
@EntityView(EmbeddableTestEntityNestedEmbeddable.class)
public interface EmbeddableTestEntityNestedEmbeddableView {

    Set<EmbeddableTestEntityView> getNestedOneToMany();
}
