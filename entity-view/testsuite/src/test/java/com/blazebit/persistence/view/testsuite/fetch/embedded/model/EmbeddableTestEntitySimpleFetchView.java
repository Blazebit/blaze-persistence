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

package com.blazebit.persistence.view.testsuite.fetch.embedded.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.testsuite.entity.EmbeddableTestEntity2;
import com.blazebit.persistence.view.testsuite.entity.EmbeddableTestEntityId2;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@EntityView(EmbeddableTestEntity2.class)
public interface EmbeddableTestEntitySimpleFetchView {

    @IdMapping
    public Id getId();

    @EntityView(EmbeddableTestEntityId2.class)
    interface Id {
        IntIdEntitySimpleSubView getIntIdEntity();
        String getKey();
    }

}
