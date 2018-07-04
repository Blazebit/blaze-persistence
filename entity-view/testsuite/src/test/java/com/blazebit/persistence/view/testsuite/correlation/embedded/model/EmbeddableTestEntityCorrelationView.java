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

package com.blazebit.persistence.view.testsuite.correlation.embedded.model;

import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntity;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityId;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingCorrelatedSimple;

/**
 *
 * @author Christian Beikov
 * @since 1.2.1
 */
@EntityView(EmbeddableTestEntity.class)
public interface EmbeddableTestEntityCorrelationView {

    @IdMapping
    public Id getId();

    @EntityView(EmbeddableTestEntityId.class)
    interface Id {
        String getValue();
        String getKey();
    }

    @MappingCorrelatedSimple(
            correlationBasis = "id",
            correlated = EmbeddableTestEntity.class,
            correlationExpression = "id IN correlationKey",
            fetch = FetchStrategy.SELECT
    )
    public SimpleEmbeddableTestEntityCorrelationView getSelf();

    @MappingCorrelatedSimple(
            correlationBasis = "id.value",
            correlated = EmbeddableTestEntity.class,
            correlationExpression = "VIEW_ROOT(id.value) IN correlationKey",
            fetch = FetchStrategy.SELECT
    )
    public SimpleEmbeddableTestEntityCorrelationView getSelfSelect();

    @MappingCorrelatedSimple(
            correlationBasis = "id.value",
            correlated = EmbeddableTestEntity.class,
            correlationExpression = "VIEW_ROOT(id.value) IN correlationKey",
            fetch = FetchStrategy.SUBSELECT
    )
    public SimpleEmbeddableTestEntityCorrelationView getSelfSubselect();

    @EntityView(EmbeddableTestEntity.class)
    public interface SimpleEmbeddableTestEntityCorrelationView {
        @Mapping("id.value")
        String getValue();
    }

}
