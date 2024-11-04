/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.ticket.view;

import java.io.Serializable;

import com.blazebit.persistence.examples.itsm.model.ticket.entity.Ticket;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@EntityView(Ticket.class)
public interface TicketBase extends Serializable {

    @IdMapping
    Long getNumber();

    String getSubject();

}
