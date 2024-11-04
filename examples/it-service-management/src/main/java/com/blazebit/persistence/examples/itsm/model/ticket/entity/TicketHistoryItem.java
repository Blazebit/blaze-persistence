/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.ticket.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;

import com.blazebit.persistence.examples.itsm.model.common.entity.User;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class TicketHistoryItem
        implements Comparable<TicketHistoryItem>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Instant created;

    @ManyToOne
    private Ticket ticket;

    @ManyToOne
    private User author;

    protected TicketHistoryItem() {
    }

    public Long getId() {
        return this.id;
    }

    public Instant getCreated() {
        return this.created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public Ticket getTicket() {
        return this.ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public User getAuthor() {
        return this.author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    @Override
    public int compareTo(TicketHistoryItem o) {
        return this.created.compareTo(o.created);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TicketHistoryItem)) {
            return false;
        }
        TicketHistoryItem other = (TicketHistoryItem) obj;
        return Objects.equals(this.id, other.id);
    }

}
