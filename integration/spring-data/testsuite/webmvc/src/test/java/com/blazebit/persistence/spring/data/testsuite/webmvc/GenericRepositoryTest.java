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

package com.blazebit.persistence.spring.data.testsuite.webmvc;

import com.blazebit.persistence.integration.view.spring.EnableEntityViews;
import com.blazebit.persistence.spring.data.repository.config.EnableBlazeRepositories;
import com.blazebit.persistence.spring.data.testsuite.webmvc.accessor.DocumentAccessor;
import com.blazebit.persistence.spring.data.testsuite.webmvc.accessor.DocumentAccessors;
import com.blazebit.persistence.spring.data.testsuite.webmvc.entity.Document;
import com.blazebit.persistence.spring.data.testsuite.webmvc.entity.Person;
import com.blazebit.persistence.spring.data.testsuite.webmvc.repository.GenericRepository;
import com.blazebit.persistence.spring.data.testsuite.webmvc.tx.TransactionalWorkService;
import com.blazebit.persistence.spring.data.testsuite.webmvc.tx.TxWork;
import com.blazebit.persistence.spring.data.testsuite.webmvc.view.DocumentView;
import com.blazebit.persistence.view.EntityViewManager;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import javax.persistence.EntityManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
@ContextConfiguration(classes = GenericRepositoryTest.TestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class GenericRepositoryTest extends AbstractSpringTest {

    @Autowired
    private TransactionalWorkService transactionalWorkService;

    @Autowired
    private GenericRepository<DocumentView> genericRepository;

    @Test
    public void testFindOne() {
        // Given
        final Document d1 = createDocument("D1");
        final Document d2 = createDocument("D2");

        // When
        DocumentAccessor result1 = DocumentAccessors.of(genericRepository.findOne(d1.getId()));
        DocumentAccessor result2 = DocumentAccessors.of(genericRepository.findOne(d2.getId()));

        // Then
        assertEquals(d1.getId(), result1.getId());
        assertNotNull(result1);

        assertEquals(d2.getId(), result2.getId());
        assertNotNull(result2);
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
                    d.setOwner(owner);
                    owner.getDocuments().add(d);
                    em.merge(owner);
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
            entityManagerFactoryRef = "myEmf")
    static class TestConfig {
    }
}
