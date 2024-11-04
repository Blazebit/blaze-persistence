/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.fetch.embedded.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.testsuite.entity.EmbeddableTestEntity2;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@EntityView(EmbeddableTestEntity2.class)
public interface EmbeddableTestEntityFetchAsViewViewJoin extends EmbeddableTestEntityFetchAsViewView {

    @Mapping(value = "embeddable.name", fetch = FetchStrategy.JOIN)
    String getName();

    @Mapping(value = "embeddable.manyToOne", fetch = FetchStrategy.JOIN)
    EmbeddableTestEntitySimpleFetchView getManyToOne();

    @Mapping(value = "embeddable.oneToMany", fetch = FetchStrategy.JOIN)
    Set<EmbeddableTestEntitySimpleFetchView> getOneToMany();

    @Mapping(value = "embeddable.elementCollection", fetch = FetchStrategy.JOIN)
    Set<IntIdEntityFetchSubView> getElementCollection();

    @Mapping(value = "embeddableSet", fetch = FetchStrategy.JOIN)
    Set<EmbeddableTestEntityEmbeddableFetchSubView> getEmbeddableSet();

    @Mapping(value = "embeddableMap", fetch = FetchStrategy.JOIN)
    Set<EmbeddableTestEntityEmbeddableFetchSubView> getEmbeddableMap();

}
