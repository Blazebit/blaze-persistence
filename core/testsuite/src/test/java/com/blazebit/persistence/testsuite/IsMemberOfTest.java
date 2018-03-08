/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.testsuite;

import static com.googlecode.catchexception.CatchException.verifyException;
import static org.junit.Assert.assertEquals;

import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import org.junit.Test;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.Document;
import org.junit.experimental.categories.Category;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class IsMemberOfTest extends AbstractCoreTest {

    @Test
    public void testIsMemberOf() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.owner").isMemberOf("d.contacts");

        assertEquals(
            "SELECT d FROM Document d WHERE d.owner MEMBER OF d.contacts",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testIsMemberOfNull() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria.where("d.name"), NullPointerException.class).isMemberOf(null);
    }

    @Test
    public void testIsMemberOfEmpty() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria.where("d.name"), IllegalArgumentException.class).isMemberOf("");
    }

    @Test
    @Category(NoEclipselink.class)
    // Eclipselink does not support dereferencing of VALUE() functions
    public void testIsNotMemberOf() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d").isNotMemberOf("d.contacts.ownedDocuments");

        assertEquals(
            "SELECT d FROM Document d LEFT JOIN d.contacts contacts_1 WHERE d NOT MEMBER OF " + joinAliasValue("contacts_1", "ownedDocuments"),
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testIsNotMemberOfNull() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria.where("d.name"), NullPointerException.class).isNotMemberOf(null);
    }

    @Test
    public void testIsNotMemberOfEmpty() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria.where("d.name"), IllegalArgumentException.class).isNotMemberOf("");
    }
}
