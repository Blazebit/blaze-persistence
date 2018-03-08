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
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.parser.expression.SyntaxErrorException;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus4;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate;
import com.blazebit.persistence.testsuite.entity.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static com.googlecode.catchexception.CatchException.verifyException;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class TreatTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
                IntIdEntity.class,
                PolymorphicBase.class,
                PolymorphicSub1.class,
                PolymorphicSub2.class
        };
    }

    @Test
    public void treatAsExpressionRootNotAllowed() {
        CriteriaBuilder<Integer> criteria = cbf.create(em, Integer.class);
        criteria.from(PolymorphicBase.class, "p");
        verifyException(criteria, SyntaxErrorException.class).select("TREAT(p AS PolymorphicSub1)");
    }

    @Test
    public void treatOfTreatAsExpressionRootNotAllowed() {
        CriteriaBuilder<Integer> criteria = cbf.create(em, Integer.class);
        criteria.from(PolymorphicBase.class, "p");
        verifyException(criteria, SyntaxErrorException.class).select("TREAT(TREAT(p AS PolymorphicSub1).parent1 AS PolymorphicSub1)");
    }

    @Test
    // NOTE: Datanucleus does not support root treats properly with joined inheritance. Maybe a bug? TODO: report the error
    @Category({ NoDatanucleus.class })
    public void implicitJoinTreatedRoot() {
        CriteriaBuilder<Integer> criteria = cbf.create(em, Integer.class);
        criteria.from(PolymorphicBase.class, "p");
        criteria.select("TREAT(p AS PolymorphicSub1).sub1Value");
        assertEquals("SELECT " + treatRoot("p", PolymorphicSub1.class, "sub1Value") + " FROM PolymorphicBase p", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // NOTE: Datanucleus does not support root treats properly with joined inheritance. Maybe a bug? TODO: report the error
    @Category({ NoDatanucleus.class })
    public void treatInAggregateHaving() {
        CriteriaBuilder<Integer> criteria = cbf.create(em, Integer.class);
        criteria.from(PolymorphicBase.class, "p");
        criteria.select("SUM(TREAT(p AS PolymorphicSub1).sub1Value)");
        criteria.groupBy("p.name");
        criteria.having("SUM(TREAT(p AS PolymorphicSub1).sub1Value)").gt(1L);
        assertEquals("SELECT SUM(" + treatRoot("p", PolymorphicSub1.class, "sub1Value") + ")" +
                " FROM PolymorphicBase p" +
                " GROUP BY p.name" +
                " HAVING SUM(" + treatRoot("p", PolymorphicSub1.class, "sub1Value") + ") > :param_0", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // NOTE: Datanucleus does not support root treats properly with joined inheritance. Maybe a bug? TODO: report the error
    @Category({ NoDatanucleus.class })
    public void treatedRootInCaseWhenCondition() {
        CriteriaBuilder<Integer> criteria = cbf.create(em, Integer.class);
        criteria.from(PolymorphicBase.class, "p");
        criteria.select("CASE WHEN TREAT(p AS PolymorphicSub1).sub1Value > 0 THEN 1 ELSE 0 END");
        assertEquals(
                "SELECT CASE WHEN (TYPE(p) = " + PolymorphicSub1.class.getSimpleName() + " AND " + treatRoot("p", PolymorphicSub1.class, "sub1Value") + " > 0) THEN 1 ELSE 0 END" +
                        " FROM PolymorphicBase p", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // NOTE: Datanucleus does not support root treats properly with joined inheritance. Maybe a bug? TODO: report the error
    @Category({ NoDatanucleus.class })
    public void treatedRootInCaseWhenResult() {
        CriteriaBuilder<Integer> criteria = cbf.create(em, Integer.class);
        criteria.from(PolymorphicBase.class, "p");
        criteria.select("CASE WHEN 1 > 0 THEN TREAT(p AS PolymorphicSub1).sub1Value ELSE 0 END");
        assertEquals(
                "SELECT CASE WHEN 1 > 0 THEN " + treatRoot("p", PolymorphicSub1.class, "sub1Value") + " ELSE 0 END" +
                        " FROM PolymorphicBase p", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // NOTE: Datanucleus4 reports: We do not currently support JOIN to TREAT
    // NOTE: Datanucleus does not support root treats properly with joined inheritance. Maybe a bug? TODO: report the error
    // NOTE: Eclipselink does not support root treat joins i.e. "JOIN TREAT(..).relation"
    @Category({ NoDatanucleus.class , NoDatanucleus4.class, NoEclipselink.class })
    public void fetchJoinTreatedRoot() {
        CriteriaBuilder<Integer> criteria = cbf.create(em, Integer.class);
        criteria.from(PolymorphicBase.class, "p");
        criteria.fetch("TREAT(p AS PolymorphicSub1).relation1");
        assertEquals("SELECT p FROM PolymorphicBase p LEFT JOIN FETCH " + treatRootJoin("p", PolymorphicSub1.class, "relation1") + " relation1_1", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // NOTE: Datanucleus4 reports: We do not currently support JOIN to TREAT
    // NOTE: Datanucleus does not support root treats properly with joined inheritance. Maybe a bug? TODO: report the error
    // NOTE: Eclipselink does not support root treat joins i.e. "JOIN TREAT(..).relation"
    @Category({ NoDatanucleus.class , NoDatanucleus4.class, NoEclipselink.class })
    public void fetchJoinTreatedNode() {
        CriteriaBuilder<Integer> criteria = cbf.create(em, Integer.class);
        criteria.from(PolymorphicBase.class, "p");
        criteria.select("parent");
        criteria.fetch("TREAT(parent AS PolymorphicSub1).relation1");
        assertEquals("SELECT parent_1 FROM PolymorphicBase p LEFT JOIN p.parent parent_1 LEFT JOIN FETCH " + treatRootJoin("parent_1", PolymorphicSub1.class, "relation1") + " relation1_1", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // NOTE: Datanucleus does not support root treats properly with joined inheritance. Maybe a bug? TODO: report the error
    @Category({ NoDatanucleus.class })
    public void implicitJoinTreatedRelation() {
        CriteriaBuilder<Integer> criteria = cbf.create(em, Integer.class);
        criteria.from(PolymorphicBase.class, "p");
        criteria.select("TREAT(p.parent AS PolymorphicSub1).sub1Value");
        assertEquals("SELECT " + treatRoot("parent_1", PolymorphicSub1.class, "sub1Value") + " FROM PolymorphicBase p LEFT JOIN p.parent parent_1", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // NOTE: Datanucleus does not support root treats properly with joined inheritance. Maybe a bug? TODO: report the error
    // NOTE: Eclipselink does not support root treat joins i.e. "JOIN TREAT(..).relation"
    @Category({ NoDatanucleus.class, NoEclipselink.class })
    public void singleValuedAssociationIdOfTreatedImplicitJoinedRelation() {
        CriteriaBuilder<Integer> criteria = cbf.create(em, Integer.class);
        criteria.from(PolymorphicBase.class, "relation1");
        criteria.select("TREAT(parent AS PolymorphicSub1).relation1.id");
        assertEquals("SELECT " + treatRoot("parent_1", PolymorphicSub1.class, "relation1.id") + " FROM PolymorphicBase relation1 LEFT JOIN relation1.parent parent_1", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // NOTE: Datanucleus4 reports: We do not currently support JOIN to TREAT
    // NOTE: Datanucleus does not support root treats properly with joined inheritance. Maybe a bug? TODO: report the error
    // NOTE: Eclipselink does not support root treat joins i.e. "JOIN TREAT(..).relation"
    @Category({ NoDatanucleus.class , NoDatanucleus4.class, NoEclipselink.class })
    public void implicitJoinTreatedRootTreatJoin() {
        CriteriaBuilder<Integer> criteria = cbf.create(em, Integer.class);
        criteria.from(PolymorphicBase.class, "p");
        criteria.select("TREAT(TREAT(p AS PolymorphicSub1).parent1 AS PolymorphicSub1).sub1Value");
        assertEquals("SELECT " + treatRoot("parent1_1", PolymorphicSub1.class, "sub1Value") + " FROM PolymorphicBase p LEFT JOIN " + treatRootJoin("p", PolymorphicSub1.class, "parent1") + " parent1_1", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // NOTE: Datanucleus4 reports: We do not currently support JOIN to TREAT
    // NOTE: Datanucleus does not support root treats properly with joined inheritance. Maybe a bug? TODO: report the error
    @Category({ NoDatanucleus.class, NoDatanucleus4.class })
    public void joinTreatedRelation() {
        CriteriaBuilder<Integer> criteria = cbf.create(em, Integer.class);
        criteria.from(PolymorphicBase.class, "p");
        criteria.select("polymorphicSub1.sub1Value");
        criteria.innerJoin("TREAT(p.parent AS PolymorphicSub1)", "polymorphicSub1");
        String treatJoinWhereFragment = treatJoinWhereFragment(PolymorphicBase.class, "parent", "polymorphicSub1", PolymorphicSub1.class, JoinType.INNER, null);
        assertEquals("SELECT polymorphicSub1.sub1Value FROM PolymorphicBase p JOIN " + treatJoin("p.parent", PolymorphicSub1.class) + " polymorphicSub1" + treatJoinWhereFragment, criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // NOTE: With datanucleus this only fails with INNER JOIN but works with left join. Maybe a bug? TODO: report the error
    // Eclipselink and Datanucleus do not support root treat joins
    @Category({ NoDatanucleus.class, NoEclipselink.class})
    public void joinTreatedRoot() {
        CriteriaBuilder<Integer> criteria = cbf.create(em, Integer.class);
        criteria.from(PolymorphicBase.class, "p");
        criteria.select("parent1.name");
        criteria.innerJoin("TREAT(p AS PolymorphicSub1).parent1", "parent1");
        assertEquals("SELECT parent1.name FROM PolymorphicBase p JOIN " + treatRootJoin("p", PolymorphicSub1.class, "parent1") + " parent1", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // NOTE: With datanucleus this only fails with INNER JOIN but works with left join. Maybe a bug? TODO: report the error
    // Eclipselink does not support dereferencing of TREAT join path elements
    @Category({ NoDatanucleus.class, NoEclipselink.class })
    public void joinTreatedRootEmbeddable() {
        CriteriaBuilder<Integer> criteria = cbf.create(em, Integer.class);
        criteria.from(PolymorphicBase.class, "p");
        criteria.select("intIdEntity.name");
        criteria.innerJoin("TREAT(p AS PolymorphicSub1).embeddable1.intIdEntity", "intIdEntity");
        assertEquals("SELECT intIdEntity.name FROM PolymorphicBase p JOIN " + treatRootJoin("p", PolymorphicSub1.class, "embeddable1.intIdEntity") + " intIdEntity", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // NOTE: With datanucleus this only fails with INNER JOIN but works with left join. Maybe a bug? TODO: report the error
    // Eclipselink does not support dereferencing of TREAT join path elements
    @Category({ NoDatanucleus.class, NoEclipselink.class })
    public void selectTreatedRootEmbeddable() {
        CriteriaBuilder<Integer> criteria = cbf.create(em, Integer.class);
        criteria.from(PolymorphicBase.class, "p");
        criteria.select("TREAT(p AS PolymorphicSub2).embeddable2.intIdEntity.name");
        assertEquals("SELECT intIdEntity_1.name FROM PolymorphicBase p LEFT JOIN " + treatRootJoin("p", PolymorphicSub2.class, "embeddable2.intIdEntity") + " intIdEntity_1", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // NOTE: With datanucleus this only fails with INNER JOIN but works with left join. Maybe a bug? TODO: report the error
    @Category({ NoDatanucleus.class, NoHibernate.class })
    public void treatJoinTreatedRootRelation() {
        CriteriaBuilder<Integer> criteria = cbf.create(em, Integer.class);
        criteria.from(PolymorphicBase.class, "p");
        criteria.select("polymorphicSub1.sub1Value");
        criteria.innerJoin("TREAT(TREAT(p AS PolymorphicSub1).parent1 AS PolymorphicSub1)", "polymorphicSub1");
        String treatJoinWhereFragment = treatJoinWhereFragment(PolymorphicSub1.class, "parent1", "polymorphicSub1", PolymorphicSub1.class, JoinType.INNER, null);
        assertEquals("SELECT polymorphicSub1.sub1Value FROM PolymorphicBase p "
                + treatRootTreatJoin(JoinType.INNER, "p", PolymorphicSub1.class, "parent1", PolymorphicSub1.class, "polymorphicSub1") + treatJoinWhereFragment, criteria.getQueryString());
        criteria.getResultList();
    }
}
