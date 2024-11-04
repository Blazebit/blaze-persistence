/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import org.junit.Test;

import java.util.Arrays;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * @author Christian Beikov
 * @since 1.2.1
 */
public class QuotedIdentifiersTest extends AbstractCoreTest {

    @Override
    protected Properties applyProperties(Properties properties) {
        Properties p = super.applyProperties(properties);
        p.setProperty("hibernate.globally_quoted_identifiers", "true");
        return p;
    }

    // Test for issue #574
    @Test
    public void testQueryValues() {
        long count = cbf.create(em, Long.class)
                .fromValues(Integer.class, "intVal", Arrays.asList(1, 2))
                .select("COUNT(*)")
                .getSingleResult();
        assertEquals(2L, count);
    }

}
