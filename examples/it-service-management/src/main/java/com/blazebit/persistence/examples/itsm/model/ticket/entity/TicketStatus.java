/*
 * Copyright 2014 - 2021 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.examples.itsm.model.ticket.entity;

import com.blazebit.persistence.examples.itsm.model.article.entity.LocalizedEntity;
import com.blazebit.persistence.examples.itsm.model.article.entity.LocalizedEntity_;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OrderBy;
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

    @OrderBy(LocalizedEntity_.NAME)
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
