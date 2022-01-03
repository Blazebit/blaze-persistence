/*
 * Copyright 2014 - 2022 Blazebit.
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
