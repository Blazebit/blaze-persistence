/*
 * Copyright 2014 - 2023 Blazebit.
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

import static com.blazebit.persistence.spring.data.testsuite.webmvc.controller.DocumentController.APPLICATION_VND_BLAZEBIT_UPDATE_1_JSON;
import static com.blazebit.persistence.spring.data.testsuite.webmvc.controller.DocumentController.APPLICATION_VND_BLAZEBIT_UPDATE_2_JSON;
import static com.blazebit.persistence.spring.data.testsuite.webmvc.controller.DocumentController.APPLICATION_VND_BLAZEBIT_UPDATE_3_JSON;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.blazebit.persistence.integration.view.spring.EnableEntityViews;
import com.blazebit.persistence.spring.data.repository.config.EnableBlazeRepositories;
import com.blazebit.persistence.spring.data.testsuite.webmvc.entity.Document;
import com.blazebit.persistence.spring.data.testsuite.webmvc.entity.Person;
import com.blazebit.persistence.spring.data.testsuite.webmvc.tx.TransactionalWorkService;
import com.blazebit.persistence.spring.data.testsuite.webmvc.view.DocumentCreateOrUpdateView;
import com.blazebit.persistence.spring.data.testsuite.webmvc.view.DocumentCreateOrUpdateViewBuilder;
import com.blazebit.persistence.spring.data.testsuite.webmvc.view.DocumentUpdateView;
import com.blazebit.persistence.spring.data.webmvc.impl.BlazePersistenceWebConfiguration;
import java.util.Collections;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.web.config.SpringDataWebConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

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
                .andExpect(jsonPath("$.content[0].id", is(d2.getId().intValue())))
                .andExpect(content().string(containsString("\"someInstant\"")));
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
                .contentType(MediaType.APPLICATION_JSON)
                .accept(APPLICATION_VND_BLAZEBIT_UPDATE_1_JSON))
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
                .contentType(MediaType.APPLICATION_JSON)
                .accept(APPLICATION_VND_BLAZEBIT_UPDATE_2_JSON))
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
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(updateView.getName())));
    }


    // Test for #1733
    @Test
    public void testIdResetOnRequestFailure() throws Exception {
        // Given
        DocumentCreateOrUpdateView updateView = new DocumentCreateOrUpdateViewBuilder.Init(Collections.emptyMap())
            .withName("D1")
            .build();
        DocumentCreateOrUpdateView createView = new DocumentCreateOrUpdateViewBuilder.Init(Collections.emptyMap())
            .withName("D2")
            .build();

        // When / Then
        // This update sets the ID in the com.blazebit.persistence.spring.data.webmvc.impl.json.EntityViewIdValueHolder
        // The update should fail because ID 50 does not exist. Still, the com.blazebit.persistence.spring.data.webmvc.impl.json.EntityViewIdValueHolder
        // should be reset.
        mockMvc.perform(put("/documents/{id}", 50L)
                .content(toJsonWithId(updateView))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(APPLICATION_VND_BLAZEBIT_UPDATE_3_JSON))
            .andExpect(status().isConflict());
        // This subsequent create request runs in the same thread as the previous update request hence will access
        // the same value in the ThreadLocal inside com.blazebit.persistence.spring.data.webmvc.impl.json.EntityViewIdValueHolder.
        // If the com.blazebit.persistence.spring.data.webmvc.impl.json.EntityViewIdValueHolder has not been reset
        // correctly after the previous request this request will wrongly re-use the ID 50, and an update will be
        // attempted because the entity view used is both updatable and creatable. The update would fail like before.
        mockMvc.perform(post("/documents")
                .content(toJsonWithId(createView))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
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
    @EnableBlazeRepositories(
            basePackages = "com.blazebit.persistence.spring.data.testsuite.webmvc.repository",
            entityManagerFactoryRef = "myEmf")
    static class TestConfig {
    }
}
