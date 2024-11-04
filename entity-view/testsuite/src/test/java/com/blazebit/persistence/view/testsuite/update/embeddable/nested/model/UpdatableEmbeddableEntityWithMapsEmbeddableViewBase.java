/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.embeddable.nested.model;

import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityEmbeddable;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityNestedEmbeddable;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@UpdatableEntityView
@EntityView(EmbeddableTestEntityEmbeddable.class)
public interface UpdatableEmbeddableEntityWithMapsEmbeddableViewBase {

    public Map<String, SimpleIntIdEntityView> getManyToMany();

    public void setManyToMany(Map<String, SimpleIntIdEntityView> manyToMany);

    public Map<String, SimpleNameObjectView> getElementCollection();

    public void setElementCollection(Map<String, SimpleNameObjectView> elementCollection);

}
