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
import com.blazebit.persistence.spring.data.rest.impl.BlazePersistenceWebConfiguration;
import com.blazebit.persistence.spring.data.testsuite.entity.Document;
import com.blazebit.persistence.spring.data.testsuite.entity.Person;
import com.blazebit.persistence.spring.data.testsuite.tx.TransactionalWorkService;
import com.blazebit.persistence.spring.data.testsuite.tx.TxWork;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.persistence.EntityManager;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Moritz Becker
 * @since 1.4.0
 */
@ContextConfiguration(classes = {DocumentControllerTest.TestConfig.class, BlazePersistenceWebConfiguration.class})
public class DocumentControllerTest extends AbstractSpringWebMvcTest {

    @Autowired
    private TransactionalWorkService transactionalWorkService;

    @Test
    public void testDocumentControllerDefaults() throws Exception {
        // Given
        Document d1 = createDocument("D1");
        Document d2 = createDocument("D2");

        // When / Then
        mockMvc.perform(get("/document-views"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keysetPage.lowest.tuple.length()", is(1)))
                .andExpect(jsonPath("$.keysetPage.lowest.tuple[0]", is(d1.getId().intValue())))
                .andExpect(jsonPath("$.keysetPage.highest.tuple.length()", is(1)))
                .andExpect(jsonPath("$.keysetPage.highest.tuple[0]", is(d2.getId().intValue())));
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
                d.setOwner(owner);
                em.persist(d);
                return d;
            }
        });
    }

    @Configuration
    @ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*TestConfig"))
    @ImportResource("classpath:/com/blazebit/persistence/spring/data/impl/application-config.xml")
    @EnableWebMvc
    @EnableEntityViews(basePackages = "com.blazebit.persistence.spring.data.testsuite.view")
    @EnableJpaRepositories(
            basePackages = "com.blazebit.persistence.spring.data.testsuite.repository",
            entityManagerFactoryRef = "myEmf",
            repositoryFactoryBeanClass = BlazePersistenceRepositoryFactoryBean.class)
    static class TestConfig { }
}
