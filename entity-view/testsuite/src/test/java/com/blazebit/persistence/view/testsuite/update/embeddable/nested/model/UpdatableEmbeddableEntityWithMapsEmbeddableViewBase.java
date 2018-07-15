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
