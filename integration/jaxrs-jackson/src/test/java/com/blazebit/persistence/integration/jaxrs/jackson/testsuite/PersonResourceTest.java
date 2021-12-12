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

package com.blazebit.persistence.integration.jaxrs.jackson.testsuite;

import com.blazebit.persistence.integration.jaxrs.jackson.testsuite.view.PersonCreateView;
import com.blazebit.persistence.integration.jaxrs.jackson.testsuite.view.PersonUpdateView;
import com.blazebit.persistence.integration.jaxrs.jackson.testsuite.view.PersonView;
import com.blazebit.persistence.integration.jaxrs.jackson.testsuite.entity.Person;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public class PersonResourceTest extends AbstractJaxrsTest {

    @Test
    public void testUpdatePerson() throws Exception {
        // Given
        Person p1 = createPerson("P1");

        // When
        PersonUpdateView updateView = transactional(em -> {
            return evm.find(em, PersonUpdateView.class, p1.getId());
        });
        updateView.setName("P2");
        PersonView updatedView = webTarget.path("/persons/{id}")
                .resolveTemplate("id", p1.getId())
                .request()
                .buildPut(Entity.entity(toJsonWithoutId(updateView), MediaType.APPLICATION_JSON_TYPE))
                .invoke(PersonViewImpl.class);

        // Then
        assertEquals(updateView.getName(), updatedView.getName());
    }

    @Test
    public void testCreatePerson() throws Exception {
        // When
        PersonCreateView personCreateView = evm.create(PersonCreateView.class);
        personCreateView.setId(UUID.randomUUID().toString());
        personCreateView.setName("P1");
        Response response = webTarget.path("/persons")
                .request()
                .buildPost(Entity.entity(toJsonWithId(personCreateView), MediaType.APPLICATION_JSON_TYPE))
                .invoke();

        // Then
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        PersonView createdPerson = transactional(em -> {
            return evm.find(em, PersonView.class, personCreateView.getId());
        });
        assertNotNull(createdPerson);
    }

    private Person createPerson(String name) {
        return createPerson(name, 0L);
    }

    private Person createPerson(String name, long age) {
        return transactional(em -> {
            Person p = new Person(name);
            p.setAge(age);
            em.persist(p);
            return p;
        });
    }
}
