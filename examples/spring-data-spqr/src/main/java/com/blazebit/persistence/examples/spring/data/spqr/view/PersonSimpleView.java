/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.data.spqr.view;

import com.blazebit.persistence.examples.spring.data.spqr.model.Person;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;

import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.6.4
 */
@UpdatableEntityView
@EntityView(Person.class)
public interface PersonSimpleView extends PersonIdView {

    String getName();

    Set<ChildView> getChildren();
}
