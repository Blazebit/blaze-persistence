/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.examples.quarkus.base.view;

import com.blazebit.persistence.examples.quarkus.base.entity.DocumentType;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@UpdatableEntityView
@EntityView(DocumentType.class)
public interface DocumentTypeUpdateView extends DocumentTypeView {
    void setName(String name);
}
