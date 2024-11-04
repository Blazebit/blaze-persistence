/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.proxy.model;

import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntity;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityId;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(EmbeddableTestEntity.class)
public abstract class EmbeddableTestEntityView implements IdHolderView<EmbeddableTestEntityView.Id> {

    @UpdatableEntityView
    @EntityView(EmbeddableTestEntityId.class)
    public static interface Id {
        String getKey();
        void setKey(String key);
        String getValue();
        void setValue(String value);
    }
}
