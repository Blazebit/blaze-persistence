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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;

import com.blazebit.persistence.examples.itsm.model.ticket.repository.TicketDetailRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.blazebit.persistence.view.EntityViewManager;

import com.blazebit.persistence.examples.itsm.model.article.entity.Article;
import com.blazebit.persistence.examples.itsm.model.article.entity.Person;
import com.blazebit.persistence.examples.itsm.model.article.repository.ArticleRepository;
import com.blazebit.persistence.examples.itsm.model.article.repository.ArticleViewRepository;
import com.blazebit.persistence.examples.itsm.model.article.repository.PersonRepository;
import com.blazebit.persistence.examples.itsm.model.article.view.ArticleView;
import com.blazebit.persistence.examples.itsm.model.common.entity.User;
import com.blazebit.persistence.examples.itsm.model.common.repository.UserRepository;
import com.blazebit.persistence.examples.itsm.model.common.view.EntityRevisionDetail;
import com.blazebit.persistence.examples.itsm.model.ticket.repository.TicketSummaryRepository;
import com.blazebit.persistence.examples.itsm.model.ticket.view.TicketDetailUpdatable;
import com.blazebit.persistence.examples.itsm.model.ticket.view.TicketSummary;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@DataJpaTest(showSql = false)
@ContextConfiguration(classes = BlazeConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class EnversTests {

    @Autowired
    private EntityViewManager evm;

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void articleViewHasEnversMetadata(
            @Autowired PersonRepository personRepository,
            @Autowired ArticleRepository articleRepository,
            @Autowired ArticleViewRepository repository) {
        Person p1 = new Person();
        p1.setName("Giovanni");
        personRepository.save(p1);

        Article a1 = new Article();
        a1.setAuthor(p1);
        Map<Locale, String> title = a1.getTitle().getLocalizedValues();
        title.put(Locale.ENGLISH, "English");
        title.put(Locale.ITALIAN, "Italiano");
        title.put(Locale.GERMAN, "Deutsch");
        Map<Locale, String> content = a1.getTitle().getLocalizedValues();
        content.put(Locale.ENGLISH, "English");
        content.put(Locale.ITALIAN, "Italiano");
        content.put(Locale.GERMAN, "Deutsch");
        Long id = articleRepository.saveAndFlush(a1).getId();

        ArticleView view = repository.findById(id).get();

        EntityRevisionDetail metadata = view.getCreationMetadata();
        assertNotNull(metadata);

        Instant timestamp = metadata.getTimestamp();
        assertNotNull(timestamp);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void ticketSummaryHasEnversMetadata(
            @Autowired TicketDetailRepository ticketDetailRepository,
            @Autowired TicketSummaryRepository ticketSummaryRepository,
            @Autowired UserRepository userRepository) {
        User user = userRepository.save(new User());
        TicketDetailUpdatable ticket = this.evm.create(TicketDetailUpdatable.class);
        Long number = ticketDetailRepository.saveAndFlush(ticket).getNumber();

        TicketSummary summary = ticketSummaryRepository.findByNumber(number, user)
                .get();

        EntityRevisionDetail metadata = summary.getCreationMetadata();
        assertNotNull(metadata);

        Instant timestamp = metadata.getTimestamp();
        assertNotNull(timestamp);
    }

}
