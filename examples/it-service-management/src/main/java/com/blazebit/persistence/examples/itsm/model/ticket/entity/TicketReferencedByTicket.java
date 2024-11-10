/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.ticket.entity;

import jakarta.persistence.Entity;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@Entity
public class TicketReferencedByTicket extends TicketHistoryItem {

    private Ticket referencingTicket;

    public Ticket getReferencingTicket() {
        return this.referencingTicket;
    }

    public void setReferencingTicket(Ticket referencingTicket) {
        this.referencingTicket = referencingTicket;
    }

}
