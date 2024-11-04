/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.limit.model;

import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.Limit;
import com.blazebit.persistence.view.Mapping;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
@EntityView(Person.class)
public interface PersonLimitSubselectView extends PersonLimitView {

    @Limit(limit = "1", order = {"age", "id"})
    @Mapping(fetch = FetchStrategy.SUBSELECT)
    public List<DocumentLimitView> getOwnedDocuments();

}
