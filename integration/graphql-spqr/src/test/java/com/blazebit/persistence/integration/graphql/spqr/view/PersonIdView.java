/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.graphql.spqr.view;

import com.blazebit.persistence.integration.graphql.spqr.model.Person;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;

/**
 * @author Christian Beikov
 * @since 1.6.4
 */
@EntityView(Person.class)
public interface PersonIdView {

    @IdMapping
    Long getId();

    void setId(Long id);

}
