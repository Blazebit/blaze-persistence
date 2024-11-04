/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.examples.quarkus.base.view;

import com.blazebit.persistence.examples.quarkus.base.entity.DocumentType;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@CreatableEntityView
@EntityView(DocumentType.class)
public interface DocumentTypeCreateView extends DocumentTypeView {
    void setId(String id);

    void setName(String name);
}
