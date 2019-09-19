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

package com.blazebit.persistence.spring.data.testsuite;

import com.blazebit.persistence.integration.view.spring.EnableEntityViews;
import com.blazebit.persistence.spring.data.impl.repository.BlazePersistenceRepositoryFactoryBean;
import com.blazebit.persistence.spring.data.testsuite.accessor.DocumentAccessor;
import com.blazebit.persistence.spring.data.testsuite.accessor.DocumentAccessors;
import com.blazebit.persistence.spring.data.testsuite.entity.Document;
import com.blazebit.persistence.spring.data.testsuite.entity.Person;
import com.blazebit.persistence.spring.data.testsuite.repository.GenericRepository;
import com.blazebit.persistence.spring.data.testsuite.tx.TransactionalWorkService;
import com.blazebit.persistence.spring.data.testsuite.tx.TxWork;
import com.blazebit.persistence.spring.data.testsuite.view.DocumentView;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import javax.persistence.EntityManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@ContextConfiguration(classes = DocumentRepositoryTest.TestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class DocumentRepositoryTest extends AbstractSpringTest {

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
            public Document work(EntityManager em) {
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
    @ImportResource("classpath:/com/blazebit/persistence/spring/data/impl/application-config.xml")
    @EnableEntityViews(basePackages = "com.blazebit.persistence.spring.data.testsuite.view")
    @EnableJpaRepositories(
            basePackages = "com.blazebit.persistence.spring.data.testsuite.repository",
            entityManagerFactoryRef = "myEmf",
            repositoryFactoryBeanClass = BlazePersistenceRepositoryFactoryBean.class)
    static class TestConfig {
    }
}
