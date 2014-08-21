/*
 * Copyright 2014 Blazebit.
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
package com.blazebit.persistence;

import com.blazebit.persistence.entity.Document;
import com.blazebit.persistence.entity.Workflow;
import java.util.Locale;
import javax.persistence.Tuple;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class ArrayExpressionTest extends AbstractCoreTest {

    @Test
    public void testSelectPathIndex() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("d.contacts[d.idx]");

        assertEquals("SELECT " + joinAliasValue("contacts_d_idx") + " FROM Document d LEFT JOIN d.contacts contacts_d_idx " + ON_CLAUSE
            + " KEY(contacts_d_idx) = d.idx", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testSelectParameterIndex() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("d.contacts[:age]");

        assertEquals("SELECT " + joinAliasValue("contacts_age") + " FROM Document d LEFT JOIN d.contacts contacts_age " + ON_CLAUSE
            + " KEY(contacts_age) = :age", criteria.getQueryString());
        criteria.setParameter("age", 1).getResultList();
    }

    @Test
    public void testSelectMultipleArrayPath() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("d.contacts[:age].localized[d.idx]");

        assertEquals("SELECT " + joinAliasValue("localized_d_idx") + " FROM Document d LEFT JOIN d.contacts contacts_age " + ON_CLAUSE
            + " KEY(contacts_age) = :age LEFT JOIN contacts_age.localized localized_d_idx " + ON_CLAUSE + " KEY(localized_d_idx) = d.idx", criteria
                     .getQueryString());
        criteria.setParameter("age", 1).getResultList();
    }

    @Test
    public void testSelectAlternatingArrayPath() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("d.contacts[:age].partnerDocument.contacts[d.idx]");

        assertEquals("SELECT " + joinAliasValue("contacts_d_idx") + " FROM Document d LEFT JOIN d.contacts contacts_age " + ON_CLAUSE
            + " KEY(contacts_age) = :age LEFT JOIN contacts_age.partnerDocument partnerDocument_1 LEFT JOIN partnerDocument_1.contacts contacts_d_idx "
            + ON_CLAUSE + " KEY(contacts_d_idx) = d.idx", criteria.getQueryString());
        criteria.setParameter("age", 1).getResultList();
    }

    @Test
    public void testArrayIndexImplicitJoin() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("d.contacts[d.versions.idx]");
        
        assertEquals("SELECT " + joinAliasValue("contacts_versions_1_idx") + " FROM Document d LEFT JOIN d.versions versions_1 LEFT JOIN d.contacts contacts_versions_1_idx " + ON_CLAUSE
            + " KEY(contacts_versions_1_idx) = versions_1.idx", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testArrayIndexExplicitJoinAlias() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("d.contacts[v.idx]").leftJoinDefault("d.versions", "v");

        assertEquals("SELECT " + joinAliasValue("contacts_v_idx") + " FROM Document d LEFT JOIN d.versions v LEFT JOIN d.contacts contacts_v_idx " + ON_CLAUSE
            + " KEY(contacts_v_idx) = v.idx", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testRedundantArrayTransformation() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("contacts[1]").where("contacts[1]").isNull();

        assertEquals("SELECT " + joinAliasValue("contacts_1") + " FROM Document d LEFT JOIN d.contacts contacts_1 " + ON_CLAUSE
            + " KEY(contacts_1) = 1 WHERE " + joinAliasValue("contacts_1") + " IS NULL", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // Map dereferencing is actually not allowed in JPQL
    public void testMapDereferencing() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("owner.partnerDocument", "x").leftJoinDefault("owner.partnerDocument", "p").where("p.contacts[1].name").isNull();

        assertEquals(
            "SELECT p AS x FROM Document d JOIN d.owner owner_1 LEFT JOIN owner_1.partnerDocument p LEFT JOIN p.contacts contacts_1 "
            + ON_CLAUSE + " KEY(contacts_1) = 1 WHERE contacts_1.name IS NULL", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testMore() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("owner.partnerDocument", "x").leftJoinDefault("owner.partnerDocument", "p").leftJoinDefault("p.contacts", "c").where(
            "c[1]").isNull();

        assertEquals(
            "SELECT p AS x FROM Document d JOIN d.owner owner_1 LEFT JOIN owner_1.partnerDocument p LEFT JOIN p.contacts c "
            + ON_CLAUSE + " KEY(c) = 1 WHERE " + joinAliasValue("c") + " IS NULL", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testMore2() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("d.contacts[1].partnerDocument.name", "x");

        assertEquals("SELECT partnerDocument_1.name AS x FROM Document d LEFT JOIN d.contacts contacts_1 " + ON_CLAUSE
            + " KEY(contacts_1) = 1 LEFT JOIN contacts_1.partnerDocument partnerDocument_1", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testMapSelectWithAlias() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("d.contacts[1]", "x");

        assertEquals("SELECT " + joinAliasValue("contacts_1") + " AS x FROM Document d LEFT JOIN d.contacts contacts_1 " + ON_CLAUSE
            + " KEY(contacts_1) = 1", criteria.getQueryString());
        criteria.getResultList();
    }
}
