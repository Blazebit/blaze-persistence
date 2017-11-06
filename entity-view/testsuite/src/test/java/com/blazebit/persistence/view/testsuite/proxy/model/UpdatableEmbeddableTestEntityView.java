/*
 * Copyright 2014 - 2017 Blazebit.
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

package com.blazebit.persistence.view.testsuite.proxy.model;

import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntity;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityEmbeddable;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.PostCreate;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.testsuite.basic.model.IntIdEntityView;

import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@CreatableEntityView
@UpdatableEntityView
@EntityView(EmbeddableTestEntity.class)
public abstract class UpdatableEmbeddableTestEntityView extends EmbeddableTestEntityView {

    @PostCreate
    void postCreate(EntityViewManager evm) {
        getEmbeddable().getElementCollection().put("test", evm.create(UpdatableNameObjectView.class));
    }

    public abstract EmbeddableTestEntityEmbeddableView getEmbeddable();
    public abstract void setEmbeddable(EmbeddableTestEntityEmbeddableView embeddable);

    @UpdatableEntityView
    @EntityView(EmbeddableTestEntityEmbeddable.class)
    public static interface EmbeddableTestEntityEmbeddableView {

        public EmbeddableTestEntityView getManyToOne();
        public void setManyToOne(EmbeddableTestEntityView manyToOne);

        public Set<EmbeddableTestEntityView> getOneToMany();
        public void setOneToMany(Set<EmbeddableTestEntityView> oneToMany);

        public Map<String, UpdatableNameObjectView> getElementCollection();
        public void setElementCollection(Map<String, UpdatableNameObjectView> elementCollection);

        public Map<String, IntIdEntityView> getManyToMany();
        public void setManyToMany(Map<String, IntIdEntityView> manyToMany);

        public UpdatableEmbeddableTestEntityNestedEmbeddableView getNestedEmbeddable();
        public void setNestedEmbeddable(UpdatableEmbeddableTestEntityNestedEmbeddableView nestedEmbeddable);
    }
}
