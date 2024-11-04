/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.basic.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.testsuite.entity.EmbeddableTestEntity2;
import com.blazebit.persistence.view.testsuite.entity.EmbeddableTestEntityId2;

import java.io.Serializable;

/**
 *
 * @author Christian Beikov
 * @since 1.2.1
 */
@EntityView(EmbeddableTestEntity2.class)
public interface EmbeddableTestEntityIdView extends IdHolderView<EmbeddableTestEntityIdView.Id> {

    @Mapping("id.key")
    public String getIdKey();

    @Mapping("embeddable.name")
    String getName();

    @EntityView(EmbeddableTestEntityId2.class)
    interface Id extends Serializable {
        @Mapping("intIdEntity.id")
        Integer getIntIdEntityId();
        String getKey();
    }

}
