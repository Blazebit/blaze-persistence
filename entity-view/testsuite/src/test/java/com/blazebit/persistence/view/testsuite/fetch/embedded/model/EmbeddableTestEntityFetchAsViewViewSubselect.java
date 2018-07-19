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
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.testsuite.entity.EmbeddableTestEntity;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@EntityView(EmbeddableTestEntity.class)
public interface EmbeddableTestEntityFetchAsViewViewSubselect extends EmbeddableTestEntityFetchAsViewView {

    @Mapping(value = "embeddable.name", fetch = FetchStrategy.SUBSELECT)
    String getName();

    @Mapping(value = "embeddable.manyToOne", fetch = FetchStrategy.SUBSELECT)
    EmbeddableTestEntitySimpleFetchView getManyToOne();

    @Mapping(value = "embeddable.oneToMany", fetch = FetchStrategy.SUBSELECT)
    Set<EmbeddableTestEntitySimpleFetchView> getOneToMany();

    @Mapping(value = "embeddable.elementCollection", fetch = FetchStrategy.SUBSELECT)
    Set<IntIdEntityFetchSubView> getElementCollection();

    @Mapping(value = "embeddableSet", fetch = FetchStrategy.SUBSELECT)
    Set<EmbeddableTestEntityEmbeddableFetchSubView> getEmbeddableSet();

    @Mapping(value = "embeddableMap", fetch = FetchStrategy.SUBSELECT)
    Set<EmbeddableTestEntityEmbeddableFetchSubView> getEmbeddableMap();

}
