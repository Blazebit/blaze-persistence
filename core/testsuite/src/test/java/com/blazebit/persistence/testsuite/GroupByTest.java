/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDB2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoH2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate62;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMSSQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.base.jpa.category.NoPostgreSQL;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import jakarta.persistence.Tuple;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public class GroupByTest extends AbstractCoreTest {

    @Test
    public void testGroupByEntitySelect() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d");
        criteria.groupBy("d.owner");
        String expectedQuery = "SELECT d FROM Document d JOIN d.owner owner_1 ";
        if (jpaProvider.supportsGroupByEntityAlias()) {
            expectedQuery += "GROUP BY owner_1, d";
        } else {
            expectedQuery += "GROUP BY owner_1, d.age, d.archived, d.byteArray, d.creationDate, d.creationDate2, d.defaultContact, d.documentType, d.id, d.idx, d.intIdEntity.id, d.lastModified, d.lastModified2, d.name, d.nameContainer.name, d.nameContainer.nameObject.intIdEntity.id, d.nameContainer.nameObject.primaryName, d.nameContainer.nameObject.secondaryName, d.nameObject.intIdEntity.id, d.nameObject.primaryName, d.nameObject.secondaryName, d.nonJoinable, owner_1.id, d.owner.id, d.parent.id, d.responsiblePerson.id, d.someValue, d.version, d.wrappedByteArray";
        }

        assertEquals(expectedQuery, criteria.getQueryString());
        criteria.getResultList();
    }
    
    @Test
    public void testSizeTransformWithImplicitParameterGroupBy1() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d")
                .select("SIZE(d.versions)")
                .selectCase().when("d.age").lt(2L).thenExpression("'a'").otherwiseExpression("'b'");

        final String expected = "SELECT " + count("versions_1.id") + ", CASE WHEN d.age < 2L THEN 'a' ELSE 'b' END FROM Document d LEFT JOIN d.versions versions_1 GROUP BY d.id, CASE WHEN d.age < 2L THEN 'a' ELSE 'b' END";
        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList();
    }

    // This is a MySQL only test
    @Test
    @Category({ NoPostgreSQL.class, NoDB2.class, NoOracle.class, NoH2.class, NoMSSQL.class, NoEclipselink.class})
    public void testImplicitGroupByFunctionExpression() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class).from(Document.class, "d")
                .select("CAST_DATE(FUNCTION('date', '2000-01-01'))")
                .select("COUNT(*)");

        final String expected = "SELECT " + function("cast_date", function("date", "'2000-01-01'")) + ", " + countStar() + " FROM Document d GROUP BY " + function("cast_date", function("date", "'2000-01-01'"));
        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testGroupByKeyExpression() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class);
        criteria.from(Document.class, "d")
                .select("KEY(contacts)")
                // Assert that a KEY expression is valid in group by even if the DB does not support "complex group bys"
                .groupBy("KEY(contacts)");
        assertEquals("SELECT KEY(contacts_1) FROM Document d LEFT JOIN d.contacts contacts_1 GROUP BY KEY(contacts_1)", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testOmitGroupByLiteral() {
        CriteriaBuilder<Long> cb = cbf.create(em, Long.class)
                .from(Document.class, "d")
                .select("d.id")
                .select("SIZE(d.people)")
                .select("''");

        assertEquals("SELECT d.id, " + count("INDEX(people_1)") + ", '' FROM Document d LEFT JOIN d.people people_1 GROUP BY d.id", cb.getQueryString());
        cb.getResultList();
    }

    // NOTE: Hibernate ORM doesn't detect that it has to use the join alias column
    @Test
    @Category({ NoHibernate.class })
    public void testGroupBySubqueryCorrelatedExpression() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class)
                .from(Document.class, "d")
                .innerJoinDefault("d.owner", "o")
                .selectSubquery("subquery", "CASE WHEN EXISTS subquery THEN 1 ELSE 0 END")
                    .from(Person.class, "p")
                    .select("p.id")
                    .where("p.id").eqExpression("o.id")
                .end()
                .select("COUNT(*)");

        assertEquals("SELECT CASE WHEN EXISTS (SELECT p.id FROM Person p WHERE p.id = o.id) THEN 1 ELSE 0 END, " + countStar() + " FROM Document d JOIN d.owner o GROUP BY o.id", cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testGroupByEmbeddable() {
        CriteriaBuilder<Long> cb = cbf.create(em, Long.class)
                .from(Document.class, "d")
                .select("d.id")
                .groupBy("names");

        if (jpaProvider.supportsSingleValuedAssociationIdExpressions()) {
            if (jpaProvider.needsElementCollectionIdCutoff()) {
                assertEquals("SELECT d.id FROM Document d LEFT JOIN d.names names_1 GROUP BY names_1.intIdEntity, names_1.primaryName, names_1.secondaryName, d.id", cb.getQueryString());
            } else {
                assertEquals("SELECT d.id FROM Document d LEFT JOIN d.names names_1 GROUP BY names_1.intIdEntity.id, names_1.primaryName, names_1.secondaryName, d.id", cb.getQueryString());
            }
        } else {
            assertEquals("SELECT d.id FROM Document d LEFT JOIN d.names names_1 LEFT JOIN names_1.intIdEntity intIdEntity_1 GROUP BY intIdEntity_1.id, names_1.primaryName, names_1.secondaryName, d.id", cb.getQueryString());
        }
        cb.getResultList();
    }

    @Test
    public void testGroupByElementCollectionKey() {
        CriteriaBuilder<Long> cb = cbf.create(em, Long.class)
                .from(Document.class, "d")
                .select("d.id")
                .groupBy("KEY(nameMap)");

        assertEquals("SELECT d.id FROM Document d LEFT JOIN d.nameMap nameMap_1 GROUP BY KEY(nameMap_1), d.id", cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testGroupByElementCollectionValue() {
        CriteriaBuilder<Long> cb = cbf.create(em, Long.class)
                .from(Document.class, "d")
                .select("d.id")
                .groupBy("VALUE(nameMap)");

        if (jpaProvider.supportsSingleValuedAssociationIdExpressions()) {
            if (jpaProvider.needsElementCollectionIdCutoff()) {
                assertEquals("SELECT d.id FROM Document d LEFT JOIN d.nameMap nameMap_1 GROUP BY nameMap_1.intIdEntity, nameMap_1.primaryName, nameMap_1.secondaryName, d.id", cb.getQueryString());
            } else {
                assertEquals("SELECT d.id FROM Document d LEFT JOIN d.nameMap nameMap_1 GROUP BY nameMap_1.intIdEntity.id, nameMap_1.primaryName, nameMap_1.secondaryName, d.id", cb.getQueryString());
            }
        } else {
            assertEquals("SELECT d.id FROM Document d LEFT JOIN d.nameMap nameMap_1 LEFT JOIN nameMap_1.intIdEntity intIdEntity_1 GROUP BY intIdEntity_1.id, " + joinAliasValue("nameMap_1", "primaryName") + ", " + joinAliasValue("nameMap_1", "secondaryName") + ", d.id", cb.getQueryString());
        }
        cb.getResultList();
    }
}
