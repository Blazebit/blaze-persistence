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

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.parser.expression.SyntaxErrorException;
import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlMacro;
import com.blazebit.persistence.testsuite.entity.Document;
import com.googlecode.catchexception.CatchException;
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
        CatchException.verifyException(cb, SyntaxErrorException.class).where("ID(d)");

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
        CatchException.verifyException(newCb, SyntaxErrorException.class).where("ID(d)");
    }
}
