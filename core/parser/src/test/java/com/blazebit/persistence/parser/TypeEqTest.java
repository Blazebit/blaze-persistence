/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser;

import com.blazebit.persistence.parser.expression.GeneralCaseExpression;
import com.blazebit.persistence.parser.expression.WhenClauseExpression;
import com.blazebit.persistence.parser.predicate.EqPredicate;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class TypeEqTest extends AbstractParserTest {

    @Test
    public void testTypeEqType() {
        GeneralCaseExpression actual = (GeneralCaseExpression) parse("CASE WHEN TYPE(a.b.c) = TYPE(d.e.f) THEN 0 ELSE 1 END");
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new EqPredicate(typeFunction(path("a", "b", "c")), typeFunction(path("d", "e", "f"))), _int("0"))), _int("1"));
        assertEquals(expected, actual);
    }

    @Test
    public void testPathEqType1() {
        entityTypes.put(Entity.class.getName(), Entity.class);
        entityTypes.put(Entity.class.getSimpleName(), Entity.class);
        GeneralCaseExpression actual = (GeneralCaseExpression) parse("CASE WHEN Entity = TYPE(d.e.f) THEN 0 ELSE 1 END");
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new EqPredicate(_entity(Entity.class), typeFunction(path("d", "e", "f"))), _int("0"))), _int("1"));
        assertEquals(expected, actual);
    }

    @Test
    public void testPathEqType2() {
        entityTypes.put(Entity.class.getName(), Entity.class);
        entityTypes.put(Entity.class.getSimpleName(), Entity.class);
        GeneralCaseExpression actual = (GeneralCaseExpression) parse("CASE WHEN " + Entity.class.getName() + " = TYPE(d.e.f) THEN 0 ELSE 1 END");
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new EqPredicate(_entity(Entity.class), typeFunction(path("d", "e", "f"))), _int("0"))), _int("1"));
        assertEquals(expected, actual);
    }

    @Test
    public void testTypeEqPath1() {
        entityTypes.put(Entity.class.getName(), Entity.class);
        entityTypes.put(Entity.class.getSimpleName(), Entity.class);
        GeneralCaseExpression actual = (GeneralCaseExpression) parse("CASE WHEN TYPE(a.b.c) = Entity THEN 0 ELSE 1 END");
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new EqPredicate(typeFunction(path("a", "b", "c")), _entity(Entity.class)), _int("0"))), _int("1"));
        assertEquals(expected, actual);
    }

    @Test
    public void testTypeEqPath2() {
        entityTypes.put(Entity.class.getName(), Entity.class);
        entityTypes.put(Entity.class.getSimpleName(), Entity.class);
        GeneralCaseExpression actual = (GeneralCaseExpression) parse("CASE WHEN TYPE(a.b.c) = " + Entity.class.getName() + " THEN 0 ELSE 1 END");
        GeneralCaseExpression expected = new GeneralCaseExpression(Arrays.asList(new WhenClauseExpression(new EqPredicate(typeFunction(path("a", "b", "c")), _entity(Entity.class)), _int("0"))), _int("1"));
        assertEquals(expected, actual);
    }

    static class Entity {

    }
}
