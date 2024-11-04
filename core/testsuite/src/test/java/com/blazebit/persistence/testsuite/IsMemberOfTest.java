/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

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
        verifyException(criteria.where("d.name"), NullPointerException.class, r -> r.isMemberOf(null));
    }

    @Test
    public void testIsMemberOfEmpty() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria.where("d.name"), IllegalArgumentException.class, r -> r.isMemberOf(""));
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
        verifyException(criteria.where("d.name"), NullPointerException.class, r -> r.isNotMemberOf(null));
    }

    @Test
    public void testIsNotMemberOfEmpty() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria.where("d.name"), IllegalArgumentException.class, r -> r.isNotMemberOf(""));
    }
}
