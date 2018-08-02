/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
