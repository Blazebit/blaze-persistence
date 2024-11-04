/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.quarkus.base.view;

import com.blazebit.persistence.examples.quarkus.base.entity.Document;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.UpdatableEntityView;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@UpdatableEntityView
@CreatableEntityView
@EntityView(Document.class)
public interface DocumentUpdateView {

    @IdMapping
    Long getId();

    String getName();
    void setName(String name);

    Long getAge();
    void setAge(Long age);
}
