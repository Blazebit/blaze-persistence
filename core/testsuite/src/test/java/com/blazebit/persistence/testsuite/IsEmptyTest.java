/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.entity.Document;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class IsEmptyTest extends AbstractCoreTest {

    @Test
    public void testIsEmpty() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.partners").isEmpty();

        assertEquals("SELECT d FROM Document d WHERE d.partners IS EMPTY", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testIsNotEmpty() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.partners").isNotEmpty();

        assertEquals("SELECT d FROM Document d WHERE d.partners IS NOT EMPTY", criteria.getQueryString());
        criteria.getResultList();
    }
}
