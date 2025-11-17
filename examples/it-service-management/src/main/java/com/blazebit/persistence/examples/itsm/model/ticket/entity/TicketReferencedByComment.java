/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.ticket.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@Entity
public class TicketReferencedByComment extends TicketHistoryItem {

    @ManyToOne
    private TicketComment referencingComment;

    public TicketComment getReferencingComment() {
        return this.referencingComment;
    }

    public void setReferencingComment(TicketComment referencingComment) {
        this.referencingComment = referencingComment;
    }

}
