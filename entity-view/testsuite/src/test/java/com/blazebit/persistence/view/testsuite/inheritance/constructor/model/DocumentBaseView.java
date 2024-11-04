/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.inheritance.constructor.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewInheritance;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.testsuite.entity.Document;

import java.util.Collection;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(Document.class)
@EntityViewInheritance
public abstract class DocumentBaseView {

    private final Collection<SimplePersonSubView> people;

    public DocumentBaseView(@Mapping("owner") Collection<SimplePersonSubView> owners) {
        this.people = owners;
    }

    @IdMapping
    public abstract Long getId();

    public abstract String getName();

    public abstract long getAge();

    public abstract int getIdx();

    public Collection<SimplePersonSubView> getPeople() {
        return people;
    }
}
