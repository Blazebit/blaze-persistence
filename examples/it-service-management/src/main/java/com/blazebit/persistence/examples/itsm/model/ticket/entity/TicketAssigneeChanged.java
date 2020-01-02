/*
 * Copyright 2014 - 2020 Blazebit.
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

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

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
