/*
 * Copyright 2014 - 2019 Blazebit.
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
