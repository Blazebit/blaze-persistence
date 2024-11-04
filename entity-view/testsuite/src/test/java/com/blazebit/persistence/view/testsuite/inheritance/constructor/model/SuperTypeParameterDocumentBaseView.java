/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.inheritance.constructor.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewInheritance;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(Document.class)
@EntityViewInheritance
public abstract class SuperTypeParameterDocumentBaseView {

    private final String name;

    public SuperTypeParameterDocumentBaseView(@Mapping("name") String name) {
        this.name = name;
    }

    @IdMapping
    public abstract Long getId();

    public String getName() {
        return name;
    }
}
