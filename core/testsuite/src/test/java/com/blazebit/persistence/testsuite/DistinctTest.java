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
public class DistinctTest extends AbstractCoreTest {

    @Test
    public void testDistinct() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.select("d.partners.name").distinct();

        assertEquals("SELECT DISTINCT partners_1.name FROM Document d LEFT JOIN d.partners partners_1", criteria.getQueryString());
    }

    @Test
    public void testDistinctWithoutSelect() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.distinct();
        assertEquals("SELECT DISTINCT d FROM Document d", criteria.getQueryString());
    }

}
