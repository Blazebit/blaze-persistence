/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.rollback.model;

import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@CreatableEntityView
@EntityView(Person.class)
public interface CreatePersonRollbackView extends PersonNameView {

    public void setName(String name);

}
