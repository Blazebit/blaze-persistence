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

package com.blazebit.persistence.integration.jaxrs.testsuite;

import com.blazebit.persistence.integration.jaxrs.testsuite.entity.Person;
import com.blazebit.persistence.integration.jaxrs.testsuite.view.PersonUpdateView;
import com.blazebit.persistence.integration.jaxrs.testsuite.view.PersonView;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import static org.junit.Assert.assertEquals;

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
