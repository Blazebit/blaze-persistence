/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.proxy.model;

import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntity;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityEmbeddable;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.PostCreate;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.testsuite.basic.model.IntIdEntityView;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@CreatableEntityView
@EntityView(EmbeddableTestEntity.class)
public abstract class CreatableEmbeddableTestEntityViewWithEmbeddableContainingConstructor extends EmbeddableTestEntityView {

    public abstract ReadOnlyEmbeddableTestEntityEmbeddableView getEmbeddable();

    @EntityView(EmbeddableTestEntityEmbeddable.class)
    public static abstract class ReadOnlyEmbeddableTestEntityEmbeddableView {

        public ReadOnlyEmbeddableTestEntityEmbeddableView(@MappingParameter("test") String test) {
        }
    }
}
