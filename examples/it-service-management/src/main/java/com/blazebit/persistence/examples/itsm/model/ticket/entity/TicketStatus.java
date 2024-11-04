/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.ticket.entity;

import com.blazebit.persistence.examples.itsm.model.article.entity.LocalizedEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@Entity
public class TicketStatus extends LocalizedEntity
        implements Comparable<TicketStatus> {

    private boolean initial;

    private boolean active;

    private boolean assigneeRequired;

    private boolean activityRequired;

    private boolean scheduledActivityRequired;

    private boolean publicCommentRequired;

    private boolean appliedChangeRequired;

    private boolean incidentReportRequired;

    private String theme;

    @ManyToMany
    private SortedSet<TicketStatus> next = new TreeSet<>();

    public TicketStatus(String name) {
        this.setName(name);
    }

    public SortedSet<TicketStatus> getNext() {
        return this.next;
    }

    @Override
    public int compareTo(TicketStatus o) {
        return Comparator.comparing(TicketStatus::getName).compare(this, o);
    }

}
