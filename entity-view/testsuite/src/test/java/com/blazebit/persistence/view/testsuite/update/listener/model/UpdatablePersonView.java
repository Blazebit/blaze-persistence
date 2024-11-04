/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.listener.model;

import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@UpdatableEntityView
@CreatableEntityView
@EntityView(Person.class)
public interface UpdatablePersonView extends PersonView {

    public void setName(String name);

}
