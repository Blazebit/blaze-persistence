/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.fetch.subview.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@EntityView(Document.class)
public interface DocumentSubselectSubviewTestView {

    @IdMapping
    public Long getId();

    public String getName();

    @Mapping(fetch = FetchStrategy.SUBSELECT)
    public Person getOwner();

}
