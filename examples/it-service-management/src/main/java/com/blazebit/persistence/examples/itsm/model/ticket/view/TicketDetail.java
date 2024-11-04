/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.ticket.view;

import com.blazebit.persistence.examples.itsm.model.ticket.entity.Ticket;
import com.blazebit.persistence.view.EntityView;

import com.blazebit.persistence.examples.itsm.model.common.view.UserBase;
import com.blazebit.persistence.examples.itsm.model.customer.view.AbstractCustomerBase;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@EntityView(Ticket.class)
public interface TicketDetail extends TicketWithHistory {

    AbstractCustomerBase getCustomer();

    UserBase getAuthor();

    UserBase getAssignee();

    boolean isOpen();

}
