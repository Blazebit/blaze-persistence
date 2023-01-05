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

import com.blazebit.persistence.integration.view.spring.EnableEntityViews;
import com.blazebit.persistence.spring.data.repository.config.EnableBlazeRepositories;
import com.blazebit.persistence.spring.data.testsuite.webmvc.entity.Person;
import com.blazebit.persistence.spring.data.testsuite.webmvc.tx.TransactionalWorkService;
import com.blazebit.persistence.spring.data.testsuite.webmvc.view.PersonUpdateView;
import com.blazebit.persistence.spring.data.webmvc.impl.BlazePersistenceWebConfiguration;
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

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@ContextConfiguration(classes = {PersonControllerTest.TestConfig.class, BlazePersistenceWebConfiguration.class, SpringDataWebConfiguration.class})
public class PersonControllerTest extends AbstractSpringWebMvcTest {

    @Autowired
    private TransactionalWorkService transactionalWorkService;

    @Test
    public void testUpdatePerson() throws Exception {
        // Given
        Person p1 = createPerson("P1");

        // When / Then
        PersonUpdateView updateView = transactionalWorkService.doTxWork((em, evm) -> {
            return evm.find(em, PersonUpdateView.class, p1.getId());
        });
        updateView.setName("P2");
        mockMvc.perform(put("/persons/{id}", p1.getId())
                .content(toJsonWithoutId(updateView))
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(updateView.getName())));
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
