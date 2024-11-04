/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.testsuite.webmvc.view;

import com.blazebit.persistence.spring.data.testsuite.webmvc.entity.Document;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Moritz Becker
 * @since 1.6.9
 */
@CreatableEntityView
@UpdatableEntityView
@EntityView(Document.class)
public interface DocumentCreateOrUpdateView extends DocumentUpdateView {

    @JsonIgnore
    @IdMapping
    @Override
    Long getId();
}
