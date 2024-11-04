/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.fetch.embedded.model;

import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.testsuite.entity.EmbeddableTestEntity2;
import com.blazebit.persistence.view.testsuite.entity.EmbeddableTestEntitySimpleEmbeddable2;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public interface EmbeddableTestEntityFetchAsEntityView extends EmbeddableTestEntitySimpleFetchView {

    @Mapping(value = "embeddable.name")
    String getName();

    @Mapping(value = "embeddable.manyToOne")
    EmbeddableTestEntity2 getManyToOne();

    @Mapping(value = "embeddable.oneToMany")
    Set<EmbeddableTestEntity2> getOneToMany();

    @Mapping(value = "embeddable.elementCollection")
    Set<IntIdEntity> getElementCollection();

    @Mapping(value = "embeddableSet")
    Set<EmbeddableTestEntitySimpleEmbeddable2> getEmbeddableSet();

    @Mapping(value = "embeddableMap")
    Set<EmbeddableTestEntitySimpleEmbeddable2> getEmbeddableMap();

}
