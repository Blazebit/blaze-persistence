/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.fetch.embedded.model;

import com.blazebit.persistence.view.Mapping;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public interface EmbeddableTestEntityFetchAsViewView extends EmbeddableTestEntitySimpleFetchView {

    @Mapping(value = "embeddable.name")
    String getName();

    @Mapping(value = "embeddable.manyToOne")
    EmbeddableTestEntitySimpleFetchView getManyToOne();

    @Mapping(value = "embeddable.oneToMany")
    Set<EmbeddableTestEntitySimpleFetchView> getOneToMany();

    @Mapping(value = "embeddable.elementCollection")
    Set<IntIdEntityFetchSubView> getElementCollection();

    @Mapping(value = "embeddableSet")
    Set<EmbeddableTestEntityEmbeddableFetchSubView> getEmbeddableSet();

    @Mapping(value = "embeddableMap")
    Set<EmbeddableTestEntityEmbeddableFetchSubView> getEmbeddableMap();

}
