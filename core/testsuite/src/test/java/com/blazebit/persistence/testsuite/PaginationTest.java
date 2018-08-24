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

import static com.googlecode.catchexception.CatchException.verifyException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;

import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.Workflow;
import com.blazebit.persistence.testsuite.model.DocumentViewModel;
import org.junit.experimental.categories.Category;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class PaginationTest extends AbstractCoreTest {

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Document doc1 = new Document("doc1");
                Document doc2 = new Document("Doc2");
                Document doc3 = new Document("doC3");
                Document doc4 = new Document("dOc4");
                Document doc5 = new Document("DOC5");
                Document doc6 = new Document("bdoc");
                Document doc7 = new Document("adoc");

                Person o1 = new Person("Karl1");
                Person o2 = new Person("Karl2");
                Person o3 = new Person("Moritz");
                o1.getLocalized().put(1, "abra kadabra");
                o2.getLocalized().put(1, "ass");

                doc1.setOwner(o1);
                doc2.setOwner(o1);
                doc3.setOwner(o1);
                doc4.setOwner(o2);
                doc5.setOwner(o2);
                doc6.setOwner(o2);
                doc7.setOwner(o2);

                doc1.getContacts().put(1, o1);
                doc1.getContacts().put(2, o2);

                doc4.getContacts().put(1, o3);

                em.persist(o1);
                em.persist(o2);
                em.persist(o3);

                em.persist(doc1);
                em.persist(doc2);
                em.persist(doc3);
                em.persist(doc4);
                em.persist(doc5);
                em.persist(doc6);
                em.persist(doc7);
            }
        });
    }

    @Test
    public void simpleTest() {
        CriteriaBuilder<DocumentViewModel> crit = cbf.create(em, Document.class, "d")
                .selectNew(DocumentViewModel.class)
                .with("d.name")
                .with("CONCAT(d.owner.name, ' user')")
                .with("COALESCE(d.owner.localized[1],'no item')")
                .with("d.owner.partnerDocument.name")
                .end();
        crit.where("d.name").like(false).value("doc%").noEscape();
        crit.where("d.owner.name").like().value("%arl%").noEscape();
        crit.where("d.owner.localized[1]").like(false).value("a%").noEscape();
        crit.orderByAsc("d.id");

        // do not include joins that are only needed for the select clause
        String expectedCountQuery = "SELECT " + countPaginated("d.id", false) + " FROM Document d JOIN d.owner owner_1 LEFT JOIN owner_1.localized localized_1_1"
                + onClause("KEY(localized_1_1) = 1")
                + " WHERE UPPER(d.name) LIKE UPPER(:param_0) AND owner_1.name LIKE :param_1 AND UPPER(" + joinAliasValue("localized_1_1")
                + ") LIKE UPPER(:param_2)";

        String expectedObjectQuery = "SELECT d.name, CONCAT(owner_1.name,' user'), COALESCE(" + joinAliasValue("localized_1_1")
                + ",'no item'), partnerDocument_1.name FROM Document d "
                + "JOIN d.owner owner_1 LEFT JOIN owner_1.localized localized_1_1"
                + onClause("KEY(localized_1_1) = 1")
                + " LEFT JOIN owner_1.partnerDocument partnerDocument_1 "
                + "WHERE UPPER(d.name) LIKE UPPER(:param_0) AND owner_1.name LIKE :param_1 AND UPPER(" + joinAliasValue("localized_1_1")
                + ") LIKE UPPER(:param_2) "
                + "ORDER BY d.id ASC";

        PaginatedCriteriaBuilder<DocumentViewModel> pcb = crit.page(0, 2);

        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());

        PagedList<DocumentViewModel> result = pcb.getResultList();
        assertEquals(2, result.size());
        assertEquals(5, result.getTotalSize());
        assertEquals("doc1", result.get(0).getName());
        assertEquals("Doc2", result.get(1).getName());

        result = crit.page(2, 2).getResultList();
        assertEquals("doC3", result.get(0).getName());
        assertEquals("dOc4", result.get(1).getName());

        result = crit.page(4, 2).getResultList();
        assertEquals(result.size(), 1);
        assertEquals("DOC5", result.get(0).getName());
    }

    @Test
    public void simpleTestFetch() {
        PaginatedCriteriaBuilder<Document> cb = cbf.create(em, Document.class, "d")
            .where("d.name").like(true).value("doc%").noEscape()
            .where("owner.name").eq("Karl1")
            .orderByAsc("d.id")
            .fetch("owner")
            .page(0, 1);
        List<Document> result = cb.getResultList();
        assertEquals(1, result.size());
        assertEquals("doc1", result.get(0).getName());
    }

    @Test
    public void testSelectIndexedWithParameter() {
        String expectedCountQuery = "SELECT " + countPaginated("d.id", false) + " FROM Document d JOIN d.owner owner_1 WHERE owner_1.name = :param_0";
        String expectedObjectQuery = "SELECT " + joinAliasValue("contacts_contactNr_1", "name") + " FROM Document d " +
                "LEFT JOIN d.contacts contacts_contactNr_1"
                + onClause("KEY(contacts_contactNr_1) = :contactNr")
                + " JOIN d.owner owner_1 WHERE owner_1.name = :param_0 ORDER BY d.id ASC";
        PaginatedCriteriaBuilder<Document> cb = cbf.create(em, Document.class, "d")
                .where("owner.name").eq("Karl1")
                .select("contacts[:contactNr].name")
                .orderByAsc("id")
                .page(0, 1);
        assertEquals(expectedCountQuery, cb.getPageCountQueryString());
        assertEquals(expectedObjectQuery, cb.getQueryString());
        cb.setParameter("contactNr", 1).getResultList();
    }

    @Test
    public void testSelectIndexedWithParameterForceIdQuery() {
        String expectedCountQuery = "SELECT " + countPaginated("d.id", false) + " FROM Document d JOIN d.owner owner_1 WHERE owner_1.name = :param_0";
        String expectedIdQuery = "SELECT d.id FROM Document d JOIN d.owner owner_1 WHERE owner_1.name = :param_0 GROUP BY " + groupBy("d.id", "d.id") + " ORDER BY d.id ASC";
        String expectedObjectQuery = "SELECT " + joinAliasValue("contacts_contactNr_1", "name") + " FROM Document d " +
                "LEFT JOIN d.contacts contacts_contactNr_1"
                + onClause("KEY(contacts_contactNr_1) = :contactNr")
                + " WHERE d.id IN :ids ORDER BY d.id ASC";
        PaginatedCriteriaBuilder<Document> cb = cbf.create(em, Document.class, "d")
                .where("owner.name").eq("Karl1")
                .select("contacts[:contactNr].name")
                .orderByAsc("id")
                .page(0, 1)
                .withForceIdQuery(true);
        assertEquals(expectedCountQuery, cb.getPageCountQueryString());
        assertEquals(expectedIdQuery, cb.getPageIdQueryString());
        assertEquals(expectedObjectQuery, cb.getQueryString());
        cb.setParameter("contactNr", 1).getResultList();
    }

    @Test
    public void testSelectEmptyResultList() {
        PaginatedCriteriaBuilder<Document> cb = cbf.create(em, Document.class, "d")
                .where("name").isNull()
                .orderByAsc("name")
                .orderByAsc("id")
                .page(0, 1);
        assertEquals(0, cb.getResultList().size());
        cb.getResultList();
    }

    @Test
    @Category(NoEclipselink.class)
    // TODO: Maybe report that EclipseLink seemingly can't handle subqueries in functions
    public void testPaginationWithReferenceObject() {
        Document reference = cbf.create(em, Document.class).where("name").eq("adoc").getSingleResult();
        String expectedCountQuery =
                "SELECT " + countPaginated("d.id", false) + ", "
                + function("PAGE_POSITION",
                        "(SELECT _page_position_d.id "
                        + "FROM Document _page_position_d "
                        + "GROUP BY " + groupBy("_page_position_d.id", "_page_position_d.name", "_page_position_d.id")
                        + " ORDER BY _page_position_d.name ASC, _page_position_d.id ASC)",
                        ":_entityPagePositionParameter")
                    + " "
                + "FROM Document d";
        
        PaginatedCriteriaBuilder<Document> cb = cbf.create(em, Document.class, "d")
                .orderByAsc("name")
                .orderByAsc("id")
                .page(reference.getId(), 1);
        
        List<Document> originalList = cbf.create(em, Document.class, "d")
                .orderByAsc("name")
                .orderByAsc("id")
                .getResultList();
        // Apparently Datanucleus doesn't implement all java.util.List methods properly 
        originalList = new ArrayList<Document>(originalList);
        
        assertEquals(expectedCountQuery, cb.getPageCountQueryString());
        PagedList<Document> list = cb.getResultList();
        assertEquals(originalList.indexOf(reference), list.getFirstResult());
        assertEquals(originalList.indexOf(reference) + 1, list.getPage());
        assertEquals(originalList.size(), list.getTotalPages());
        assertEquals(originalList.size(), list.getTotalSize());
        assertEquals(1, list.size());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testPaginationWithInvalidReferenceObject() {
        cbf.create(em, Document.class, "d").page("test", 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPaginationWithNonUniqueIdentifierExpression() {
        cbf.create(em, Document.class, "d").pageBy(0, 1, "owner.id");
    }

    @Test
    @Category(NoEclipselink.class)
    // TODO: Maybe report that EclipseLink seemingly can't handle subqueries in functions
    public void testPaginationWithNotExistingReferenceObject() {
        Document reference = cbf.create(em, Document.class).where("name").eq("adoc").getSingleResult();
        String expectedCountQuery =
                "SELECT " + countPaginated("d.id", false) + ", "
                + function("PAGE_POSITION",
                        "(SELECT _page_position_d.id "
                        + "FROM Document _page_position_d "
                        + "WHERE _page_position_d.name <> :param_0 "
                        + "GROUP BY " + groupBy("_page_position_d.id", "_page_position_d.name", "_page_position_d.id")
                        + " ORDER BY _page_position_d.name ASC, _page_position_d.id ASC)",
                        ":_entityPagePositionParameter")
                    + " "
                + "FROM Document d "
                + "WHERE d.name <> :param_0";
        
        PaginatedCriteriaBuilder<Document> cb = cbf.create(em, Document.class, "d")
                .where("name").notEq("adoc")
                .orderByAsc("name")
                .orderByAsc("id")
                .page(reference.getId(), 1);
        PaginatedCriteriaBuilder<Document> firstPageCb = cbf.create(em, Document.class, "d")
                .where("name").notEq("adoc")
                .orderByAsc("name")
                .orderByAsc("id")
                .page(0, 1);
        
        assertEquals(expectedCountQuery, cb.getPageCountQueryString());
        PagedList<Document> expectedList = firstPageCb.getResultList();
        PagedList<Document> list = cb.getResultList();
        assertEquals(expectedList, list);
        
        assertEquals(-1, list.getFirstResult());
        assertEquals(1, list.getPage());
        assertEquals(6, list.getTotalPages());
        assertEquals(6, list.getTotalSize());
        assertEquals(1, list.size());
    }

    @Test
    // Apparently, Datanucleus doesn't like it when using a joined element collection in a function
    @Category(NoDatanucleus.class)
    public void testPaginatedWithGroupByExplicitPagination() {
        String expectedCountQuery = "SELECT " + countPaginated("d.id", false) + " FROM Document d";
        String expectedIdQuery = "SELECT d.id FROM Document d GROUP BY " + groupBy("d.id") + " ORDER BY d.id ASC";
        String expectedObjectQuery = "SELECT d.id, strings_1, COUNT(" + joinAliasValue("contacts_1", "id") + ") FROM Document d LEFT JOIN d.contacts contacts_1 LEFT JOIN d.strings strings_1"
                + " WHERE d.id IN :ids"
                + " GROUP BY " + groupBy("d.id", "strings_1")
                + " ORDER BY d.id ASC";
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("d.id").select("strings").select("COUNT(contacts.id)").groupBy("id").orderByAsc("d.id");
        PaginatedCriteriaBuilder<Tuple> pcb = cb.pageBy(0, 1, "d.id");
        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        assertEquals(expectedIdQuery, pcb.getPageIdQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());
        pcb.getResultList();
    }

    @Test
    // Apparently, Datanucleus doesn't like it when using a joined element collection in a function
    @Category(NoDatanucleus.class)
    public void testPaginatedWithGroupBy1() {
        String expectedCountQuery = "SELECT " + countPaginated("d.id, "+ "strings_1", true) + " FROM Document d LEFT JOIN d.strings strings_1";
        String expectedIdQuery = "SELECT d.id, strings_1 FROM Document d LEFT JOIN d.strings strings_1 GROUP BY " + groupBy("d.id", "strings_1") + " ORDER BY d.id ASC";
        String expectedObjectQuery = "SELECT d.id, strings_1, COUNT(" + joinAliasValue("contacts_1", "id") + ") FROM Document d LEFT JOIN d.contacts contacts_1 LEFT JOIN d.strings strings_1"
                + " WHERE (d.id = :ids_0_0 AND strings_1 = :ids_1_0)"
                + " GROUP BY " + groupBy("d.id", "strings_1")
                + " ORDER BY d.id ASC";
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("d.id").select("strings").select("COUNT(contacts.id)").groupBy("id").orderByAsc("d.id");
        PaginatedCriteriaBuilder<Tuple> pcb = cb.page(0, 1);
        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        assertEquals(expectedIdQuery, pcb.getPageIdQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());
        pcb.getResultList();
    }

    @Test
    // Apparently, Datanucleus doesn't like it when using a joined element collection in a function
    @Category(NoDatanucleus.class)
    public void testPaginatedWithGroupBy2() {
        String expectedCountQuery = "SELECT " + countPaginated("d.id, " + "strings_1", true) + " FROM Document d LEFT JOIN d.strings strings_1";
        String expectedIdQuery = "SELECT d.id, strings_1 FROM Document d LEFT JOIN d.strings strings_1 GROUP BY " + groupBy("d.id", "strings_1") + " ORDER BY d.id ASC";
        String expectedObjectQuery = "SELECT d.id, strings_1, COUNT(" + joinAliasValue("contacts_1", "id") + ") FROM Document d LEFT JOIN d.contacts contacts_1 LEFT JOIN d.strings strings_1"
                + " WHERE (d.id = :ids_0_0 AND strings_1 = :ids_1_0)"
                + " GROUP BY " + groupBy("d.id", "strings_1")
                + " ORDER BY d.id ASC";
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("d.id").select("strings").select("COUNT(contacts.id)");
        cb.page(0, 1);
        cb.groupBy("id").orderByAsc("d.id");

        PaginatedCriteriaBuilder<Tuple> pcb = cb.page(0, 1);
        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        assertEquals(expectedIdQuery, pcb.getPageIdQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());
        pcb.getResultList();
    }

    @Test
    // Apparently, Datanucleus doesn't like it when using a joined element collection in a function
    @Category(NoDatanucleus.class)
    public void testPaginatedWithGroupBy3() {
        String expectedCountQuery = "SELECT " + countPaginated("d.id, " + "strings_1", true) + " FROM Document d LEFT JOIN d.strings strings_1";
        String expectedIdQuery = "SELECT d.id, strings_1 FROM Document d LEFT JOIN d.strings strings_1 GROUP BY " + groupBy("d.id", "strings_1") + " ORDER BY d.id ASC";
        String expectedObjectQuery = "SELECT d.id, " + "strings_1" +", COUNT(" + joinAliasValue("contacts_1", "id") + ") FROM Document d LEFT JOIN d.contacts contacts_1 LEFT JOIN d.strings strings_1"
                + " WHERE (d.id = :ids_0_0 AND strings_1 = :ids_1_0)"
                + " GROUP BY " + groupBy("d.id", "strings_1")
                + " ORDER BY d.id ASC";
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("d.id").select("strings").select("COUNT(contacts.id)");
        cb.page(0, 1);
        cb.groupBy("id", "name").orderByAsc("d.id");

        PaginatedCriteriaBuilder<Tuple> pcb = cb.page(0, 1);
        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        assertEquals(expectedIdQuery, pcb.getPageIdQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());
        pcb.getResultList();
    }

    @Test
    // Apparently, Datanucleus doesn't like it when using a joined element collection in a function
    @Category(NoDatanucleus.class)
    public void testPaginatedWithGroupBy4() {
        String expectedCountQuery = "SELECT " + countPaginated("d.id, " + "strings_1", true) + " FROM Document d LEFT JOIN d.strings strings_1";
        String expectedIdQuery = "SELECT d.id, strings_1 FROM Document d LEFT JOIN d.strings strings_1 GROUP BY " + groupBy("d.id", "strings_1") + " ORDER BY d.id ASC";
        String expectedObjectQuery = "SELECT d.id, strings_1, COUNT(" + joinAliasValue("contacts_1", "id") + ") FROM Document d LEFT JOIN d.contacts contacts_1 LEFT JOIN d.strings strings_1"
                + " WHERE (d.id = :ids_0_0 AND strings_1 = :ids_1_0)"
                + " GROUP BY " + groupBy("d.id", "strings_1")
                + " ORDER BY d.id ASC";
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("d.id").select("strings").select("COUNT(contacts.id)");
        cb.page(0, 1);
        cb.orderByAsc("d.id");

        PaginatedCriteriaBuilder<Tuple> pcb = cb.page(0, 1);
        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        assertEquals(expectedIdQuery, pcb.getPageIdQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());
        pcb.getResultList();
    }

    @Test
    // Apparently, Datanucleus doesn't like it when using a joined element collection in a function
    @Category(NoDatanucleus.class)
    public void testPaginatedWithGroupBy5() {
        String expectedCountQuery = "SELECT " + countPaginated("d.id, "+ "strings_1", true) + " FROM Document d LEFT JOIN d.strings strings_1";
        String expectedIdQuery = "SELECT d.id, strings_1 FROM Document d LEFT JOIN d.strings strings_1 GROUP BY " + groupBy("d.id", "d.name", "strings_1") + " ORDER BY d.name ASC, d.id ASC";
        String expectedObjectQuery = "SELECT d.id, strings_1, COUNT(" + joinAliasValue("contacts_1", "id") + ") FROM Document d LEFT JOIN d.contacts contacts_1 LEFT JOIN d.strings strings_1"
                + " WHERE (d.id = :ids_0_0 AND strings_1 = :ids_1_0)"
                + " GROUP BY " + groupBy("d.id", "strings_1", "d.name")
                + " ORDER BY d.name ASC, d.id ASC";
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("d.id").select("strings").select("COUNT(contacts.id)");
        cb.page(0, 1);
        cb.orderByAsc("d.name").orderByAsc("d.id");

        PaginatedCriteriaBuilder<Tuple> pcb = cb.page(0, 1);
        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        assertEquals(expectedIdQuery, pcb.getPageIdQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());
        pcb.getResultList();
    }

    @Test
    // Apparently, Datanucleus doesn't like it when using a joined element collection in a function
    // Eclipselink does not render the table alias necessary for the path expression in the count function...
    @Category({ NoEclipselink.class, NoDatanucleus.class })
    public void testPaginatedWithGroupBy6() {
        String expectedCountQuery = "SELECT " + countPaginated("d.name, " + "strings_1", true) + " FROM Document d LEFT JOIN d.strings strings_1";
        String expectedIdQuery = "SELECT d.name, strings_1 FROM Document d LEFT JOIN d.strings strings_1 GROUP BY " + groupBy("d.name", "strings_1") + " ORDER BY d.name ASC, " + renderNullPrecedence("strings_1", "ASC", "LAST");
        String expectedObjectQuery = "SELECT d.name, strings_1, COUNT(" + joinAliasValue("contacts_1", "id") + ") FROM Document d LEFT JOIN d.contacts contacts_1 LEFT JOIN d.strings strings_1"
                + " WHERE (d.name = :ids_0_0 AND strings_1 = :ids_1_0)"
                + " GROUP BY " + groupBy("d.name", "strings_1")
                + " ORDER BY d.name ASC, " + renderNullPrecedence("strings_1", "ASC", "LAST");
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("d.name").select("strings").select("COUNT(contacts.id)");
        cb.page(0, 1);
        cb.groupBy("d.name").orderByAsc("d.name").orderByAsc("strings");

        PaginatedCriteriaBuilder<Tuple> pcb = cb.page(0, 1);
        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        assertEquals(expectedIdQuery, pcb.getPageIdQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());
        pcb.getResultList();
    }

    @Test
    // Apparently, Datanucleus doesn't like it when using a joined element collection in a function
    // Eclipselink does not render the table alias necessary for the path expression in the count function...
    @Category({ NoEclipselink.class, NoDatanucleus.class })
    public void testPaginatedWithGroupBy7() {
        String expectedCountQuery = "SELECT " + countPaginated("d.name, d.age, " + "strings_1", true) + " FROM Document d LEFT JOIN d.strings strings_1";
        String expectedIdQuery = "SELECT d.name, d.age, strings_1 FROM Document d LEFT JOIN d.strings strings_1 GROUP BY " + groupBy("d.name", "strings_1", "d.age") + " ORDER BY d.name ASC, d.age ASC, " + renderNullPrecedence("strings_1", "ASC", "LAST");
        String expectedObjectQuery = "SELECT d.name, strings_1, COUNT(" + joinAliasValue("contacts_1", "id") + ") FROM Document d LEFT JOIN d.contacts contacts_1 LEFT JOIN d.strings strings_1"
                + " WHERE (d.name = :ids_0_0 AND d.age = :ids_1_0 AND strings_1 = :ids_2_0)"
                + " GROUP BY " + groupBy("d.name", "strings_1", "d.age")
                + " ORDER BY d.name ASC, d.age ASC, " + renderNullPrecedence("strings_1", "ASC", "LAST");
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("d.name").select("strings").select("COUNT(contacts.id)");
        cb.page(0, 1);
        cb.groupBy("d.name").orderByAsc("d.name").orderByAsc("d.age").orderByAsc("strings");

        PaginatedCriteriaBuilder<Tuple> pcb = cb.page(0, 1);
        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        assertEquals(expectedIdQuery, pcb.getPageIdQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());
        pcb.getResultList();
    }

    @Test
    // Apparently, Datanucleus doesn't like it when using a joined element collection in a function
    // Eclipselink does not render the table alias necessary for the path expression in the count function...
    @Category({ NoEclipselink.class, NoDatanucleus.class })
    public void testPaginatedWithGroupBy8() {
        String expectedCountQuery = "SELECT " + countPaginated("d.id", false) + " FROM Document d";
        String expectedIdQuery = "SELECT d.id FROM Document d GROUP BY " + groupBy("d.id", "d.name", "d.age") + " ORDER BY d.name ASC, d.age ASC, d.id ASC";
        String expectedObjectQuery = "SELECT d.id, d.name, strings_1, COUNT(" + joinAliasValue("contacts_1", "id") + ") FROM Document d LEFT JOIN d.contacts contacts_1 LEFT JOIN d.strings strings_1"
                + " WHERE d.id IN :ids"
                + " GROUP BY " + groupBy("d.id", "d.name", "strings_1", "d.age")
                + " ORDER BY d.name ASC, d.age ASC, d.id ASC";
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("d.id")
                .select("d.name")
                .select("strings")
                .select("COUNT(contacts.id)");
        cb.page(0, 1);
        cb.orderByAsc("d.name").orderByAsc("d.age").orderByAsc("d.id");

        PaginatedCriteriaBuilder<Tuple> pcb = cb.pageBy(0, 1, "d.id");
        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        assertEquals(expectedIdQuery, pcb.getPageIdQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());
        pcb.getResultList();
    }

    @Test
    // Apparently, Datanucleus doesn't like it when using a joined element collection in a function
    // Eclipselink does not render the table alias necessary for the path expression in the count function...
    @Category({ NoEclipselink.class, NoDatanucleus.class })
    public void testPaginatedWithGroupBy9() {
        String expectedCountQuery = "SELECT " + countPaginated("d.id", false) + " FROM Document d";
        String expectedIdQuery = "SELECT d.id FROM Document d GROUP BY " + groupBy("d.id") + " ORDER BY d.id ASC";
        String expectedObjectQuery = "SELECT d.id, d.name, strings_1, COUNT(" + joinAliasValue("contacts_1", "id") + ") FROM Document d LEFT JOIN d.contacts contacts_1 LEFT JOIN d.strings strings_1"
                + " WHERE d.id IN :ids"
                + " GROUP BY " + groupBy("d.id", "d.name", "strings_1")
                + " ORDER BY d.id ASC";
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("d.id")
                .select("d.name")
                .select("strings")
                .select("COUNT(contacts.id)");
        cb.page(0, 1);
        cb.orderByAsc("d.id");

        PaginatedCriteriaBuilder<Tuple> pcb = cb.pageBy(0, 1, "d.id");
        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        assertEquals(expectedIdQuery, pcb.getPageIdQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());
        pcb.getResultList();
    }

    @Test
    public void testPaginatedWithGroupBy10() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("d.id")
                .select("d.name")
                .select("COUNT(contacts.id)");
        cb.page(0, 1);
        cb.groupBy("d.name").orderByAsc("d.name").orderByAsc("d.age");

        // No unique ordering
        PaginatedCriteriaBuilder<Tuple> pcb = cb.pageBy(0, 1, "d.id");
        verifyException(pcb, IllegalStateException.class).getQueryString();
    }

    @Test
    @Category(NoEclipselink.class)
    // Eclipselink does not render the table alias necessary for the path expression in the count function...
    public void testPaginatedWithGroupByGroupAwayCollections() {
        String expectedCountQuery = "SELECT " + countPaginated("d.id", false) + " FROM Document d";
        String expectedObjectQuery = "SELECT d.id, d.name, COUNT(" + joinAliasValue("contacts_1", "id") + ") FROM Document d LEFT JOIN d.contacts contacts_1"
                + " GROUP BY " + groupBy("d.id", "d.name")
                + " ORDER BY d.id ASC";
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("d.id")
                .select("d.name")
                .select("COUNT(contacts.id)");
        cb.page(0, 1);
        cb.orderByAsc("d.id");

        PaginatedCriteriaBuilder<Tuple> pcb = cb.pageBy(0, 1, "d.id");
        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());
        pcb.getResultList();
    }

    @Test
    public void testPaginateExplicitWithGroupBy() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("d.name").select("COUNT(contacts.id)");
        cb.page(0, 1);
        cb.groupBy("d.name").orderByAsc("d.name").orderByAsc("d.age");
        PaginatedCriteriaBuilder<Tuple> pcb = cb.pageBy(0, 1, "d.id");
        verifyException(pcb, IllegalStateException.class).getQueryString();
    }

    @Test
    public void testGroupByExplicitPaginated() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("d.name").select("COUNT(contacts.id)");
        cb.page(0, 1);
        cb.orderByAsc("d.name").orderByAsc("d.age");
        cb.pageBy(0, 1, "d.id");
        try {
            cb.groupBy("d.name");
            fail("Expected exception");
        } catch (IllegalStateException ex) {
            // Expected
        }
    }

    @Test
    public void testPaginateExplicitWithImplicitGroupBy() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("d.name").select("COUNT(contacts.id)");
        cb.page(0, 1);
        cb.orderByAsc("d.name").orderByAsc("d.age");

        PaginatedCriteriaBuilder<Tuple> pcb = cb.pageBy(0, 1, "d.id");
        verifyException(pcb, IllegalStateException.class).getQueryString();
    }

    @Test
    public void testImplicitGroupByExplicitPaginated() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("d.name").select("COUNT(contacts.id)");
        cb.page(0, 1);
        cb.orderByAsc("d.name").orderByAsc("d.age");
        cb.pageBy(0, 1, "d.id");
        verifyException(cb, IllegalStateException.class);
    }

    @Test
    public void testPaginatedWithDistinct1() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("d.id").select("COUNT(contacts.id)").distinct();
        verifyException(cb, IllegalStateException.class).page(0, 1);
    }

    @Test
    public void testPaginatedWithDistinct2() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("d.id").select("COUNT(contacts.id)");
        cb.page(0, 1);
        try {
            cb.distinct();
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException ex) {
            // OK, we expected that
        }
    }

    @Test
    @Category(NoEclipselink.class)
    // Eclipselink does not support dereferencing of VALUE() functions
    public void testOrderByExpression() {
        PaginatedCriteriaBuilder<Document> cb = cbf.create(em, Document.class, "d")
                .orderByAsc("contacts[:contactNr].name")
                .orderByAsc("id")
                .setParameter("contactNr", 1)
                .page(0, 1);
        String expectedObjectQuery = "SELECT d FROM Document d LEFT JOIN d.contacts contacts_contactNr_1"
                + onClause("KEY(contacts_contactNr_1) = :contactNr")
                + " ORDER BY " + renderNullPrecedence(joinAliasValue("contacts_contactNr_1", "name"), "ASC", "LAST") + ", d.id ASC";
        assertEquals(expectedObjectQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    @Category(NoEclipselink.class)
    // Eclipselink does not support dereferencing of VALUE() functions
    public void testOrderBySelectAlias() {
        PaginatedCriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("contacts[:contactNr].name", "contactName")
                .orderByAsc("contactName")
                .orderByAsc("id")
                .setParameter("contactNr", 1)
                .page(0, 1);
        String expectedObjectQuery = "SELECT " + joinAliasValue("contacts_contactNr_1", "name") + " AS contactName FROM Document d LEFT JOIN d.contacts contacts_contactNr_1"
                + onClause("KEY(contacts_contactNr_1) = :contactNr")
                + " ORDER BY " + renderNullPrecedence("contactName", joinAliasValue("contacts_contactNr_1", "name"),  "ASC", "LAST") + ", d.id ASC";
        assertEquals(expectedObjectQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testOrderBySubquery() {
        PaginatedCriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .selectSubquery("contactCount")
                    .from(Document.class, "d2")
                    .select("COUNT(d2.contacts.id)")
                    .where("d2.id").eqExpression("d.id")
                .end()
                .orderByAsc("contactCount")
                .orderByAsc("id")
                .page(0, 1);
        String expectedSubQuery = "(SELECT COUNT(" + joinAliasValue("contacts_1", "id") + ") FROM Document d2 LEFT JOIN d2.contacts contacts_1 WHERE d2.id = d.id)";
        String expectedObjectQuery = "SELECT " + expectedSubQuery + " AS contactCount FROM Document d"
                + " ORDER BY contactCount ASC, d.id ASC";
        assertEquals(expectedObjectQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    // NOTE: We haven't handeled SIZE transformations for all clauses yet
    // TODO: 188
    @Ignore("#188")
    public void testOrderBySize() {
        PaginatedCriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("SIZE(d.contacts)")
                .orderByAsc("SIZE(d.contacts)")
                .orderByAsc("id")
                .page(0, 1);
        String expectedIdQuery = "SELECT d.id FROM Document d GROUP BY " + groupBy("d.id", "SIZE(d.contacts)", "d.id") + " ORDER BY SIZE(d.contacts) ASC, d.id ASC";
        String expectedCountQuery = "SELECT " + countPaginated("d.id", false) + " FROM Document d";
        String expectedObjectQuery = "SELECT COUNT(" + joinAliasValue("contacts_1") + ") FROM Document d LEFT JOIN d.contacts contacts_1 WHERE d.id IN :ids GROUP BY " + groupBy("d.id", "SIZE(d.contacts)", "d.id") + " ORDER BY SIZE(d.contacts) ASC, d.id ASC";

        assertEquals(expectedIdQuery, cb.getPageIdQueryString());
        assertEquals(expectedCountQuery, cb.getPageCountQueryString());
        assertEquals(expectedObjectQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testOrderBySizeAlias() {
        PaginatedCriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("SIZE(d.contacts)", "contactCount")
                .orderByAsc("contactCount")
                .orderByAsc("id")
                .page(0, 1);
        String expectedCountQuery = "SELECT " + countPaginated("d.id", false) + " FROM Document d";
        String expectedObjectQuery = "SELECT " + function("COUNT_TUPLE", "KEY(contacts_1)") + " AS contactCount FROM Document d LEFT JOIN d.contacts contacts_1 "
                + "GROUP BY " + groupBy("d.id")
                + " ORDER BY contactCount ASC, d.id ASC";

        assertEquals(expectedCountQuery, cb.getPageCountQueryString());
        assertEquals(expectedObjectQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    // Apparently, Datanucleus doesn't like it when using a joined element collection in a function
    @Category(NoDatanucleus.class)
    public void testOrderBySizeAlias2() {
        PaginatedCriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("SIZE(d.contacts)", "contactCount")
                .select("strings")
                .orderByAsc("contactCount")
                .orderByAsc("id")
                .page(0, 1);
        String expectedIdQuery = "SELECT d.id, strings_1, " + function("COUNT_TUPLE", "'DISTINCT',KEY(contacts_1)") + " AS contactCount FROM Document d LEFT JOIN d.contacts contacts_1 LEFT JOIN d.strings strings_1 "
                + "GROUP BY " + groupBy("d.id", "strings_1")
                + " ORDER BY contactCount ASC, d.id ASC";
        String expectedCountQuery = "SELECT " + countPaginated("d.id, " + "strings_1", true) + " FROM Document d LEFT JOIN d.strings strings_1";
        String expectedObjectQuery = "SELECT " + function("COUNT_TUPLE", "'DISTINCT',KEY(contacts_1)") + " AS contactCount, strings_1 FROM Document d LEFT JOIN d.contacts contacts_1 LEFT JOIN d.strings strings_1 " +
                "WHERE (d.id = :ids_0_0 AND strings_1 = :ids_1_0) "
                + "GROUP BY " + groupBy("d.id", "strings_1")
                + " ORDER BY contactCount ASC, d.id ASC";

        assertEquals(expectedIdQuery, cb.getPageIdQueryString());
        assertEquals(expectedCountQuery, cb.getPageCountQueryString());
        assertEquals(expectedObjectQuery, cb.getQueryString());
        cb.getResultList();
    }
    
    @Test
    @Category(NoEclipselink.class)
    // TODO: report eclipse bug, the expression "VALUE(c) IS NULL" seems illegal but JPA spec 4.6.11 allows it
    public void testSelectOnlyPropagationForWithJoins1() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d");
        PaginatedCriteriaBuilder<Tuple> pcb = cb.select("d.contacts[d.owner.age]").where("d.contacts").isNull().orderByAsc("id").page(0, 1);

        // NOTE: This test is a bit stupid. The where clause references a different join node(d.contacts) than what is selected(d.contacts[d.owner.age]). Don't mix them up
        String expectedCountQuery = "SELECT " + countPaginated("d.id", true) + " FROM Document d LEFT JOIN d.contacts contacts_1 WHERE " + joinAliasValue("contacts_1") + " IS NULL";
        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        pcb.getPageCountQueryString();
        cb.getResultList();
    }
    
    @Test
    @Category(NoEclipselink.class)
    // TODO: report eclipse bug, the expression "VALUE(c) IS NULL" seems illegal but JPA spec 4.6.11 allows it
    public void testSelectOnlyPropagationForWithJoins2() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d");
        PaginatedCriteriaBuilder<Tuple> pcb = cb.select("d.contacts[d.owner.age]").where("d.contacts[d.owner.age]").isNull().orderByAsc("id").page(0, 1);

        String expectedCountQuery = "SELECT " + countPaginated("d.id", false) + " FROM Document d JOIN d.owner owner_1 " +
                "LEFT JOIN d.contacts contacts_owner_1_age_1"
                + onClause("KEY(contacts_owner_1_age_1) = owner_1.age")
                + " WHERE " + joinAliasValue("contacts_owner_1_age_1") + " IS NULL";
        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        pcb.getPageCountQueryString();
        cb.getResultList();
    }

    @Test
    @Category(NoEclipselink.class)
    // TODO: report eclipse bug, the expression "VALUE(c) IS NULL" seems illegal but JPA spec 4.6.11 allows it
    public void testSelectOnlyPropagationForWithJoins3() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d");
        PaginatedCriteriaBuilder<Tuple> pcb = cb
                .select("c")
                .leftJoinOn("d.contacts", "c")
                .on("KEY(c)").eqExpression("d.owner.age")
                .end()
                .where("c").isNull().orderByAsc("id").page(0, 1);

        String expectedCountQuery = "SELECT " + countPaginated("d.id", false) + " FROM Document d JOIN d.owner owner_1 " +
                "LEFT JOIN d.contacts c"
                + onClause("KEY(c) = owner_1.age") +
                " WHERE " + joinAliasValue("c") + " IS NULL";
        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        cb.getResultList();
    }
    
    @Test
    @Category(NoEclipselink.class)
    // TODO: Maybe report that EclipseLink has a bug in case when rendering
    public void testCountQueryWhereClauseConjuncts() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Workflow.class, "w");
        PaginatedCriteriaBuilder<Tuple> pcb = cb
            .select("w.id", "Workflow_id")
            .select("w.defaultLanguage", "Workflow_defaultLanguage")
            .select("COALESCE(NULLIF(COALESCE(w.localized[:language].name, w.localized[w.defaultLanguage].name), ''), ' - ')", "Workflow_name")
            .select("COALESCE(NULLIF(COALESCE(w.localized[:language].description, w.localized[w.defaultLanguage].description), ''), ' - ')", "Workflow_description")
            .select("COALESCE(NULLIF(SUBSTRING(COALESCE(w.localized[:language].description, w.localized[w.defaultLanguage].description), 1, 20), ''), ' - ')", "Workflow_descriptionPreview")
            .select("CASE WHEN w.localized[:language].name IS NULL THEN 0 ELSE 1 END", "Workflow_localizedValue")
            .orderByAsc("Workflow_name")
            .orderByAsc("Workflow_id")
            .page(0, 1);

        String expectedCountQuery = "SELECT " + countPaginated("w.id", false) + " FROM Workflow w";

        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        pcb.getPageCountQueryString();
        cb.setParameter("language", Locale.GERMAN).getResultList();
    }
    
    @Test
    public void testPaginationWithoutOrderBy() {
        PaginatedCriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d").page(0, 10);
        verifyException(cb, IllegalStateException.class).getResultList();
    }

    @Test
    public void testPaginationWithoutUniqueOrderBy() {
        PaginatedCriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .orderByAsc("d.name").page(0, 10);
        verifyException(cb, IllegalStateException.class).getResultList();
    }
    
    @Test
    public void testPaginationWithoutUniqueLastOrderBy() {
        PaginatedCriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .orderByAsc("d.id").orderByAsc("d.name").page(0, 10);
        cb.getResultList();
    }
    
    @Test
    public void testPaginationObjectQueryClauseExclusions() {
        PaginatedCriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("id")
                .innerJoinDefault("contacts", "c")
                .where("c.name").eq("Karl1")
                .orderByAsc("d.id").page(0, 10);
        String query = "SELECT d.id FROM Document d JOIN d.contacts c WHERE d.id IN :ids ORDER BY d.id ASC";
        assertEquals(query, cb.getQueryString());
        cb.getResultList();
    }
    
    @Test
    public void testPaginationWithExplicitRestrictingJoin() {
        PaginatedCriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("id").select("c.name")
                .innerJoinDefaultOn("contacts", "c")
                    .on("KEY(c)").eqExpression("1")
                .end()
                .orderByAsc("d.id")
                .page(0, 10);
        
        String countQuery = "SELECT " + countPaginated("d.id", false) + " FROM Document d JOIN d.contacts c"
                + onClause("KEY(c) = 1");
        String objectQuery = "SELECT d.id, " + joinAliasValue("c", "name") + " FROM Document d JOIN d.contacts c"
                + onClause("KEY(c) = 1") +
                " ORDER BY d.id ASC";
        assertEquals(countQuery, cb.getPageCountQueryString());
        assertEquals(objectQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    // Test for issue #420
    public void testPaginationWithJoinFromSelectSubquery() {
        PaginatedCriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .selectSubquery()
                    .from(Person.class, "pSub")
                    .select("COUNT(*)")
                    .where("pSub.id").eqExpression("OUTER(contacts.id)")
                .end()
                .orderByAsc("d.id")
                .page(0, 10);

        String countQuery = "SELECT " + countPaginated("d.id", true) + " FROM Document d LEFT JOIN d.contacts contacts_1";
        String idQuery = "SELECT d.id FROM Document d LEFT JOIN d.contacts contacts_1" +
                " GROUP BY " + groupBy("d.id", "d.id") + " ORDER BY d.id ASC";
        String objectQuery = "SELECT (SELECT " + countStar() + " FROM Person pSub WHERE pSub.id = " + joinAliasValue("contacts_1", "id") + ") FROM Document d LEFT JOIN d.contacts contacts_1" +
                " WHERE d.id IN :ids ORDER BY d.id ASC";
        assertEquals(countQuery, cb.getPageCountQueryString());
        assertEquals(idQuery, cb.getPageIdQueryString());
        assertEquals(objectQuery, cb.getQueryString());
        cb.getResultList();
    }
}
