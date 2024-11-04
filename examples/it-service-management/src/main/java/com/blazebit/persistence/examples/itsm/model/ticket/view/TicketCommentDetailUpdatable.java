/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.ticket.view;

import com.blazebit.persistence.examples.itsm.model.ticket.entity.TicketComment;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;

import com.blazebit.persistence.examples.itsm.model.common.view.UserBase;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@EntityView(TicketComment.class)
@CreatableEntityView
@UpdatableEntityView
public interface TicketCommentDetailUpdatable extends TicketCommentDetail {

    void setAuthor(UserBase author);

    void setAssignee(UserBase assignee);

}
