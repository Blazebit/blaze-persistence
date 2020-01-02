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

package com.blazebit.persistence.examples.itsm.model.ticket.view;

import java.time.Instant;

import com.blazebit.persistence.examples.itsm.model.ticket.entity.TicketHistoryItem;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewInheritance;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.UpdatableEntityView;

import com.blazebit.persistence.examples.itsm.model.common.view.UserBase;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@EntityView(TicketHistoryItem.class)
@UpdatableEntityView
@EntityViewInheritance
public interface TicketHistoryDetail {

    @IdMapping
    Long getId();

    Instant getCreated();

    void setCreated(Instant created);

    UserBase getAuthor();

    void setAuthor(UserBase author);

}
