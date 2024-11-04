/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.testsuite.webmvc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.blazebit.persistence.integration.view.spring.EnableEntityViews;
import com.blazebit.persistence.spring.data.repository.config.EnableBlazeRepositories;
import com.blazebit.persistence.spring.data.testsuite.webmvc.entity.Document;
import com.blazebit.persistence.spring.data.testsuite.webmvc.entity.Identifiable;
import com.blazebit.persistence.spring.data.testsuite.webmvc.entity.Person;
import com.blazebit.persistence.spring.data.testsuite.webmvc.repository.ReadOnlyDocumentEntityRepository;
import com.blazebit.persistence.spring.data.testsuite.webmvc.repository.ReadOnlyDocumentViewRepository;
import com.blazebit.persistence.spring.data.testsuite.webmvc.tx.TransactionalWorkService;
import com.blazebit.persistence.spring.data.testsuite.webmvc.tx.TxWork;
import com.blazebit.persistence.spring.data.testsuite.webmvc.view.DocumentView;
import com.blazebit.persistence.view.EntityViewManager;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.persistence.EntityManager;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Moritz Becker
 * @since 1.6.15
 */
@ContextConfiguration(classes = ReadOnlyDocumentRepositoryTest.TestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ReadOnlyDocumentRepositoryTest extends AbstractSpringTest {

    private static final Pageable UNPAGED;

    static {
        Pageable unpaged = null;
        try {
            Method unpagedMethod = Class.forName("org.springframework.data.domain.Pageable").getMethod("unpaged");
            unpaged = (Pageable) unpagedMethod.invoke(null);
        } catch (Exception e) {
            // ignore
        }
        UNPAGED = unpaged;
    }

    @Autowired
    private ReadOnlyDocumentEntityRepository documentEntityRepository;
    @Autowired
    private ReadOnlyDocumentViewRepository documentViewRepository;
    @Autowired
    private TransactionalWorkService transactionalWorkService;

    @Test
    public void dynamicProjectionWithUnpagedPagination() {
        // Given
        Document d1 = createDocument("A");
        Document d2 = createDocument("A");
        createDocument("B");

        // When
        Page<Document> entityPage = documentEntityRepository.findByName("A", Document.class, UNPAGED);
        Page<DocumentView> viewPage = documentViewRepository.findByName("A", DocumentView.class, UNPAGED);

        // Then
        assertEquals(entityPage.getTotalElements(), viewPage.getTotalElements());
        assertEquals(entityPage.getTotalPages(), viewPage.getTotalPages());
        assertEquals(entityPage.getNumber(), viewPage.getNumber());
        assertEquals(entityPage.getSize(), viewPage.getSize());
        assertEquals(entityPage.getNumberOfElements(), viewPage.getNumberOfElements());
        assertEquals(entityPage.hasNext(), viewPage.hasNext());
        assertEquals(entityPage.hasPrevious(), viewPage.hasPrevious());
        assertEquals(entityPage.hasContent(), viewPage.hasContent());
        assertEquals(entityPage.isFirst(), viewPage.isFirst());
        assertEquals(entityPage.isLast(), viewPage.isLast());
        assertEquals(entityPage.getContent().size(), viewPage.getContent().size());
        List<Long> entityResultIds = entityPage.getContent().stream().map(Identifiable::getId).collect(Collectors.toList());
        assertTrue(entityResultIds.contains(d1.getId()));
        assertTrue(entityResultIds.contains(d2.getId()));
        List<Long> viewResultIds = viewPage.getContent().stream().map(Identifiable::getId).collect(Collectors.toList());
        assertEquals(entityResultIds, viewResultIds);
    }

    private Document createDocument(String name) {
        return createDocument(name, null);
    }

    private Document createDocument(final String name, final Person owner) {
        return createDocument(name, null, 0L, owner);
    }

    private Document createDocument(final String name, final String description, final long age, final Person owner) {
        return transactionalWorkService.doTxWork(new TxWork<Document>() {
            @Override
            public Document work(EntityManager em, EntityViewManager evm) {
                Document d = new Document(name);
                d.setDescription(description);
                d.setAge(age);
                em.persist(d);
                if (owner != null) {
                    d.setOwner(em.getReference(Person.class, owner.getId()));
                }
                return d;
            }
        });
    }

    @Configuration
    @ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*TestConfig"))
    @ImportResource("classpath:/com/blazebit/persistence/spring/data/testsuite/webmvc/application-config.xml")
    @EnableEntityViews(basePackages = "com.blazebit.persistence.spring.data.testsuite.webmvc.view")
    @EnableBlazeRepositories(
            basePackages = "com.blazebit.persistence.spring.data.testsuite.webmvc.repository",
            entityManagerFactoryRef = "myEmf"
    )
    static class TestConfig {
    }
}
