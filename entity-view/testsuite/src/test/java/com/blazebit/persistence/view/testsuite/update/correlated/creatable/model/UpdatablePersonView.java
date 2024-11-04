/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.correlated.creatable.model;

import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.testsuite.basic.model.IdHolderView;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@UpdatableEntityView
@EntityView(Person.class)
public interface UpdatablePersonView extends IdHolderView<Long> {

    public String getName();

    public void setName(String name);
}
