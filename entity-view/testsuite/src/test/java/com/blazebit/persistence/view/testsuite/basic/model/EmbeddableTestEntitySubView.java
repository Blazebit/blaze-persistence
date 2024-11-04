/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.basic.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.testsuite.entity.EmbeddableTestEntity2;
import com.blazebit.persistence.view.testsuite.entity.EmbeddableTestEntityId2;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(EmbeddableTestEntity2.class)
public interface EmbeddableTestEntitySubView extends IdHolderView<EmbeddableTestEntityId2> {

    @Mapping("id.intIdEntity")
    public IntIdEntity getIdIntIdEntity();
    
    @Mapping("id.intIdEntity.id")
    public Integer getIdIntIdEntityId();
    
    @Mapping("id.intIdEntity.name")
    public String getIdIntIdEntityName();
    
    @Mapping("id.key")
    public String getIdKey();
}
