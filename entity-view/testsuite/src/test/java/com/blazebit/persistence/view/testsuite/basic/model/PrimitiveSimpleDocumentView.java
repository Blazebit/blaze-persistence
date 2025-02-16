/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.view.testsuite.basic.model;

import com.blazebit.persistence.testsuite.entity.PrimitiveDocument;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(PrimitiveDocument.class)
public interface PrimitiveSimpleDocumentView {
    
    @IdMapping
    public long getId();

    @Mapping("id")
    public long getDocId();

    public String getName();

    public boolean isDeleted();

}
