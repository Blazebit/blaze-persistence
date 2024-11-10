/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.parser.expression.ArrayExpression;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import jakarta.persistence.Tuple;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class ArrayExpressionTest extends AbstractCoreTest {

    @Override
    protected void setUpOnce() {
        // TODO: Remove me when DataNucleus fixes map value access: https://github.com/datanucleus/datanucleus-rdbms/issues/230
        cleanDatabase();
    }

    @Test
    public void testSelectPathIndex() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.select("d.contacts[d.idx]");

        assertEquals("SELECT " + joinAliasValue("contacts_d_idx_1") + " FROM Document d" +
                " LEFT JOIN d.contacts contacts_d_idx_1"
                + onClause("KEY(contacts_d_idx_1) = d.idx"), criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testSelectParameterIndex() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.select("d.contacts[:age]");

        assertEquals("SELECT " + joinAliasValue("contacts_age_1") + " FROM Document d" +
                " LEFT JOIN d.contacts contacts_age_1"
                + onClause("KEY(contacts_age_1) = :age"), criteria.getQueryString());
        criteria.setParameter("age", 1).getResultList();
    }

    @Test
    public void testSelectMultipleArrayPath() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.select("d.contacts[:age].localized[d.idx]");

        assertEquals("SELECT " + joinAliasValue("localized_d_idx_1") + " FROM Document d" +
                " LEFT JOIN d.contacts contacts_age_1"
                + onClause("KEY(contacts_age_1) = :age") +
                " LEFT JOIN contacts_age_1.localized localized_d_idx_1" +
                onClause("KEY(localized_d_idx_1) = d.idx"), criteria.getQueryString());
        criteria.setParameter("age", 1).getResultList();
    }

    @Test
    public void testSelectAlternatingArrayPath() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.select("d.contacts[:age].partnerDocument.contacts[d.idx]");

        assertEquals("SELECT " + joinAliasValue("contacts_d_idx_1") + " FROM Document d" +
                " LEFT JOIN d.contacts contacts_age_1"
                + onClause("KEY(contacts_age_1) = :age") +
                " LEFT JOIN contacts_age_1.partnerDocument partnerDocument_1 LEFT JOIN partnerDocument_1.contacts contacts_d_idx_1"
                + onClause("KEY(contacts_d_idx_1) = d.idx"), criteria.getQueryString());
        criteria.setParameter("age", 1).getResultList();
    }

    @Test
    public void testArrayIndexImplicitJoin() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.select("d.contacts[d.versions.versionIdx]");

        assertEquals("SELECT " + joinAliasValue("contacts_d_versions_versionIdx_1") + " FROM Document d" +
                " LEFT JOIN d.versions versions_1" +
                " LEFT JOIN d.contacts contacts_d_versions_versionIdx_1"
                + onClause("KEY(contacts_d_versions_versionIdx_1) = versions_1.versionIdx"), criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testArrayIndexImplicitJoinImplicitRoot() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.select("d.contacts[intIdEntity.id]");

        final String contactsJoinAlias;
        if (jpaProvider.supportsSingleValuedAssociationIdExpressions()) {
            contactsJoinAlias = "contacts_intIdEntity_id_1";
        } else {
            contactsJoinAlias = "contacts_intIdEntity_id_1";
        }

        assertEquals("SELECT " + joinAliasValue(contactsJoinAlias) + " FROM Document d"
                + singleValuedAssociationIdJoin("d.intIdEntity", "intIdEntity_1", true) +
                " LEFT JOIN d.contacts " + contactsJoinAlias
                + onClause("KEY(" + contactsJoinAlias + ") = " + singleValuedAssociationIdPath("d.intIdEntity.id", "intIdEntity_1")), criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testArrayIndexExplicitJoinAlias() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.select("d.contacts[v.versionIdx]").leftJoinDefault("d.versions", "v");

        assertEquals("SELECT " + joinAliasValue("contacts_v_versionIdx_1") + " FROM Document d" +
                " LEFT JOIN d.versions v" +
                " LEFT JOIN d.contacts contacts_v_versionIdx_1"
                + onClause("KEY(contacts_v_versionIdx_1) = v.versionIdx"), criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    @Category(NoEclipselink.class)
    // TODO: report eclipse bug, the expression "VALUE(c) IS NULL" seems illegal but JPA spec 4.6.11 allows it
    public void testRedundantArrayTransformation() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.select("contacts[1]").where("contacts[1]").isNull();

        assertEquals("SELECT " + joinAliasValue("contacts_1_1") + " FROM Document d" +
                " LEFT JOIN d.contacts contacts_1_1"
                + onClause("KEY(contacts_1_1) = 1")
                + " WHERE " + joinAliasValue("contacts_1_1") + " IS NULL", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // Map dereferencing is actually not allowed in JPQL
    public void testMapDereferencing() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.select("owner.partnerDocument", "x").leftJoinDefault("owner.partnerDocument", "p").where("p.contacts[1].name").isNull();

        assertEquals(
                "SELECT p AS x FROM Document d" +
                        " JOIN d.owner owner_1" +
                        " LEFT JOIN owner_1.partnerDocument p" +
                        " LEFT JOIN p.contacts contacts_1_1"
                        + onClause("KEY(contacts_1_1) = 1")
                        + " WHERE " + joinAliasValue("contacts_1_1", "name") + " IS NULL", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    @Category(NoEclipselink.class)
    // TODO: report eclipse bug, the expression "VALUE(c) IS NULL" seems illegal but JPA spec 4.6.11 allows it
    public void testMore() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.select("owner.partnerDocument", "x").leftJoinDefault("owner.partnerDocument", "p").leftJoinDefault("p.contacts", "c").where(
            "c[1]").isNull();

        assertEquals(
                "SELECT p AS x FROM Document d" +
                        " JOIN d.owner owner_1" +
                        " LEFT JOIN owner_1.partnerDocument p" +
                        " LEFT JOIN p.contacts c"
                        + onClause("KEY(c) = 1")
                        + " WHERE " + joinAliasValue("c") + " IS NULL", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testMore2() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.select("d.contacts[1].partnerDocument.name", "x");

        assertEquals("SELECT partnerDocument_1.name AS x FROM Document d" +
                " LEFT JOIN d.contacts contacts_1_1"
                + onClause("KEY(contacts_1_1) = 1")
                + " LEFT JOIN contacts_1_1.partnerDocument partnerDocument_1", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testMapSelectWithAlias() {
        CriteriaBuilder<Person> criteria = cbf.create(em, Person.class).from(Document.class, "d");
        criteria.select("d.contacts[1]", "x");

        assertEquals("SELECT " + joinAliasValue("contacts_1_1") + " AS x FROM Document d" +
                " LEFT JOIN d.contacts contacts_1_1"
                + onClause("KEY(contacts_1_1) = 1"), criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testPredicateIndex() {
        CriteriaBuilder<Person> criteria = cbf.create(em, Person.class).from(Document.class, "d");
        criteria.select("d.contacts[KEY(_) = d.idx]");

        assertEquals("SELECT " + joinAliasValue("contacts_KEY_" + ArrayExpression.ELEMENT_NAME + "____d_idx_1") + " FROM Document d" +
                " LEFT JOIN d.contacts contacts_KEY_" + ArrayExpression.ELEMENT_NAME + "____d_idx_1"
                + onClause("KEY(contacts_KEY_" + ArrayExpression.ELEMENT_NAME +"____d_idx_1) = d.idx"), criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testPredicateIndexReuse() {
        CriteriaBuilder<Person> criteria = cbf.create(em, Person.class).from(Document.class, "d");
        criteria.select("d.contacts[KEY(_) = d.idx]")
                .where("d.contacts[KEY(_) = d.idx].name").eq("pers1");

        assertEquals("SELECT " + joinAliasValue("contacts_KEY_" + ArrayExpression.ELEMENT_NAME + "____d_idx_1") + " FROM Document d" +
                " LEFT JOIN d.contacts contacts_KEY_" + ArrayExpression.ELEMENT_NAME + "____d_idx_1"
                + onClause("KEY(contacts_KEY_" + ArrayExpression.ELEMENT_NAME + "____d_idx_1) = d.idx")
                + " WHERE " + joinAliasValue("contacts_KEY_" + ArrayExpression.ELEMENT_NAME + "____d_idx_1", "name") + " = :param_0", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testPredicateIndexReuseBase() {
        CriteriaBuilder<String> criteria = cbf.create(em, String.class).from(Document.class, "d");
        criteria.select("d.contacts[KEY(_) = d.idx].name")
                .where("d.contacts[KEY(_) = d.idx].name").eq("pers1");

        assertEquals("SELECT " + joinAliasValue("contacts_KEY_" + ArrayExpression.ELEMENT_NAME + "____d_idx_1", "name") + " FROM Document d" +
                " LEFT JOIN d.contacts contacts_KEY_" + ArrayExpression.ELEMENT_NAME + "____d_idx_1"
                + onClause("KEY(contacts_KEY_" + ArrayExpression.ELEMENT_NAME + "____d_idx_1) = d.idx")
                + " WHERE " + joinAliasValue("contacts_KEY_" + ArrayExpression.ELEMENT_NAME + "____d_idx_1", "name") + " = :param_0", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testNewImplicitRoot() {
        CriteriaBuilder<Person> criteria = cbf.create(em, Person.class).from(Document.class, "d");
        criteria.select("d.contacts[friend IS NULL]");

        assertEquals("SELECT " + joinAliasValue("contacts_" + ArrayExpression.ELEMENT_NAME + "_friend_IS_NULL_1") + " FROM Document d" +
                " LEFT JOIN d.contacts contacts_" + ArrayExpression.ELEMENT_NAME + "_friend_IS_NULL_1"
                + onClause(joinAliasValue("contacts_" + ArrayExpression.ELEMENT_NAME + "_friend_IS_NULL_1", "friend") + " IS NULL"), criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testEntityArray() {
        CriteriaBuilder<Person> criteria = cbf.create(em, Person.class).from(Document.class, "d");
        criteria.select("Person[name = d.name]");

        assertEquals("SELECT Person_" + ArrayExpression.ELEMENT_NAME + "_name___d_name_1 FROM Document d" +
                " LEFT JOIN Person Person_" + ArrayExpression.ELEMENT_NAME + "_name___d_name_1"
                + onClause("Person_" + ArrayExpression.ELEMENT_NAME + "_name___d_name_1.name = d.name"), criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testEntityArrayReuse() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class).from(Document.class, "d");
        criteria.select("Person[name = d.name].id");
        criteria.select("Person[name = d.name].name");

        assertEquals("SELECT Person_" + ArrayExpression.ELEMENT_NAME + "_name___d_name_1.id, Person_" + ArrayExpression.ELEMENT_NAME + "_name___d_name_1.name FROM Document d" +
                " LEFT JOIN Person Person_" + ArrayExpression.ELEMENT_NAME + "_name___d_name_1"
                + onClause("Person_" + ArrayExpression.ELEMENT_NAME + "_name___d_name_1.name = d.name"), criteria.getQueryString());
        criteria.getResultList();
    }
}
