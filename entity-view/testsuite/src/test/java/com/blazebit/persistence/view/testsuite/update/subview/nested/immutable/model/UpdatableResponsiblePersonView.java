/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.subview.nested.immutable.model;

import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(Person.class)
@UpdatableEntityView
public interface UpdatableResponsiblePersonView extends UpdatablePersonView {

    public FriendPersonView getFriend();

    public void setFriend(FriendPersonView friend);

}
