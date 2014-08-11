/*
 * Copyright 2014 Blazebit.
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
package com.blazebit.persistence;

import com.blazebit.persistence.entity.Document;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class IsMemberOfTest extends AbstractCoreTest {

    @Test
    public void testIsMemberOf() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.owner").isMemberOf("d.contacts");

        assertEquals("SELECT d FROM Document d LEFT JOIN d.contacts contacts JOIN d.owner owner WHERE owner MEMBER OF d.contacts", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test(expected = NullPointerException.class)
    public void testIsMemberOfNull() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.name").isMemberOf(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsMemberOfEmpty() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.name").isMemberOf("");
    }

    @Test
    public void testIsNotMemberOf() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d").isNotMemberOf("d.contacts.ownedDocuments");

        assertEquals("SELECT d FROM Document d LEFT JOIN d.contacts contacts LEFT JOIN contacts.ownedDocuments ownedDocuments WHERE NOT d MEMBER OF contacts.ownedDocuments", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test(expected = NullPointerException.class)
    public void testIsNotMemberOfNull() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.name").isNotMemberOf(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsNotMemberOfEmpty() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.where("d.name").isNotMemberOf("");
    }
}
