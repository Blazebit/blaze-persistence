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

package com.blazebit.persistence.spring.data.testsuite.webflux;

import com.blazebit.persistence.spring.data.testsuite.webflux.controller.PersonController;
import com.blazebit.persistence.spring.data.testsuite.webflux.entity.Person;
import com.blazebit.persistence.spring.data.testsuite.webflux.tx.TransactionalWorkService;
import com.blazebit.persistence.spring.data.testsuite.webflux.view.PersonUpdateView;
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
@WebFluxTest(PersonController.class)
@ContextConfiguration(classes = {PersonControllerTest.TestConfig.class, BlazePersistenceWebConfiguration.class})
public class PersonControllerTest extends AbstractSpringTest {

    @Autowired
    private TransactionalWorkService transactionalWorkService;

    @Test
    public void testUpdatePerson() throws Exception {
        // Given
        Person p1 = createPerson("P1");

        // When
        PersonUpdateView updateView = transactionalWorkService.doTxWork((em, evm) -> {
            return evm.find(em, PersonUpdateView.class, p1.getId());
        });
        updateView.setName("P2");
        webTestClient.put()
                .uri("/persons/{id}", p1.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(toJsonWithoutId(updateView)), byte[].class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                // Then
                .jsonPath("$.name").isEqualTo(updateView.getName());
    }

    private Person createPerson(String name) {
        return createPerson(name, 0L);
    }

    private Person createPerson(String name, long age) {
        return transactionalWorkService.doTxWork((em, evm) -> {
            Person p = new Person(name);
            p.setAge(age);
            em.persist(p);
            return p;
        });
    }

    @Configuration
    @ImportResource("classpath:/com/blazebit/persistence/spring/data/testsuite/webflux/application-config.xml")
    @ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*TestConfig"))
    static class TestConfig {
    }
}
