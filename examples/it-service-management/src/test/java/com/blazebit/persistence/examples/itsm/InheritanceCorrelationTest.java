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

package com.blazebit.persistence.examples.itsm;

import com.blazebit.persistence.CTE;
import com.blazebit.persistence.CTEBuilder;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.examples.itsm.model.common.entity.User;
import com.blazebit.persistence.examples.itsm.model.common.repository.UserRepository;
import com.blazebit.persistence.examples.itsm.model.common.view.EntityRevisionDetail;
import com.blazebit.persistence.examples.itsm.model.customer.entity.AbstractCustomer;
import com.blazebit.persistence.examples.itsm.model.customer.entity.Customer;
import com.blazebit.persistence.examples.itsm.model.customer.entity.ServiceContract;
import com.blazebit.persistence.examples.itsm.model.customer.entity.ShippingAddress;
import com.blazebit.persistence.examples.itsm.model.customer.view.AbstractCustomerDetail;
import com.blazebit.persistence.examples.itsm.model.customer.view.CustomerDetail;
import com.blazebit.persistence.examples.itsm.model.customer.view.ShippingAddressSummary;
import com.blazebit.persistence.examples.itsm.model.ticket.entity.Ticket;
import com.blazebit.persistence.examples.itsm.model.ticket.entity.TicketComment;
import com.blazebit.persistence.examples.itsm.model.ticket.entity.TicketComment_;
import com.blazebit.persistence.examples.itsm.model.ticket.entity.TicketCommented;
import com.blazebit.persistence.examples.itsm.model.ticket.entity.TicketFilter;
import com.blazebit.persistence.examples.itsm.model.ticket.entity.TicketHistoryItem;
import com.blazebit.persistence.examples.itsm.model.ticket.entity.TicketStatus;
import com.blazebit.persistence.examples.itsm.model.ticket.entity.Ticket_;
import com.blazebit.persistence.examples.itsm.model.ticket.repository.TicketSummaryRepository;
import com.blazebit.persistence.examples.itsm.model.ticket.view.TicketSummary;
import com.blazebit.persistence.view.CTEProvider;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewInheritance;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingCorrelatedSimple;
import com.blazebit.persistence.view.With;
import org.hibernate.envers.RevisionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;

import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


/**
 * @author Christian Beikov
 * @since 1.4.0
 */
@DataJpaTest(showSql = false)
@ContextConfiguration(classes = BlazeConfiguration.class)
public class InheritanceCorrelationTest {

    @Autowired
    TestEntityManager em;
    @Autowired
    CriteriaBuilderFactory cbf;
    @Autowired
    EntityViewManager evm;

    @Test
    public void testNullInheritanceMapping() {
        EntityViewSetting<TicketHistoryView, CriteriaBuilder<TicketHistoryView>> setting = EntityViewSetting.create(TicketHistoryView.class);
        List<TicketHistoryView> resultList = evm.applySetting(setting, cbf.create(em.getEntityManager(), TicketHistoryItem.class))
                .getResultList();
        System.out.println(resultList);
    }

    @EntityView(TicketHistoryItem.class)
    @EntityViewInheritance
    public static interface TicketHistoryView extends Serializable {
        @IdMapping
        Long getId();
    }

    @EntityView(TicketCommented.class)
    public static interface TicketHistoryActivityView extends TicketHistoryView {
        TicketBase getTicket();
    }

    @EntityView(Ticket.class)
    public static interface TicketBase {
        @IdMapping
        Long getNumber();
        CustomerSummary getCustomer();
    }

    @EntityView(AbstractCustomer.class)
    @With(CustomerSummaryCteProvider.class)
    public static interface CustomerSummary extends AbstractCustomerDetail {

        @IdMapping
        Long getId();

        @MappingCorrelatedSimple(correlated = TicketAggregateCte.class, correlationBasis = "this",
                correlationExpression = "customerId = embedding_view(id)", fetch = FetchStrategy.JOIN)
        TicketAggregateView getTicketAggregates();
    }

    @EntityView(TicketAggregateCte.class)
    public static interface TicketAggregateView extends Serializable {
        String getCustomerId();
        @Mapping("coalesce(totalTicketCount, 0L)")
        long getTotalTicketCount();
        @Mapping("coalesce(openTicketCount, 0L)")
        long getOpenTicketCount();

    }

    @CTE
    @Entity
    public static class TicketAggregateCte implements Serializable {
        @Id
        String customerId;
        long totalTicketCount;
        long openTicketCount;
    }

    public static class CustomerSummaryCteProvider implements CTEProvider {

        private static final String COUNT = "count(*)";
        private static final String CUSTOMER_ID = "customerId";

        @Override
        public void applyCtes(CTEBuilder<?> builder, Map<String, Object> optionalParameters) {
            builder.with(TicketAggregateCte.class).from(Ticket.class, "t")
                .bind(CUSTOMER_ID).select("t.customer.id")
                .bind("totalTicketCount").select(COUNT)
                .bind("openTicketCount").select("count(case when t.open = true then 1 end)")
            .end();
        }

    }


}
