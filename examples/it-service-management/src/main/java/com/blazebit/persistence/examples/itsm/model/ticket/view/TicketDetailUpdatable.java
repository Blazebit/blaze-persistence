/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.ticket.view;

import com.blazebit.persistence.examples.itsm.model.ticket.entity.Ticket;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;

import com.blazebit.persistence.examples.itsm.model.common.view.UserBase;
import com.blazebit.persistence.examples.itsm.model.customer.view.AbstractCustomerBase;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@EntityView(Ticket.class)
@CreatableEntityView
@UpdatableEntityView
public interface TicketDetailUpdatable extends TicketDetail {

    void setCustomer(AbstractCustomerBase customer);

    void setAuthor(UserBase author);

    void setAssignee(UserBase assignee);

    void setOpen(boolean open);

}
