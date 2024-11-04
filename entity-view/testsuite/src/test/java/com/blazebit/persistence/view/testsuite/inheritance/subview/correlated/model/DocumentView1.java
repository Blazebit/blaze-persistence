/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.inheritance.subview.correlated.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.MappingCorrelatedSimple;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@EntityView(Document.class)
public interface DocumentView1 extends DocumentView {

    @MappingCorrelatedSimple(correlated = Person.class, correlationBasis = "owner", correlationExpression = "this = correlationKey", fetch = FetchStrategy.JOIN)
    public PersonBaseView1 getOwner();
}
