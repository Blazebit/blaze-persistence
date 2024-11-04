/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.Document;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class EntityNullnessGeneralTest extends AbstractCoreTest {

    @Test
    public void neverRewriteEntityAssociationNullnessInWhere() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.owner").isNull();

        assertEquals("SELECT d FROM Document d WHERE d.owner IS NULL", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void alwaysRewriteEntityAssociationIdNullnessInWhere() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.owner.id").isNull();

        assertEquals("SELECT d FROM Document d WHERE " + singleValuedAssociationIdNullnessPath("d.owner", "id") + " IS NULL", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void alwaysRewriteEntityIdNullnessInWhere() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.id").isNull();

        assertEquals("SELECT d FROM Document d WHERE d IS NULL", criteria.getQueryString());
        criteria.getResultList();
    }

}
