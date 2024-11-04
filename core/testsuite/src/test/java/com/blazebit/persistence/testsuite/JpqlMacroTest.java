/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.parser.expression.SyntaxErrorException;
import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlMacro;
import com.blazebit.persistence.testsuite.entity.Document;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class JpqlMacroTest extends AbstractCoreTest {

    @Test
    public void testPrefixMacro() {
        CriteriaBuilder<Document> cb = cbf.create(em, Document.class, "d");
        cb.where("PREFIX(d, id)").eq(1L);
        String expected = "SELECT d FROM Document d WHERE d.id = :param_0";
        
        assertEquals(expected, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testScopedMacro() {
        CriteriaBuilder<Document> cb = cbf.create(em, Document.class, "d");
        verifyException(cb, SyntaxErrorException.class, r -> r.where("ID(d)"));

        cb.registerMacro("id", new JpqlMacro() {
            @Override
            public void render(FunctionRenderContext context) {
                context.addArgument(0);
                context.addChunk(".id");
            }
        });
        cb.where("ID(d)").eq(1);
        String expected = "SELECT d FROM Document d WHERE d.id = :param_0";
        assertEquals(expected, cb.getQueryString());

        CriteriaBuilder<Document> newCb = cbf.create(em, Document.class, "d");
        verifyException(newCb, SyntaxErrorException.class, r -> r.where("ID(d)"));
    }
}
