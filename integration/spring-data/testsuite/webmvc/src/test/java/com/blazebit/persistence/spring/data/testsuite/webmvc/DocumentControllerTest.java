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
import com.blazebit.persistence.spring.data.testsuite.webmvc.tx.TransactionalWorkService;
import com.blazebit.persistence.spring.data.testsuite.webmvc.view.DocumentUpdateView;
import com.blazebit.persistence.spring.data.webmvc.impl.BlazePersistenceWebConfiguration;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.config.SpringDataWebConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Moritz Becker
 * @since 1.4.0
 */
@ContextConfiguration(classes = {DocumentControllerTest.TestConfig.class, BlazePersistenceWebConfiguration.class, SpringDataWebConfiguration.class})
public class DocumentControllerTest extends AbstractSpringWebMvcTest {

    @Autowired
    private TransactionalWorkService transactionalWorkService;

    @Test
    public void testDocumentControllerDefaults() throws Exception {
        // Given
        Document d1 = createDocument("D1");
        Document d2 = createDocument("D2");

        // When / Then
        mockMvc.perform(get("/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keysetPage.lowest.tuple.length()", is(1)))
                .andExpect(jsonPath("$.keysetPage.lowest.tuple[0]", is(d1.getId().intValue())))
                .andExpect(jsonPath("$.keysetPage.highest.tuple.length()", is(1)))
                .andExpect(jsonPath("$.keysetPage.highest.tuple[0]", is(d2.getId().intValue())));
    }

    @Test
    public void testDocumentControllerOffsetParameter() throws Exception {
        // Given
        createDocument("D1");
        Document d2 = createDocument("D2");

        // When / Then
        mockMvc.perform(get("/documents?offset={offset}&size={size}", 1, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfElements", is(1)))
                .andExpect(jsonPath("$.content[0].id", is(d2.getId().intValue())));
    }

    @Test
    public void testUpdateDocument1() throws Exception {
        // Given
        Document d1 = createDocument("D1");

        // When / Then
        DocumentUpdateView updateView = transactionalWorkService.doTxWork((em, evm) -> {
            return evm.find(em, DocumentUpdateView.class, d1.getId());
        });
        updateView.setName("D2");
        mockMvc.perform(put("/documents/{id}", d1.getId())
                .content(toJsonWithoutId(updateView))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept("application/vnd.blazebit.update1+json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(updateView.getName())));
    }

    @Test
    public void testUpdateDocument2() throws Exception {
        // Given
        Document d1 = createDocument("D1");

        // When / Then
        DocumentUpdateView updateView = transactionalWorkService.doTxWork((em, evm) -> {
            return evm.find(em, DocumentUpdateView.class, d1.getId());
        });
        updateView.setName("D2");
        mockMvc.perform(put("/documents/{id}", d1.getId())
                .content(toJsonWithoutId(updateView))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept("application/vnd.blazebit.update2+json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(updateView.getName())));
    }

    @Test
    public void testUpdateDocument3() throws Exception {
        // Given
        Document d1 = createDocument("D1");

        // When / Then
        DocumentUpdateView updateView = transactionalWorkService.doTxWork((em, evm) -> {
            return evm.find(em, DocumentUpdateView.class, d1.getId());
        });
        updateView.setName("D2");
        mockMvc.perform(put("/documents")
                .content(toJsonWithId(updateView))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(updateView.getName())));
    }

    private Document createDocument(String name) {
        return createDocument(name, null);
    }

    private Document createDocument(final String name, final Person owner) {
        return createDocument(name, null, 0L, owner);
    }

    private Document createDocument(final String name, final String description, final long age, final Person owner) {
        return transactionalWorkService.doTxWork((em, evm) -> {
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
    @ImportResource("classpath:/com/blazebit/persistence/spring/data/testsuite/webmvc/application-config.xml")
    @EnableWebMvc
    @EnableEntityViews(basePackages = "com.blazebit.persistence.spring.data.testsuite.webmvc.view")
    @EnableJpaRepositories(
            basePackages = "com.blazebit.persistence.spring.data.testsuite.webmvc.repository",
            entityManagerFactoryRef = "myEmf",
            repositoryFactoryBeanClass = BlazePersistenceRepositoryFactoryBean.class)
    static class TestConfig {
    }
}
