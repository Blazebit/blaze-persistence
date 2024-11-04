/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.ticket.view;

import java.util.List;

import com.blazebit.persistence.examples.itsm.model.ticket.entity.TicketComment;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;

import com.blazebit.persistence.examples.itsm.model.article.view.AttachmentView;
import com.blazebit.persistence.examples.itsm.model.common.view.UserBase;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@EntityView(TicketComment.class)
public interface TicketCommentDetail {

    @IdMapping
    Long getId();

    UserBase getAuthor();

    UserBase getAssignee();

    List<AttachmentView> getAttachments();

}
