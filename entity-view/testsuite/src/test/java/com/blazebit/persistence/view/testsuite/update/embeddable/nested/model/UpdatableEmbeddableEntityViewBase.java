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

package com.blazebit.persistence.view.testsuite.update.embeddable.nested.model;

import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntity;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityId;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.UpdatableEntityView;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@UpdatableEntityView
@EntityView(EmbeddableTestEntity.class)
public interface UpdatableEmbeddableEntityViewBase {//} extends SimpleEmbeddableEntityView {

    @IdMapping
    public Id getId();

    public Long getVersion();

    public UpdatableEmbeddableEntityEmbeddableViewBase getEmbeddable();

    public void setEmbeddable(UpdatableEmbeddableEntityEmbeddableViewBase embeddable);

    @EntityView(EmbeddableTestEntityId.class)
    static interface Id {
        public String getValue();
        public void setValue(String value);

        public String getKey();
        public void setKey(String key);
    }

}
