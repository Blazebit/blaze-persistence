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
