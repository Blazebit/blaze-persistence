/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.ticket.view;

import com.blazebit.persistence.examples.itsm.model.ticket.entity.TicketCommented;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@EntityView(TicketCommented.class)
@CreatableEntityView
@UpdatableEntityView
public interface TicketCommentedDetail extends TicketHistoryDetail {

    TicketCommentDetail getComment();

    void setComment(TicketCommentDetail comment);

}
