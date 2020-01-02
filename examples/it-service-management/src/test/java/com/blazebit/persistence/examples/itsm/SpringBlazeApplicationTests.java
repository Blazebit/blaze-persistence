/*
 * Copyright 2014 - 2020 Blazebit.
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

import com.blazebit.persistence.examples.itsm.model.article.entity.Article;
import com.blazebit.persistence.examples.itsm.model.article.entity.Article_;
import com.blazebit.persistence.examples.itsm.model.article.entity.LocalizedString;
import com.blazebit.persistence.examples.itsm.model.article.entity.LocalizedString_;
import com.blazebit.persistence.examples.itsm.model.article.entity.Person;
import com.blazebit.persistence.examples.itsm.model.article.entity.Person_;
import com.blazebit.persistence.examples.itsm.model.article.repository.ArticleRepository;
import com.blazebit.persistence.examples.itsm.model.article.repository.ArticleViewRepository;
import com.blazebit.persistence.examples.itsm.model.article.repository.PersonRepository;
import com.blazebit.persistence.examples.itsm.model.article.repository.PersonViewRepository;
import com.blazebit.persistence.examples.itsm.model.article.view.ArticleLocalized;
import com.blazebit.persistence.examples.itsm.model.article.view.ArticleView;
import com.blazebit.persistence.examples.itsm.model.article.view.LocalizedStringView;
import com.blazebit.persistence.examples.itsm.model.article.view.PersonView;
import com.blazebit.persistence.examples.itsm.model.common.entity.User;
import com.blazebit.persistence.examples.itsm.model.customer.entity.Customer;
import com.blazebit.persistence.examples.itsm.model.customer.entity.ServiceContractFilter;
import com.blazebit.persistence.examples.itsm.model.customer.entity.ServiceContract_;
import com.blazebit.persistence.examples.itsm.model.customer.entity.ServiceItem_;
import com.blazebit.persistence.examples.itsm.model.customer.repository.CustomerDetailRepository;
import com.blazebit.persistence.examples.itsm.model.customer.repository.CustomerSummaryRepository;
import com.blazebit.persistence.examples.itsm.model.customer.repository.ServiceContractRepository;
import com.blazebit.persistence.examples.itsm.model.customer.repository.ServiceItemRepository;
import com.blazebit.persistence.examples.itsm.model.customer.view.AbstractCustomerDetail;
import com.blazebit.persistence.examples.itsm.model.customer.view.ServiceDetailView;
import com.blazebit.persistence.examples.itsm.model.hotspot.entity.HotspotConfiguration;
import com.blazebit.persistence.examples.itsm.model.hotspot.repository.ConfigurationRepository;
import com.blazebit.persistence.examples.itsm.model.hotspot.repository.ConfigurationViewRepository;
import com.blazebit.persistence.examples.itsm.model.hotspot.view.HotspotConfigurationView;
import com.blazebit.persistence.examples.itsm.model.ticket.entity.Ticket;
import com.blazebit.persistence.examples.itsm.model.ticket.entity.TicketStatus;
import com.blazebit.persistence.examples.itsm.model.ticket.entity.TicketStatus_;
import com.blazebit.persistence.examples.itsm.model.ticket.repository.TicketDetailRepository;
import com.blazebit.persistence.examples.itsm.model.ticket.view.StatusDetail;
import com.blazebit.persistence.examples.itsm.model.ticket.view.StatusDetailRepository;
import com.blazebit.persistence.examples.itsm.model.ticket.view.StatusItem;
import com.blazebit.persistence.examples.itsm.model.ticket.view.TicketDetailUpdatable;
import com.blazebit.persistence.examples.itsm.model.ticket.view.TicketSummary;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@DataJpaTest(showSql = false)
@ContextConfiguration(classes = BlazeConfiguration.class)
public class SpringBlazeApplicationTests {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private EntityViewManager evm;

    private Article article;

    @BeforeEach
    public void populateRepository() {
        CriteriaBuilder cb = this.em.getEntityManager().getCriteriaBuilder();
        Person p1 = new Person();
        p1.setName("Giovanni");
        this.em.persist(p1);

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
        this.article = this.em.persist(a1);

        this.em.flush();
        this.em.clear();
    }

    @Test
    public void testFindAll(@Autowired ArticleRepository repository) {
        List<Article> articles = repository.findAll();
        assertEquals(1, articles.size());
    }

    @Test
    public void testFindByAuthorView(
            @Autowired ArticleViewRepository repository,
            @Autowired PersonViewRepository personRepository) {
        PersonView author = personRepository.getOne(this.article.getAuthor().getId());
        List<ArticleView> articles = repository.findByAuthor(author);
        assertEquals(1, articles.size());
    }

    @Test
    public void testRepositorySave(@Autowired ArticleRepository repository) {
        Article a1 = new Article();
        repository.save(a1);
        assertNotNull(a1.getId());
    }

    @Test
    public void testFindAllViews(@Autowired ArticleViewRepository repository) {
        List<ArticleView> articles = repository.findAll();
        assertEquals(1, articles.size());
    }

    @Test
    public void testFindViewById(@Autowired ArticleViewRepository repository) {
        Optional<ArticleView> view = repository.findById(this.article.getId());
        assertTrue(view.isPresent());
    }

    @Test
    public void testCreatableEntityViewSave(
            @Autowired PersonViewRepository personRepository,
            @Autowired EntityViewManager evm) {
        PersonView person = evm.create(PersonView.class);
        person.setName("Foo");
        personRepository.save(person);

        String name = personRepository.findById(person.getId()).get()
                .getName();
        assertEquals("Foo", name);
    }

    @Test
    @Disabled("PersonView no more updatable")
    public void testUpdatableEntityViewSave(
            @Autowired ArticleViewRepository repository) {
        ArticleView view = repository.findById(this.article.getId()).get();
        view.getAuthor().setName("Foo");
        repository.saveAndFlush(view);

        String name = repository.findById(this.article.getId()).get()
                .getAuthor().getName();
        assertEquals("Foo", name);
    }

    @Test
    public void testCorrelatedSubquery(
            @Autowired PersonViewRepository repository) {
        repository.findAll((root, query, builder) -> {
            Subquery<Article> subquery = query.subquery(Article.class);
            Root<Person> correlation = subquery.correlate(root);
            Join<Person, Article> articles = correlation.join(Person_.articles);
            Predicate predicate = builder.like(articles.get(Article_.slug),
                    "FOO");
            return builder.exists(subquery.select(articles).where(predicate));
        });
    }

    @Test
    public void testExistsByQuery(
            @Autowired ArticleRepository articleRepository) {
        boolean actual = articleRepository.existsByAuthorName("Giovanni");
        assertTrue(actual);
    }

    @Test
    public void testCountByQuery(@Autowired PersonRepository repository) {
        long actual = repository.countByName("Giovanni");
        assertEquals(1, actual);
    }

    @Test
    public void testFindOne(@Autowired ArticleRepository repository) {
        Optional<ArticleLocalized> view = repository
                .findOne(
                        (root, query, builder) -> builder.equal(root.get("id"),
                                this.article.getId()),
                        Locale.ITALIAN, Locale.ENGLISH);
        assertNotNull(view);
    }

    @Test
    public void testNestedEmbeddables(
            @Autowired ConfigurationRepository repository,
            @Autowired ConfigurationViewRepository viewRepository) {
        HotspotConfiguration c = repository.save(new HotspotConfiguration());
        HotspotConfigurationView v = repository.findOne((root, query, builder) -> builder
                .equal(root.get("id"), c.getId()));
        this.em.flush();
        this.em.clear();
        v.getLoginConfiguration().getWelcomeMessage().getLocalizedValues()
                .put(Locale.ENGLISH, "foo");
        v.getLoginConfiguration().getWelcomeMessage().getLocalizedValues()
                .put(Locale.ITALIAN, "foo");
        viewRepository.save(v);
        this.em.flush();
        this.em.clear();
        repository.findOne((root, query, builder) -> builder
                .equal(root.get("id"), c.getId()));
    }

    @Test
    public void testBooleanMapping(
            @Autowired CustomerSummaryRepository repository) {
        repository.findAll();
    }

    @Test
    public void testCriteriaJoin(
            @Autowired ServiceContractRepository repository) {
        ServiceContractFilter filter = new ServiceContractFilter();
        filter.setCustomerCity("foo");
        repository.count(filter);
        repository.findAll(filter);
    }

    @Test
    public void testUpdatableNestedView(
            @Autowired CustomerDetailRepository customerRepository,
            @Autowired EntityViewManager evm) {
        Long id = this.em.persistAndGetId(new Customer(), Long.class);
        AbstractCustomerDetail customer = customerRepository.findById(id).get();
        customer.getServiceDetail().setServiceHours("foo");
        customerRepository.saveAndFlush(customer);
        ServiceDetailView detail = customerRepository.findById(id).get()
                .getServiceDetail();
        assertEquals("foo", detail.getServiceHours());
    }

    @Test
    public void testManyToManyInverseJoin(
            @Autowired ServiceItemRepository repository) {
        repository.findAll((r, q, b) -> {
            Subquery<Boolean> sq = q.subquery(Boolean.class);
            Path<String> contract = sq.correlate(r).join(ServiceItem_.serviceContracts)
                    .get(ServiceContract_.id);
            sq.select(b.literal(true)).where(b.equal(contract, "FOO"));
            return b.exists(sq);
        });
    }

    @Test
    public void createTicketFromView(
            @Autowired TicketDetailRepository ticketDetailRepository) {
        TicketDetailUpdatable ticket = this.evm.create(TicketDetailUpdatable.class);
        ticketDetailRepository.save(ticket);
        ticketDetailRepository.getOne(ticket.getNumber());
    }

    @Test
    public void testLocalizedStringView(
            @Autowired ArticleViewRepository articleRepository) {
        LocalizedStringView title = this.evm.create(LocalizedStringView.class);
        ArticleView article = articleRepository.getOne(this.article.getId());
        title.getLocalizedValues().put(Locale.ENGLISH, "eng");
        title.getLocalizedValues().put(Locale.ITALIAN, "ita");
        article.setTitle(title);
        articleRepository.save(article);
    }

    @Test
    @Disabled("Not sure what Giovanni wanted to test here")
    public void testArticleLocalizedWithJoin(
            @Autowired ArticleRepository repository) {
        Specification<Article> specification = (root, query, builder) -> {
            MapJoin<LocalizedString, Locale, String> path = root.join(Article_.title)
                    .join(LocalizedString_.localizedValues);
            return builder.like(builder.upper(path), "%I%");
        };
        int count = repository
                .findAll(specification, Locale.ITALIAN, Locale.ENGLISH).size();
        assertEquals(1, count);
    }

    @Test
    public void testArticleLocalizedWithSubquery(
            @Autowired ArticleRepository repository) {
        Specification<Article> specification = (root, query, builder) -> {
            Subquery<Boolean> subquery = query.subquery(Boolean.class);
            MapJoin<LocalizedString, Locale, String> path = subquery.correlate(root).join(Article_.title)
                    .join(LocalizedString_.localizedValues);
            Predicate predicate = builder.like(builder.upper(path), "%I%");
            return builder.exists(subquery.where(predicate));
        };
        int count = repository
                .findAll(specification, Locale.ITALIAN, Locale.ENGLISH).size();
        assertEquals(1, count);
    }

    @Test
    public void testStatusWithNextView(
            @Autowired StatusDetailRepository repository) {
        TicketStatus s1 = this.em.persist(new TicketStatus("foo"));
        TicketStatus s2 = this.em.persist(new TicketStatus("bar"));
        TicketStatus s3 = this.em.persist(new TicketStatus("baz"));
        s3.getNext().add(s1);
        s3.getNext().add(s2);
        this.em.persistAndFlush(s3);

        repository.findAll((r, q, b) -> {
            Subquery<TicketStatus> sq = q.subquery(TicketStatus.class);
            Root<TicketStatus> sqRoot = sq.from(TicketStatus.class);
            SetJoin<TicketStatus, TicketStatus> next = sqRoot.join(TicketStatus_.next);
            sq.select(next)
                    .where(b.equal(sqRoot.get(TicketStatus_.id), s3.getId()));
            return r.in(sq);
        });
    }

    @Test
    public void testAddStatusSubtype(
            @Autowired StatusDetailRepository repository) {
        TicketStatus s1 = this.em.persist(new TicketStatus("foo"));
        TicketStatus s2 = this.em.persist(new TicketStatus("bar"));
        Object s1Id = this.em.persistAndGetId(s1);
        Object s2Id = this.em.persistAndGetId(s2);

        EntityManager entityManager = this.em.getEntityManager();

        StatusDetail detail = this.evm.find(entityManager, StatusDetail.class, s1Id);
        StatusItem item = this.evm.find(entityManager, StatusItem.class, s2Id);
        detail.getNext().add(item);
    }

    @Test
    public void testOptimisticLocking(
            @Autowired ArticleViewRepository repository) {
        ArticleView view = repository.findById(this.article.getId()).get();
        view.setSlug("foo");
        repository.save(view);
    }

    @Test
    public void testNullInheritanceMapping() {
        User user = this.em.persist(new User());
        Ticket ticket = new Ticket();
        Object id = this.em.persistAndGetId(ticket);
        EntityViewSetting<TicketSummary, com.blazebit.persistence.CriteriaBuilder<TicketSummary>> setting = EntityViewSetting.create(TicketSummary.class);
        setting.addOptionalParameter("observer", user);
        TicketSummary summary = this.evm.find(this.em.getEntityManager(), setting, id);
        AbstractCustomerDetail customer = summary.getCustomer();
        assertNull(customer);
    }

}
