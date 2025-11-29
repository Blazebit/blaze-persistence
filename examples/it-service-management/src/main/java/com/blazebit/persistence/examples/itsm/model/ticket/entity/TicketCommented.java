/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.ticket.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@Entity
public class TicketCommented extends TicketHistoryItem {

    @OneToOne
    private TicketComment comment;

    public TicketComment getComment() {
        return this.comment;
    }

    public void setComment(TicketComment comment) {
        this.comment = comment;
    }

}
