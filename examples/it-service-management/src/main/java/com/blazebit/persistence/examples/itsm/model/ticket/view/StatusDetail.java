/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.ticket.view;

import com.blazebit.persistence.examples.itsm.model.ticket.entity.TicketStatus;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.UpdatableEntityView;

import com.blazebit.persistence.examples.itsm.model.article.view.LocalizedEntityView;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@EntityView(TicketStatus.class)
@CreatableEntityView
@UpdatableEntityView(strategy = FlushStrategy.ENTITY)
public interface StatusDetail
        extends StatusBase, StatusWithNext, LocalizedEntityView<TicketStatus> {

    void setInitial(boolean initial);

    void setActive(boolean active);

    void setAssigneeRequired(boolean assigneeRequired);

    void setActivityRequired(boolean activityRequired);

    void setScheduledActivityRequired(boolean scheduledActivityRequired);

    void setPublicCommentRequired(boolean publicCommentRequired);

    void setAppliedChangeRequired(boolean appliedChangeRequired);

    void setIncidentReportRequired(boolean incidentReportRequired);

    void setTheme(String theme);

}
