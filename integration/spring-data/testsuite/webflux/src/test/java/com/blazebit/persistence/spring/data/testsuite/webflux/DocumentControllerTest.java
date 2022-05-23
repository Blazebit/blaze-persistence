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

package com.blazebit.persistence.spring.data.testsuite.webflux;

import com.blazebit.persistence.spring.data.testsuite.webflux.controller.DocumentController;
import com.blazebit.persistence.spring.data.testsuite.webflux.entity.Document;
import com.blazebit.persistence.spring.data.testsuite.webflux.entity.Person;
import com.blazebit.persistence.spring.data.testsuite.webflux.tx.TransactionalWorkService;
import com.blazebit.persistence.spring.data.testsuite.webflux.view.DocumentUpdateView;
import com.blazebit.persistence.spring.data.webflux.impl.BlazePersistenceWebConfiguration;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Mono;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@WebFluxTest(DocumentController.class)
@ContextConfiguration(classes = {DocumentControllerTest.TestConfig.class, BlazePersistenceWebConfiguration.class})
public class DocumentControllerTest extends AbstractSpringTest {

    @Autowired
    private TransactionalWorkService transactionalWorkService;

    @Test
    public void testUpdateDocument1() throws Exception {
        // Given
        Document d1 = createDocument("D1");

        // When
        DocumentUpdateView updateView = transactionalWorkService.doTxWork((em, evm) -> {
            return evm.find(em, DocumentUpdateView.class, d1.getId());
        });
        updateView.setName("D2");
        webTestClient.put()
                .uri("/documents/{id}", d1.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.parseMediaType("application/vnd.blazebit.update1+json"))
                .body(Mono.just(toJsonWithoutId(updateView)), byte[].class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                // Then
                .jsonPath("$.name").isEqualTo(updateView.getName());
    }

    @Test
    public void testUpdateDocument2() throws Exception {
        // Given
        Document d1 = createDocument("D1");

        // When
        DocumentUpdateView updateView = transactionalWorkService.doTxWork((em, evm) -> {
            return evm.find(em, DocumentUpdateView.class, d1.getId());
        });
        updateView.setName("D2");
        webTestClient.put()
                .uri("/documents/{id}", d1.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.parseMediaType("application/vnd.blazebit.update2+json"))
                .body(Mono.just(toJsonWithoutId(updateView)), byte[].class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                // Then
                .jsonPath("$.name").isEqualTo(updateView.getName())
                .jsonPath("$.someInstant").exists();
    }

    @Test
    public void testUpdateDocument3() throws Exception {
        // Given
        Document d1 = createDocument("D1");

        // When
        DocumentUpdateView updateView = transactionalWorkService.doTxWork((em, evm) -> {
            return evm.find(em, DocumentUpdateView.class, d1.getId());
        });
        updateView.setName("D2");
        webTestClient.put()
                .uri("/documents")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(toJsonWithId(updateView)), byte[].class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                // Then
                .jsonPath("$.name").isEqualTo(updateView.getName());
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
    @ImportResource("classpath:/com/blazebit/persistence/spring/data/testsuite/webflux/application-config.xml")
    @ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*TestConfig"))
    static class TestConfig {
    }
}
