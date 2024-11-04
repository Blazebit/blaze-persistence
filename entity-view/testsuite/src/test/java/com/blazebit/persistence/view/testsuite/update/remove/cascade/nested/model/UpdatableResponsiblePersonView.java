/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.remove.cascade.nested.model;

import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.CascadeType;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.UpdatableMapping;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(Person.class)
@UpdatableEntityView
public interface UpdatableResponsiblePersonView extends UpdatablePersonView {

    @UpdatableMapping(cascade = { CascadeType.AUTO, CascadeType.DELETE })
    public FriendPersonView getFriend();

    public void setFriend(FriendPersonView friend);

}
