/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.PrimitiveDocument;
import com.blazebit.persistence.testsuite.entity.PrimitivePerson;
import com.blazebit.persistence.testsuite.entity.PrimitiveVersion;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class ImplicitNamingTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class[] {
                PrimitiveDocument.class,
                PrimitivePerson.class,
                PrimitiveVersion.class
        };
    }

    /**
     * Key to the test is to use an entity the name of which contains > 1 camel case part.
     *
     * This test is for issue #372
     */
    @Test
    public void camelCaseImplicitNameDoesNotCauseNamingConflict() {
        CriteriaBuilder<PrimitiveDocument> crit = cbf.create(em, PrimitiveDocument.class)
                .whereNotExists().from(PrimitiveDocument.class)
                    .select("1")
                    .where("name").eqExpression("OUTER(name)")
                .end();

        String expected = "SELECT primitiveDocument FROM PrimitiveDocument primitiveDocument WHERE NOT EXISTS (SELECT 1 FROM PrimitiveDocument primitiveDocument_1 WHERE primitiveDocument_1.name = primitiveDocument.name)";
        String actual = crit.getQueryString();

        assertEquals(expected, actual);
        assertEquals(actual, crit.getQueryString());
        crit.getResultList();
    }
}
