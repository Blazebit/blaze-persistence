/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.jaxrs.jsonb.testsuite.view;

import com.blazebit.persistence.integration.jaxrs.jsonb.testsuite.entity.Document;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.UpdatableEntityView;

/**
 * @author Moritz Becker
 * @since 1.6.4
 */
@UpdatableEntityView
@EntityView(Document.class)
public interface DocumentUpdateView {

    @IdMapping
    Long getId();

    String getName();
    void setName(String name);
}
