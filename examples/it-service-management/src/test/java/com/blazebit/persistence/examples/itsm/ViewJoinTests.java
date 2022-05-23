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

package com.blazebit.persistence.examples.itsm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.blazebit.persistence.examples.itsm.model.common.entity.User;
import com.blazebit.persistence.examples.itsm.model.common.repository.UserRepository;
import com.blazebit.persistence.examples.itsm.model.ticket.repository.TicketSummaryRepository;
import com.blazebit.persistence.examples.itsm.model.ticket.view.TicketSummary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;

import com.blazebit.persistence.examples.itsm.model.ticket.entity.Ticket;
import com.blazebit.persistence.examples.itsm.model.ticket.entity.TicketComment;
import com.blazebit.persistence.examples.itsm.model.ticket.entity.TicketComment_;
import com.blazebit.persistence.examples.itsm.model.ticket.entity.TicketFilter;
import com.blazebit.persistence.examples.itsm.model.ticket.entity.Ticket_;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;


/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@DataJpaTest(showSql = false)
@ContextConfiguration(classes = BlazeConfiguration.class)
public class ViewJoinTests {

    @Autowired
    TestEntityManager em;

    @Test
    public void testTicketSummary(
            @Autowired TicketSummaryRepository ticketRepository) {
        User u1 = new User();
        u1.setName("Foo");
        u1 = this.em.persistFlushFind(u1);
        User u2 = new User();
        u2.setName("Bar");
        u2 = this.em.persistFlushFind(u2);

        Ticket t1 = new Ticket();
        t1.setAuthor(u1);
        t1.setOpen(true);
        t1 = this.em.persistFlushFind(t1);

        Ticket t2 = new Ticket();
        t2.setAuthor(u2);
        t2 = this.em.persistFlushFind(t2);

        TicketComment c1 = new TicketComment();
        c1.setTicket(t1);
        c1.setAuthor(u1);
        this.em.persistAndFlush(c1);

        TicketComment c2 = new TicketComment();
        c2.setTicket(t1);
        c2.setAuthor(u2);
        this.em.persistAndFlush(c2);

        List<TicketSummary> list = ticketRepository.findAll(u1, Locale.ENGLISH, Locale.ENGLISH);
        assertEquals(2, list.size());

        Long id1 = list.get(0).getNumber();
        Long id2 = list.get(1).getNumber();
        assertFalse(id1.equals(id2));
    }

    @Test
    public void testTicketSummarySpecification(
            @Autowired TicketSummaryRepository ticketRepository) {
        User u = new User();
        u.setName("Foo");
        final User user = this.em.persistFlushFind(u);
        ticketRepository.findAll((root, query, criteriaBuilder) -> {
            Predicate ticketSeen = criteriaBuilder.isNotMember(user,
                    root.get(Ticket_.seen));

            Expression<Set<TicketComment>> comments = root.get(Ticket_.comments);
            Expression<Integer> commentCount = criteriaBuilder.size(comments);

            Subquery<Long> seenCommentCountSubquery = query.subquery(Long.class);
            SetJoin<Ticket, TicketComment> commentPath = seenCommentCountSubquery.correlate(root)
                    .join(Ticket_.comments);
            Expression<Long> seenCommentCount = criteriaBuilder.count(commentPath);
            seenCommentCountSubquery.select(seenCommentCount)
                    .where(criteriaBuilder.isMember(user,
                            commentPath.get(TicketComment_.seen)));

            Predicate hasUnseenComments = criteriaBuilder.ge(criteriaBuilder
                    .diff(commentCount, seenCommentCountSubquery), 0L);

            return criteriaBuilder.or(ticketSeen, hasUnseenComments);
        }, user);
    }

    @Test
    public void testTicketSummaryCorrelations(
            @Autowired UserRepository userRepository,
            @Autowired TicketSummaryRepository ticketRepository) {
        User u1 = new User();
        u1.setName("Foo");
        u1 = this.em.persistFlushFind(u1);
        User u2 = new User();
        u2.setName("Bar");
        u2 = this.em.persistFlushFind(u2);

        Ticket t1 = new Ticket();
        t1.setAuthor(u1);
        t1.setOpen(true);
        t1 = this.em.persistFlushFind(t1);
        this.addComments(t1, 7);

        Ticket t2 = new Ticket();
        t2.setAuthor(u2);
        t2 = this.em.persistFlushFind(t2);
        this.addComments(t2, 2);

        Ticket t3 = new Ticket();
        t3.setAuthor(u2);
        t3 = this.em.persistFlushFind(t3);
        this.addComments(t3, 2);

        Ticket t4 = new Ticket();
        t4.setAuthor(u2);
        t4 = this.em.persistFlushFind(t4);
        this.addComments(t4, 3);

        int size = ticketRepository
                .findAll(new TicketFilter(), u1, PageRequest.of(0, 3)).size();
        assertEquals(3, size);
    }

    private void addComments(Ticket ticket, int numberOfComments) {
        for (int i = 0; i < numberOfComments; i++) {
            TicketComment c = new TicketComment();
            c.setTicket(ticket);
            this.em.persistAndFlush(c);
        }
    }

}
