/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.ticket.view;

import com.blazebit.persistence.examples.itsm.model.ticket.entity.TicketStatus;
import com.blazebit.persistence.view.EntityView;

import com.blazebit.persistence.examples.itsm.model.article.view.LocalizedEntityId;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@EntityView(TicketStatus.class)
public interface StatusId extends LocalizedEntityId<TicketStatus> {

}
