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
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.DocumentForOneToOne;
import com.blazebit.persistence.testsuite.entity.DocumentInfo;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class PaginationOneToOneTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return concat(super.getEntityClasses(), new Class[]{
                DocumentForOneToOne.class,
                DocumentInfo.class
        });
    }

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                DocumentForOneToOne doc1 = new DocumentForOneToOne("doc1");
                DocumentForOneToOne doc2 = new DocumentForOneToOne("doc2");

                Person p1 = new Person("owner");
                doc1.setOwner(p1);
                doc2.setOwner(p1);

                DocumentInfo o1 = new DocumentInfo(1L, doc2, "Karl1");
                DocumentInfo o2 = new DocumentInfo(2L, doc1, "Karl2");

                em.persist(p1);

                em.persist(doc1);
                em.persist(doc2);

                em.persist(o1);
                em.persist(o2);
            }
        });
    }

    @Test
    public void paginateInverse() {
        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class)
                .from(DocumentForOneToOne.class, "d")
                .select("d.name")
                .select("d.documentInfo.someInfo")
                .orderByAsc("d.documentInfo.id");

        String expectedCountQuery = "SELECT " + countPaginated("d.id", false) + " FROM DocumentForOneToOne d";

        String expectedObjectQuery = "SELECT d.name, documentInfo_1.someInfo FROM DocumentForOneToOne d LEFT JOIN d.documentInfo documentInfo_1"
                + " ORDER BY " + renderNullPrecedence("documentInfo_1.id",  "ASC", "LAST");

        PaginatedCriteriaBuilder<Tuple> pcb = crit.page(0, 1);

        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());

        PagedList<Tuple> result = pcb.getResultList();
        assertEquals(1, result.size());
        assertEquals(2, result.getTotalSize());
        assertEquals("doc2", result.get(0).get(0, String.class));
        assertEquals("Karl1", result.get(0).get(1, String.class));

        result = crit.page(1, 1).getResultList();
        assertEquals("doc1", result.get(0).get(0, String.class));
        assertEquals("Karl2", result.get(0).get(1, String.class));
    }

    @Test
    public void paginateInverseNonOptional() {
        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class)
                .from(DocumentForOneToOne.class, "d")
                .select("d.name")
                .select("d.documentInfo2.someInfo")
                .orderByAsc("d.documentInfo2.id");

        String expectedCountQuery = "SELECT " + countPaginated("d.id", false) + " FROM DocumentForOneToOne d";

        String expectedObjectQuery = "SELECT d.name, documentInfo2_1.someInfo FROM DocumentForOneToOne d JOIN d.documentInfo2 documentInfo2_1"
                + " ORDER BY documentInfo2_1.id ASC";

        PaginatedCriteriaBuilder<Tuple> pcb = crit.page(0, 1);

        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());

        PagedList<Tuple> result = pcb.getResultList();
        assertEquals(1, result.size());
        assertEquals(2, result.getTotalSize());
        assertEquals("doc2", result.get(0).get(0, String.class));
        assertEquals("Karl1", result.get(0).get(1, String.class));

        result = crit.page(1, 1).getResultList();
        assertEquals("doc1", result.get(0).get(0, String.class));
        assertEquals("Karl2", result.get(0).get(1, String.class));
    }

    @Test
    public void paginateInverseExplicit() {
        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class)
                .from(DocumentForOneToOne.class, "d")
                .select("d.name")
                .select("d.documentInfo.someInfo")
                .orderByAsc("d.documentInfo.id");

        String expectedCountQuery = "SELECT " + countPaginated("documentInfo_1.id", false) + " FROM DocumentForOneToOne d";

        String expectedObjectQuery = "SELECT d.name, documentInfo_1.someInfo FROM DocumentForOneToOne d LEFT JOIN d.documentInfo documentInfo_1"
                + " ORDER BY " + renderNullPrecedence("documentInfo_1.id",  "ASC", "LAST");

        PaginatedCriteriaBuilder<Tuple> pcb = crit.pageBy(0, 1, "d.documentInfo.id");

        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());

        PagedList<Tuple> result = pcb.getResultList();
        assertEquals(1, result.size());
        assertEquals(2, result.getTotalSize());
        assertEquals("doc2", result.get(0).get(0, String.class));
        assertEquals("Karl1", result.get(0).get(1, String.class));

        result = crit.pageBy(1, 1, "d.documentInfo.id").getResultList();
        assertEquals("doc1", result.get(0).get(0, String.class));
        assertEquals("Karl2", result.get(0).get(1, String.class));
    }

    @Test
    public void paginateInverseNonOptionalExplicit() {
        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class)
                .from(DocumentForOneToOne.class, "d")
                .select("d.name")
                .select("d.documentInfo2.someInfo")
                .orderByAsc("d.documentInfo2.id");

        String expectedCountQuery = "SELECT " + countPaginated("documentInfo2_1.id", false) + " FROM DocumentForOneToOne d";

        String expectedObjectQuery = "SELECT d.name, documentInfo2_1.someInfo FROM DocumentForOneToOne d JOIN d.documentInfo2 documentInfo2_1"
                + " ORDER BY documentInfo2_1.id ASC";

        PaginatedCriteriaBuilder<Tuple> pcb = crit.pageBy(0, 1, "d.documentInfo2.id");

        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());

        PagedList<Tuple> result = pcb.getResultList();
        assertEquals(1, result.size());
        assertEquals(2, result.getTotalSize());
        assertEquals("doc2", result.get(0).get(0, String.class));
        assertEquals("Karl1", result.get(0).get(1, String.class));

        result = crit.pageBy(1, 1, "d.documentInfo2.id").getResultList();
        assertEquals("doc1", result.get(0).get(0, String.class));
        assertEquals("Karl2", result.get(0).get(1, String.class));
    }

    @Test
    public void paginateOwned() {
        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class)
                .from(DocumentInfo.class, "d")
                .select("d.someInfo")
                .select("d.document.name")
                .orderByAsc("d.document.id");

        String expectedCountQuery = "SELECT " + countPaginated("d.id", false) + " FROM DocumentInfo d";

        String expectedObjectQuery = "SELECT d.someInfo, document_1.name FROM DocumentInfo d LEFT JOIN d.document document_1"
                + " ORDER BY " + renderNullPrecedence(singleValuedAssociationIdPath("d.document.id", "document_1"),  "ASC", "LAST");

        PaginatedCriteriaBuilder<Tuple> pcb = crit.page(0, 1);

        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());

        PagedList<Tuple> result = pcb.getResultList();
        assertEquals(1, result.size());
        assertEquals(2, result.getTotalSize());
        assertEquals("Karl2", result.get(0).get(0, String.class));
        assertEquals("doc1", result.get(0).get(1, String.class));

        result = crit.page(1, 1).getResultList();
        assertEquals("Karl1", result.get(0).get(0, String.class));
        assertEquals("doc2", result.get(0).get(1, String.class));
    }

    @Test
    public void paginateOwnedNonOptional() {
        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class)
                .from(DocumentInfo.class, "d")
                .select("d.someInfo")
                .select("d.document2.name")
                .orderByAsc("d.document2.id");

        String expectedCountQuery = "SELECT " + countPaginated("d.id", false) + " FROM DocumentInfo d";

        String expectedObjectQuery = "SELECT d.someInfo, document2_1.name FROM DocumentInfo d JOIN d.document2 document2_1"
                + " ORDER BY " + singleValuedAssociationIdPath("d.document2.id", "document2_1") + " ASC";

        PaginatedCriteriaBuilder<Tuple> pcb = crit.page(0, 1);

        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());

        PagedList<Tuple> result = pcb.getResultList();
        assertEquals(1, result.size());
        assertEquals(2, result.getTotalSize());
        assertEquals("Karl2", result.get(0).get(0, String.class));
        assertEquals("doc1", result.get(0).get(1, String.class));

        result = crit.page(1, 1).getResultList();
        assertEquals("Karl1", result.get(0).get(0, String.class));
        assertEquals("doc2", result.get(0).get(1, String.class));
    }

    @Test
    public void paginateOwnedExplicit() {
        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class)
                .from(DocumentInfo.class, "d")
                .select("d.someInfo")
                .select("d.document.name")
                .orderByAsc("d.document.id");

        String expectedCountQuery = "SELECT " + countPaginated("d.document.id", false) + " FROM DocumentInfo d";

        String expectedObjectQuery = "SELECT d.someInfo, document_1.name FROM DocumentInfo d LEFT JOIN d.document document_1"
                + " ORDER BY " + renderNullPrecedence(singleValuedAssociationIdPath("d.document.id", "document_1"),  "ASC", "LAST");

        PaginatedCriteriaBuilder<Tuple> pcb = crit.pageBy(0, 1, "d.document.id");

        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());

        PagedList<Tuple> result = pcb.getResultList();
        assertEquals(1, result.size());
        assertEquals(2, result.getTotalSize());
        assertEquals("Karl2", result.get(0).get(0, String.class));
        assertEquals("doc1", result.get(0).get(1, String.class));

        result = crit.pageBy(1, 1, "d.document.id").getResultList();
        assertEquals("Karl1", result.get(0).get(0, String.class));
        assertEquals("doc2", result.get(0).get(1, String.class));
    }

    @Test
    public void paginateOwnedNonOptionalExplicit() {
        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class)
                .from(DocumentInfo.class, "d")
                .select("d.someInfo")
                .select("d.document2.name")
                .orderByAsc("d.document2.id");

        String expectedCountQuery = "SELECT " + countPaginated("d.document2.id", false) + " FROM DocumentInfo d";

        String expectedObjectQuery = "SELECT d.someInfo, document2_1.name FROM DocumentInfo d JOIN d.document2 document2_1"
                + " ORDER BY " + singleValuedAssociationIdPath("d.document2.id", "document2_1") + " ASC";

        PaginatedCriteriaBuilder<Tuple> pcb = crit.pageBy(0, 1, "d.document2.id");

        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());

        PagedList<Tuple> result = pcb.getResultList();
        assertEquals(1, result.size());
        assertEquals(2, result.getTotalSize());
        assertEquals("Karl2", result.get(0).get(0, String.class));
        assertEquals("doc1", result.get(0).get(1, String.class));

        result = crit.pageBy(1, 1, "d.document2.id").getResultList();
        assertEquals("Karl1", result.get(0).get(0, String.class));
        assertEquals("doc2", result.get(0).get(1, String.class));
    }
}
