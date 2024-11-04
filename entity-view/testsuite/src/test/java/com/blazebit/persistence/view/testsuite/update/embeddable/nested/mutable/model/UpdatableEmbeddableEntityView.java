/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.embeddable.nested.mutable.model;

import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntity;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.model.UpdatableEmbeddableEntityViewBase;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@UpdatableEntityView
@EntityView(EmbeddableTestEntity.class)
public interface UpdatableEmbeddableEntityView extends UpdatableEmbeddableEntityViewBase {


}
