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

package com.blazebit.persistence.integration.jaxrs.jsonb.testsuite;

import com.blazebit.persistence.integration.jaxrs.jsonb.testsuite.entity.Document;
import com.blazebit.persistence.integration.jaxrs.jsonb.testsuite.entity.Person;
import com.blazebit.persistence.integration.jaxrs.jsonb.testsuite.view.DocumentUpdateView;
import com.blazebit.persistence.integration.jaxrs.jsonb.testsuite.view.DocumentView;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import static org.junit.Assert.assertEquals;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class DocumentResourceTest extends AbstractJaxrsTest {

    @Test
    public void testUpdateDocument1() {
        // Given
        Document d1 = createDocument("D1");

        // When
        DocumentUpdateView updateView = transactional(em -> {
            return evm.find(em, DocumentUpdateView.class, d1.getId());
        });
        updateView.setName("D2");
        DocumentView updatedView = webTarget.path("/documents/{id}")
                .resolveTemplate("id", d1.getId())
                .request()
                .buildPut(Entity.entity(toJsonWithoutId(updateView), "application/vnd.blazebit.update1+json"))
                .invoke(DocumentViewImpl.class);

        // Then
        assertEquals(updateView.getName(), updatedView.getName());
    }

    @Test
    public void testUpdateDocument2() {
        // Given
        Document d1 = createDocument("D1");

        // When
        DocumentUpdateView updateView = transactional(em -> {
            return evm.find(em, DocumentUpdateView.class, d1.getId());
        });
        updateView.setName("D2");
        DocumentView updatedView = webTarget.path("/documents/{id}")
                .resolveTemplate("id", d1.getId())
                .request()
                .buildPut(Entity.entity(toJsonWithoutId(updateView), "application/vnd.blazebit.update2+json"))
                .invoke(DocumentViewImpl.class);

        // Then
        assertEquals(updateView.getName(), updatedView.getName());
    }

    @Test
    public void testUpdateDocument3() {
        // Given
        Document d1 = createDocument("D1");

        // When
        DocumentUpdateView updateView = transactional(em -> {
            return evm.find(em, DocumentUpdateView.class, d1.getId());
        });
        updateView.setName("D2");
        DocumentView updatedView = webTarget.path("/documents")
                .request()
                .buildPut(Entity.entity(toJsonWithId(updateView), MediaType.APPLICATION_JSON_TYPE))
                .invoke(DocumentViewImpl.class);

        // Then
        assertEquals(updateView.getName(), updatedView.getName());
    }

    private Document createDocument(String name) {
        return createDocument(name, null);
    }

    private Document createDocument(final String name, final Person owner) {
        return createDocument(name, null, 0L, owner);
    }

    private Document createDocument(final String name, final String description, final long age, final Person owner) {
        return transactional(em -> {
            Document d = new Document(name);
            d.setDescription(description);
            d.setAge(age);
            d.setOwner(owner);
            em.persist(d);
            return d;
        });
    }
}
