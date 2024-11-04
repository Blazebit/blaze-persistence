/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.ticket.view;

import java.time.Instant;

import com.blazebit.persistence.examples.itsm.model.ticket.entity.TicketHistoryItem;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewInheritance;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.UpdatableEntityView;

import com.blazebit.persistence.examples.itsm.model.common.view.UserBase;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@EntityView(TicketHistoryItem.class)
@UpdatableEntityView
@EntityViewInheritance
public interface TicketHistoryDetail {

    @IdMapping
    Long getId();

    Instant getCreated();

    void setCreated(Instant created);

    UserBase getAuthor();

    void setAuthor(UserBase author);

}
