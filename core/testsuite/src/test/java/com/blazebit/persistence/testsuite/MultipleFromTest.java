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
import com.blazebit.persistence.testsuite.entity.Person;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public class MultipleFromTest extends AbstractCoreTest {

    @Test
    public void testMultipleFrom() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class)
                .from(Document.class, "d")
                .from(Person.class, "p");
        criteria.where("d.owner").eqExpression("p");
        criteria.select("COUNT(*)");
        assertEquals("SELECT " + countStar() + " FROM Document d, Person p WHERE d.owner = p", criteria.getQueryString());
        criteria.getResultList();
    }
}
