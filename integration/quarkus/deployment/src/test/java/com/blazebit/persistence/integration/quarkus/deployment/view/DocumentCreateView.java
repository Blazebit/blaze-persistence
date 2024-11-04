/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.integration.quarkus.deployment.view;

import com.blazebit.persistence.integration.quarkus.deployment.entity.Document;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@EntityView(Document.class)
@CreatableEntityView
public interface DocumentCreateView {

    @IdMapping
    Long getId();

    String getName();

    void setName(String name);
}
