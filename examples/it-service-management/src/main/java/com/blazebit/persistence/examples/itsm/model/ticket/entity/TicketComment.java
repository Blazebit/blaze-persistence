/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.ticket.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;

import org.hibernate.envers.Audited;

import com.blazebit.persistence.examples.itsm.model.article.entity.Attachment;
import com.blazebit.persistence.examples.itsm.model.common.entity.User;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@Entity
@Audited
public class TicketComment implements Comparable<TicketComment>, Serializable {

    @Id
    @GeneratedValue
    Long id;

    @ManyToOne
    Ticket ticket;

    @ManyToOne
    User author;

    @ManyToOne
    User assignee;

    Instant creationInstant = Instant.now();

    @OneToMany
    private Set<User> seen = new HashSet<>();

    @OrderColumn
    @ElementCollection
    private List<Attachment> attachments = new ArrayList<>();

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

    public User getAssignee() {
        return this.assignee;
    }

    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }

    public Long getId() {
        return this.id;
    }

    public Instant getCreationInstant() {
        return this.creationInstant;
    }

    public Set<User> getSeen() {
        return this.seen;
    }

    public List<Attachment> getAttachments() {
        return this.attachments;
    }

    @Override
    public int compareTo(TicketComment o) {
        return creationInstant.compareTo(o.creationInstant);
    }
}
