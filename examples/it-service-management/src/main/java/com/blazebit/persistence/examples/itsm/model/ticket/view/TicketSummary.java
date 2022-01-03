/*
 * Copyright 2014 - 2022 Blazebit.
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
import java.util.Collections;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.blazebit.persistence.CTE;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.examples.itsm.model.ticket.entity.Ticket;
import com.blazebit.persistence.examples.itsm.model.ticket.entity.TicketComment;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingCorrelatedSimple;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.SubqueryProvider;

import com.blazebit.persistence.examples.itsm.model.common.view.AuditedView;
import com.blazebit.persistence.examples.itsm.model.common.view.UserBase;
import com.blazebit.persistence.examples.itsm.model.customer.view.AbstractCustomerDetail;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@EntityView(Ticket.class)
public interface TicketSummary extends AuditedView {

    @IdMapping
    Long getNumber();

    Instant getCreationInstant();

    @Mapping("max(comments.creationInstant)")
    Instant getLastCommentInstant();

    @Mapping("size(comments)")
    long getCommentCount();

    @Mapping(value = "comments.author", fetch = FetchStrategy.SUBSELECT)
    Set<UserBase> getCommentAuthors();

    AbstractCustomerDetail getCustomer();

    @MappingSubquery(value = SeenProvider.class, subqueryAlias = "seen",
            expression = "coalesce(seen, false)")
    boolean isSeen();

    @MappingCorrelatedSimple(correlated = TicketCommentsCTE.class,
            correlationBasis = "this",
            correlationExpression = "ticketNumber = embedding_view(number)",
            fetch = FetchStrategy.JOIN)
    TicketCommentAggregate getCommentAggregates();

    default long getUnseenCommentsCount() {
        return this.getCommentCount()
                - this.getCommentAggregates().getSeenCommentCount();
    }

    default boolean isHasUnseenComments() {
        return this.getUnseenCommentsCount() > 0;
    }

    @EntityView(TicketCommentsCTE.class)
    interface TicketCommentAggregate {

        String getTicketNumber();

        @Mapping("coalesce(totalCommentCount, 0L)")
        long getTotalCommentCount();

        @Mapping("coalesce(seenCommentCount, 0L)")
        long getSeenCommentCount();

    }

    @CTE
    @Entity
    class TicketCommentsCTE {
        @Id
        String ticketNumber;
        long totalCommentCount;
        long seenCommentCount;
    }

    @MappingSubquery(CteRegistrationSubqueryProvider.class)
    Integer getUnused();

    class CteRegistrationSubqueryProvider implements SubqueryProvider {

        @Override
        public <T> T createSubquery(SubqueryInitiator<T> subqueryInitiator) {
            // @formatter:off
            return subqueryInitiator
                .fromValues(Integer.class, "v", Collections.singletonList(1))
                .select("1")
                .with(TicketCommentsCTE.class)
                    .from(TicketComment.class, "c")
                    .bind("ticketNumber").select("c.ticket.number")
                    .bind("totalCommentCount").select("count(*)")
                    .bind("seenCommentCount").select("count(*)")
                        .where(":observer").isMemberOf("c.seen")
                    .end()
                .end();
            // @formatter:on
        }

    }

    class SeenProvider implements SubqueryProvider {

        @Override
        public <T> T createSubquery(SubqueryInitiator<T> subqueryInitiator) {
            return subqueryInitiator.from("embedding_view(seen)", "s")
                    .select("true").where("s").eqExpression(":observer").end();
        }

    }

}
