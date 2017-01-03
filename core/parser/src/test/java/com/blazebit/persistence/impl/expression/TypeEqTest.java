/*
 * Copyright 2014 - 2017 Blazebit.
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

package com.blazebit.persistence.impl.expression;

import com.blazebit.persistence.impl.predicate.EqPredicate;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Moritz Becker
 * @since 1.2
 */
public class TypeEqTest extends AbstractParserTest {

    @Test
    public void testTypeEqType() {
        GeneralCaseExpression actual = (GeneralCaseExpression) parse("CASE WHEN TYPE(a.b.c) = TYPE(d.e.f) THEN 0 ELSE 1 END");
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new EqPredicate(typeFunction(path("a", "b", "c")), typeFunction(path("d", "e", "f"))), _int("0"))), _int("1"));
        assertEquals(expected, actual);
    }

    @Test
    public void testPathEqType() {
        GeneralCaseExpression actual = (GeneralCaseExpression) parse("CASE WHEN a.b.c = TYPE(d.e.f) THEN 0 ELSE 1 END");
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new EqPredicate(_entity("a.b.c"), typeFunction(path("d", "e", "f"))), _int("0"))), _int("1"));
        assertEquals(expected, actual);
    }

    @Test
    public void testTypeEqPath() {
        GeneralCaseExpression actual = (GeneralCaseExpression) parse("CASE WHEN TYPE(a.b.c) = d.e.f THEN 0 ELSE 1 END");
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new EqPredicate(typeFunction(path("a", "b", "c")), _entity("d.e.f")), _int("0"))), _int("1"));
        assertEquals(expected, actual);
    }
}
