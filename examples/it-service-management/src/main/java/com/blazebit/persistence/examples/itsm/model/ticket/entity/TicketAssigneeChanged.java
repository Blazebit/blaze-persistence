/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.ticket.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import com.blazebit.persistence.examples.itsm.model.common.entity.User;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@Entity
public class TicketAssigneeChanged extends TicketHistoryItem {

    @ManyToOne
    private User assigneeBefore;

    @ManyToOne
    private User assigneeAfter;

    public User getAssigneeBefore() {
        return this.assigneeBefore;
    }

    public void setAssigneeBefore(User assigneeBefore) {
        this.assigneeBefore = assigneeBefore;
    }

    public User getAssigneeAfter() {
        return this.assigneeAfter;
    }

    public void setAssigneeAfter(User assigneeAfter) {
        this.assigneeAfter = assigneeAfter;
    }

}
