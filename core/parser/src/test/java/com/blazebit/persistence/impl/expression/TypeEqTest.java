package com.blazebit.persistence.impl.expression;

import com.blazebit.persistence.impl.predicate.EqPredicate;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Created
 * by Moritz Becker (moritz.becker@gmx.at)
 * on 04.08.2016.
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
