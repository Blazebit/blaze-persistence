/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.parser;

import com.blazebit.persistence.parser.expression.Expression;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class PrefixingAndAliasReplacementQueryGeneratorTest extends AbstractParserTest {

    @Before
    @Override
    public void initTest() {
        super.initTest();
        entityTypes.put(TestEntity.class.getSimpleName(), TestEntity.class);
    }

    private static class TestEntity { }

    @Test
    public void testSoftKeywordsMultipleKeywordsAsSimpleUpperPath() {
        Expression result = parsePredicate("customerId = TREAT(generatedAbstractTicketHistory_0 AS TestEntity).activity.ticket.underpinningContract.project.customer.id", false);
        PrefixingAndAliasReplacementQueryGenerator generator = new PrefixingAndAliasReplacementQueryGenerator("a", "x", "NONE", "generatedAbstractTicketHistory_0", true);
        generator.setQueryBuffer(new StringBuilder());
        result.accept(generator);
        assertEquals("a.customerId = TREAT(generatedAbstractTicketHistory_0 AS TestEntity).activity.ticket.underpinningContract.project.customer.id", generator.getQueryBuffer().toString());
    }
}
