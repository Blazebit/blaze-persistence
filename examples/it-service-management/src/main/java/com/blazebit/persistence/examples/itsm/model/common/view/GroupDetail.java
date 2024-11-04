/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.common.view;

import java.util.Set;

import com.blazebit.persistence.examples.itsm.model.common.entity.Group;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.UpdatableMapping;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@EntityView(Group.class)
@CreatableEntityView
@UpdatableEntityView
public interface GroupDetail {

    @IdMapping
    Long getId();

    @UpdatableMapping
    Set<UserDetail> getUsers();

}
