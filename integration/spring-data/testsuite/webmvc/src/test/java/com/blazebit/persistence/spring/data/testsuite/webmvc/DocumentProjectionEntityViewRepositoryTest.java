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
package com.blazebit.persistence.spring.data.testsuite.webmvc;

import com.blazebit.persistence.integration.view.spring.EnableEntityViews;
import com.blazebit.persistence.spring.data.impl.repository.BlazePersistenceRepositoryFactoryBean;
import com.blazebit.persistence.spring.data.testsuite.webmvc.entity.Document;
import com.blazebit.persistence.spring.data.testsuite.webmvc.entity.Person;
import com.blazebit.persistence.spring.data.testsuite.webmvc.projection.DocumentIdProjection;
import com.blazebit.persistence.spring.data.testsuite.webmvc.projection.DocumentProjection;
import com.blazebit.persistence.spring.data.testsuite.webmvc.repository.DocumentProjectionEntityViewRepository;
import com.blazebit.persistence.spring.data.testsuite.webmvc.tx.TransactionalWorkService;
import com.blazebit.persistence.spring.data.testsuite.webmvc.tx.TxWork;
import com.blazebit.persistence.view.EntityViewManager;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;

import javax.persistence.EntityManager;

import static org.junit.Assert.assertEquals;

/**
 * @author Moritz Becker
 * @since 1.6.0
 */
@ContextConfiguration(classes = DocumentProjectionEntityViewRepositoryTest.TestConfig.class)
public class DocumentProjectionEntityViewRepositoryTest extends AbstractSpringTest {

    @Autowired
    private TransactionalWorkService transactionalWorkService;
    @Autowired
    private DocumentProjectionEntityViewRepository documentProjectionEntityViewRepository;

    @Test
    public void testStaticProjection() {
        // Given
        final Document d1 = createDocument("D1");
        final Document d2 = createDocument("D2");

        // When
        DocumentIdProjection d = documentProjectionEntityViewRepository.findByName("D1").get(0);

        // Then
        assertEquals(d1.getId(), d.getId());
    }

    @Test
    public void testDynamicProjection() {
        // Given
        final Document d1 = createDocument("D1");
        final Document d2 = createDocument("D2");

        // When
        DocumentProjection d = documentProjectionEntityViewRepository.findByName("D1", DocumentProjection.class).get(0);

        // Then
        assertEquals(d1.getId(), d.getId());
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
    @EnableJpaRepositories(
            basePackages = "com.blazebit.persistence.spring.data.testsuite.webmvc.repository",
            entityManagerFactoryRef = "myEmf",
            repositoryFactoryBeanClass = BlazePersistenceRepositoryFactoryBean.class
    )
    static class TestConfig {
    }
}
