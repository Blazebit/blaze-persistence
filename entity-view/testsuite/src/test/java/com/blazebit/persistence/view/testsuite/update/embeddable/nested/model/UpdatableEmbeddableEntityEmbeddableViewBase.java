/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.embeddable.nested.model;

import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityEmbeddable;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@UpdatableEntityView
@EntityView(EmbeddableTestEntityEmbeddable.class)
public interface UpdatableEmbeddableEntityEmbeddableViewBase {

    public SimpleEmbeddableEntityView getManyToOne();

    public void setManyToOne(SimpleEmbeddableEntityView manyToOne);

}
