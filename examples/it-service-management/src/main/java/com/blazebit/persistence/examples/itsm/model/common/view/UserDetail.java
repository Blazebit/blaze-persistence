/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.common.view;

import java.time.Instant;
import java.util.Optional;

import com.blazebit.persistence.examples.itsm.model.common.entity.User;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@EntityView(User.class)
public interface UserDetail extends UserBase {

    @Mapping("sum(case when (sessions.destroyed = false) then 1 else 0 end)")
    long getActiveSessionCount();

    @Mapping("max(sessions.heartbeatInstant)")
    Optional<Instant> getLastActiveInstant();

}
