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

package com.blazebit.persistence.spring.hateoas.webmvc;

import com.blazebit.persistence.integration.view.spring.EnableEntityViews;
import com.blazebit.persistence.spring.data.impl.repository.BlazePersistenceRepositoryFactoryBean;
import com.blazebit.persistence.spring.hateoas.webmvc.entity.Document;
import com.blazebit.persistence.spring.hateoas.webmvc.entity.Person;
import com.blazebit.persistence.spring.hateoas.webmvc.tx.TransactionalWorkService;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
@ContextConfiguration(classes = {DocumentControllerTest.TestConfig.class, HateoasAwareBlazePersistenceWebConfiguration.class})
public class DocumentControllerTest extends AbstractSpringWebMvcTest {

    @Autowired
    private TransactionalWorkService transactionalWorkService;

    @Test
    public void testDocumentControllerHeaders() throws Exception {
        // Given
        Document d1 = createDocument("D1");
        Document d2 = createDocument("D2");

        // When / Then
        mockMvc.perform(get("/documents?size=1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().stringValues(HttpHeaders.LINK, (Matcher<Iterable<String>>) (Matcher<?>) hasItem(containsString("{\"id\":" + d1.getId() + "}"))))
                .andExpect(content().string(containsString("\"someInstant\"")));
    }

    @Test
    public void testDocumentControllerHateoas() throws Exception {
        // Given
        Document d1 = createDocument("D1");
        Document d2 = createDocument("D2");

        // When / Then
        mockMvc.perform(get("/documents?size=1").accept("application/hal+json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.links[?(@.rel == 'next')].href", (Matcher<Iterable<String>>) (Matcher<?>) hasItem(containsString("{\"id\":" + d1.getId() + "}"))));
    }

    private Document createDocument(String name) {
        return createDocument(name, null);
    }

    private Document createDocument(final String name, final Person owner) {
        return createDocument(name, null, 0L, owner);
    }

    private Document createDocument(final String name, final String description, final long age, final Person owner) {
        return transactionalWorkService.txGet((em, evm) -> {
            Document d = new Document(name);
            d.setDescription(description);
            d.setAge(age);
            d.setOwner(owner);
            em.persist(d);
            return d;
        });
    }

    @Configuration
    @ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*TestConfig"))
    @ImportResource("classpath:/com/blazebit/persistence/spring/hateoas/webmvc/application-config.xml")
    @EnableWebMvc
    @EnableEntityViews(basePackages = "com.blazebit.persistence.spring.hateoas.webmvc.view")
    @EnableJpaRepositories(
            basePackages = "com.blazebit.persistence.spring.hateoas.webmvc.repository",
            entityManagerFactoryRef = "myEmf",
            repositoryFactoryBeanClass = BlazePersistenceRepositoryFactoryBean.class)
    static class TestConfig {
    }
}
