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
import com.blazebit.persistence.KeysetPage;
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.impl.ConfigurationProperties;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.DocumentWithNullableName;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.testsuite.entity.Workflow;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class OptimizedKeysetPaginationTest extends AbstractCoreTest {

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                DocumentWithNullableName doc1 = new DocumentWithNullableName("doc1");
                DocumentWithNullableName doc2 = new DocumentWithNullableName("doc2");
                DocumentWithNullableName doc3 = new DocumentWithNullableName("doc3");
                DocumentWithNullableName doc4 = new DocumentWithNullableName("doc4");
                DocumentWithNullableName doc5 = new DocumentWithNullableName("doc5");
                DocumentWithNullableName doc6 = new DocumentWithNullableName("doc6");

                Person o1 = new Person("Karl1");
                Person o2 = new Person("Karl2");
                Person o3 = new Person("Karl3");
                Person o4 = new Person("Karl4");

                doc1.setOwner(o1);
                doc2.setOwner(o2);
                doc3.setOwner(o3);
                doc4.setOwner(o4);
                doc5.setOwner(o4);
                doc6.setOwner(o4);

                em.persist(o1);
                em.persist(o2);
                em.persist(o3);
                em.persist(o4);

                em.persist(doc1);
                em.persist(doc2);
                em.persist(doc3);
                em.persist(doc4);
                em.persist(doc5);
                em.persist(doc6);
            }
        });
    }

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
                Document.class,
                Version.class,
                Person.class,
                Workflow.class,
                IntIdEntity.class,
                DocumentWithNullableName.class
        };
    }

    @Override
    protected CriteriaBuilderConfiguration configure(CriteriaBuilderConfiguration config) {
        config = super.configure(config);
        config.setProperty(ConfigurationProperties.OPTIMIZED_KEYSET_PREDICATE_RENDERING, "true");
        return config;
    }

    @Test
    public void simpleNormalTest() {
        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class).from(DocumentWithNullableName.class, "d")
                .select("d.name").select("d.owner.name");
        crit.orderByDesc("d.owner.name")
                .orderByAsc("d.name")
                .orderByAsc("d.id");

        PaginatedCriteriaBuilder<Tuple> pcb = crit.page(null, 1, 1);
        PagedList<Tuple> result = pcb.getResultList();
        simpleTest(crit, pcb, result);
    }

    @Test
    public void backwardsPaginationResultSetOrder() {
        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class).from(DocumentWithNullableName.class, "d")
                .select("d.name").select("d.owner.name");
        crit.orderByDesc("d.owner.name")
                .orderByDesc("d.name")
                .orderByAsc("d.id");

        PaginatedCriteriaBuilder<Tuple> pcb = crit.page(null, 2, 1);
        PagedList<Tuple> result = pcb.getResultList();

        // scroll backwards
        result = crit.page(result.getKeysetPage(), 0, 2).getResultList();

        assertEquals(2, result.getSize());
        assertEquals("doc6", result.get(0).get(0));
        assertEquals("doc5", result.get(1).get(0));
    }

    @Test
    public void backwardsPaginationWithCollectionResultSetOrder() {
        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class).from(DocumentWithNullableName.class, "d")
                .select("d.name").select("d.owner.name").select("d.people");
        crit.orderByDesc("d.owner.name")
                .orderByDesc("d.name")
                .orderByAsc("d.id");

        PaginatedCriteriaBuilder<Tuple> pcb = crit.page(null, 2, 1);
        PagedList<Tuple> result = pcb.getResultList();

        // scroll backwards
        result = crit.page(result.getKeysetPage(), 0, 2).getResultList();

        assertEquals(2, result.getSize());
        assertEquals("doc6", result.get(0).get(0));
        assertEquals("doc5", result.get(1).get(0));
    }

    @Test
    public void forwardBackwardsPaginationResultSetOrder() {
        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class).from(DocumentWithNullableName.class, "d")
                .select("d.name").select("d.owner.name");
        crit.orderByDesc("d.owner.name")
                .orderByDesc("d.name")
                .orderByAsc("d.id");
        /* query yields the following order:
         *  - doc6
         *  - doc5
         *  - doc4
         *  - doc3
         *  - doc2
         *  - doc1
         */

        PaginatedCriteriaBuilder<Tuple> pcb = crit.page(null, 2, 2);
        PagedList<Tuple> result = pcb.getResultList();

        // scroll forward
        pcb = crit.page(result.getKeysetPage(), 4, 2);
        assertEquals(
                "SELECT d.name, owner_1.name, d.id FROM DocumentWithNullableName d JOIN d.owner owner_1 "
                        + "WHERE owner_1.name <= :_keysetParameter_0 AND NOT (owner_1.name = :_keysetParameter_0 AND (d.name IS NOT NULL AND d.name > :_keysetParameter_1 OR (d.name IS NOT NULL AND d.name = :_keysetParameter_1 AND d.id <= :_keysetParameter_2)))"
                        + " ORDER BY owner_1.name DESC, " + renderNullPrecedence("d.name", "DESC", "LAST") + ", d.id ASC",
                pcb.getQueryString()
        );
        result = pcb.getResultList();
        assertEquals(2, result.getSize());
        assertEquals("doc2", result.get(0).get(0));
        assertEquals("doc1", result.get(1).get(0));

        // scroll backwards
        pcb = crit.page(result.getKeysetPage(), 2, 2);
        assertEquals(
                "SELECT d.name, owner_1.name, d.id FROM DocumentWithNullableName d JOIN d.owner owner_1 "
                        + "WHERE owner_1.name >= :_keysetParameter_0 AND NOT (owner_1.name = :_keysetParameter_0 AND (d.name < :_keysetParameter_1 OR (d.name = :_keysetParameter_1 AND d.id >= :_keysetParameter_2)))"
                        + " ORDER BY owner_1.name ASC, " + renderNullPrecedence("d.name", "ASC", "FIRST") + ", d.id DESC",
                pcb.getQueryString()
        );
        result = pcb.getResultList();
        assertEquals(2, result.getSize());
        assertEquals("doc4", result.get(0).get(0));
        assertEquals("doc3", result.get(1).get(0));

        // scroll forward
        result = crit.page(result.getKeysetPage(), 4, 2).getResultList();

        assertEquals(2, result.getSize());
        assertEquals("doc2", result.get(0).get(0));
        assertEquals("doc1", result.get(1).get(0));
    }

    @Test
    @Category(NoEclipselink.class)
    // TODO: report eclipselink does not support subqueries in functions
    public void testWithReferenceObject() {
        DocumentWithNullableName reference = cbf.create(em, DocumentWithNullableName.class).where("name").eq("doc5").getSingleResult();
        String expectedCountQuery =
                "SELECT " + countPaginated("d.id", false) + ", "
                        + function("PAGE_POSITION",
                        "(SELECT _page_position_d.id "
                                + "FROM DocumentWithNullableName _page_position_d "
                                + "JOIN _page_position_d.owner _page_position_owner_1 "
                                + "GROUP BY " + groupBy(renderNullPrecedenceGroupBy("_page_position_d.name"), "_page_position_owner_1.name", "_page_position_d.id")
                                + " ORDER BY _page_position_owner_1.name DESC, " + renderNullPrecedence("_page_position_d.name", "ASC", "LAST") + ", _page_position_d.id ASC)",
                        ":_entityPagePositionParameter"
                )
                        + " FROM DocumentWithNullableName d";

        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class).from(DocumentWithNullableName.class, "d")
                .select("d.name").select("d.owner.name")
                .orderByDesc("d.owner.name")
                .orderByAsc("d.name")
                .orderByAsc("d.id");

        PaginatedCriteriaBuilder<Tuple> pcb = crit.page(reference.getId(), 1).withKeysetExtraction(true);

        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        PagedList<Tuple> list = pcb.getResultList();
        assertEquals(1, list.getFirstResult());
        assertEquals(2, list.getPage());
        assertEquals(6, list.getTotalPages());
        assertEquals(6, list.getTotalSize());
        assertEquals(1, list.size());
        simpleTest(crit, pcb, list);
    }

    @Test
    @Category(NoEclipselink.class)
    // TODO: report eclipselink does not support subqueries in functions
    public void testWithNotExistingReferenceObject() {
        DocumentWithNullableName reference = cbf.create(em, DocumentWithNullableName.class).where("name").eq("doc4").getSingleResult();
        String expectedCountQuery =
                "SELECT " + countPaginated("d.id", false) + ", "
                        + function("PAGE_POSITION",
                        "(SELECT _page_position_d.id "
                                + "FROM DocumentWithNullableName _page_position_d "
                                + "JOIN _page_position_d.owner _page_position_owner_1 "
                                + "WHERE _page_position_d.name <> :param_0 "
                                + "GROUP BY " + groupBy(renderNullPrecedenceGroupBy("_page_position_d.name"), "_page_position_owner_1.name", "_page_position_d.id")
                                + " ORDER BY _page_position_owner_1.name DESC, " + renderNullPrecedence("_page_position_d.name", "ASC", "LAST") + ", _page_position_d.id ASC)",
                        ":_entityPagePositionParameter"
                )
                        + " FROM DocumentWithNullableName d "
                        + "WHERE d.name <> :param_0";

        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class).from(DocumentWithNullableName.class, "d")
                .select("d.name").select("d.owner.name")
                .where("d.name").notEq("doc4")
                .orderByDesc("d.owner.name")
                .orderByAsc("d.name")
                .orderByAsc("d.id");
        PaginatedCriteriaBuilder<Tuple> firstPageCb = cbf.create(em, Tuple.class).from(DocumentWithNullableName.class, "d")
                .select("d.name").select("d.owner.name")
                .where("d.name").notEq("doc4")
                .orderByDesc("d.owner.name")
                .orderByAsc("d.name")
                .orderByAsc("d.id")
                .page(null, 0, 1);

        PaginatedCriteriaBuilder<Tuple> pcb = crit.page(reference.getId(), 1).withKeysetExtraction(true);

        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        PagedList<Tuple> expectedList = firstPageCb.getResultList();
        PagedList<Tuple> list = pcb.getResultList();
        assertEquals(expectedList, list);

        assertEquals(-1, list.getFirstResult());
        assertEquals(1, list.getPage());
        assertEquals(5, list.getTotalPages());
        assertEquals(5, list.getTotalSize());
        assertEquals(1, list.size());
    }

    public void simpleTest(CriteriaBuilder<Tuple> crit, PaginatedCriteriaBuilder<Tuple> pcb, PagedList<Tuple> result) {
        /* query yields the following order:
         *  - doc4
         *  - doc5
         *  - doc6
         *  - doc3
         *  - doc2
         *  - doc1
         */

        // The first time we have to use the offset
        String expectedObjectQuery = "SELECT d.name, owner_1.name, d.id FROM DocumentWithNullableName d JOIN d.owner owner_1"
                + " ORDER BY owner_1.name DESC, " + renderNullPrecedence("d.name", "ASC", "LAST") + ", d.id ASC";
        assertNull(pcb.getPageIdQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());

        assertEquals(1, result.size());
        assertEquals(6, result.getTotalSize());
        assertEquals("doc5", result.get(0).get(0));

        pcb = crit.page(result.getKeysetPage(), 2, 1);
        result = pcb.getResultList();
        // Finally we can use the key set
        expectedObjectQuery = "SELECT d.name, owner_1.name, d.id FROM DocumentWithNullableName d JOIN d.owner owner_1"
                + " WHERE owner_1.name <= :_keysetParameter_0 AND NOT (owner_1.name = :_keysetParameter_0 AND (d.name IS NOT NULL AND d.name < :_keysetParameter_1 OR (d.name IS NOT NULL AND d.name = :_keysetParameter_1 AND d.id <= :_keysetParameter_2)))"
                + " ORDER BY owner_1.name DESC, " + renderNullPrecedence("d.name", "ASC", "LAST") + ", d.id ASC";
        assertNull(pcb.getPageIdQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());

        pcb = crit.page(result.getKeysetPage(), 2, 1);
        result = pcb.getResultList();
        // Same page again key set
        expectedObjectQuery = "SELECT d.name, owner_1.name, d.id FROM DocumentWithNullableName d JOIN d.owner owner_1"
                + " WHERE owner_1.name <= :_keysetParameter_0 AND NOT (owner_1.name = :_keysetParameter_0 AND (d.name < :_keysetParameter_1 OR (d.name IS NOT NULL AND d.name = :_keysetParameter_1 AND d.id < :_keysetParameter_2)))"
                + " ORDER BY owner_1.name DESC, " + renderNullPrecedence("d.name", "ASC", "LAST") + ", d.id ASC";
        assertNull(pcb.getPageIdQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());

        assertEquals(1, result.size());
        assertEquals(6, result.getTotalSize());
        assertEquals("doc6", result.get(0).get(0));

        pcb = crit.page(result.getKeysetPage(), 0, 2);
        result = pcb.getResultList();
        // Now we scroll back with increased page size
        expectedObjectQuery = "SELECT d.name, owner_1.name, d.id FROM DocumentWithNullableName d JOIN d.owner owner_1"
                + " ORDER BY owner_1.name DESC, " + renderNullPrecedence("d.name", "ASC", "LAST") + ", d.id ASC";
        assertNull(pcb.getPageIdQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());

        assertEquals(2, result.size());
        assertEquals(6, result.getTotalSize());
        assertEquals("doc4", result.get(0).get(0));
        assertEquals("doc5", result.get(1).get(0));
    }

    @Test
    public void keysetPaginationWithSimpleObjectQueryTest() {
        KeysetPage keyset = null;
        PaginatedCriteriaBuilder<String> crit = cbf.create(em, String.class)
                .from(DocumentWithNullableName.class, "d")
                .orderByAsc("d.id")
                .selectNew(new ObjectBuilder<String>() {

                    @Override
                    public <X extends SelectBuilder<X>> void applySelects(X selectBuilder) {
                        selectBuilder
                                .select("d.name")
                                .select("d.owner.name");
                    }

                    @Override
                    public String build(Object[] tuple) {
                        return tuple[0] + " - " + tuple[1];
                    }

                    @Override
                    public List<String> buildList(List<String> list) {
                        return list;
                    }
                })
                .page(keyset, 0, 1);
        PagedList<String> result = crit.getResultList();
        assertEquals(1, result.size());
        assertEquals("doc1 - Karl1", result.get(0));

        keyset = result.getKeysetPage();
        crit = crit.page(keyset, 1, 1);
        result = crit.getResultList();
        assertEquals(1, result.size());
        assertEquals("doc2 - Karl2", result.get(0));
    }
}
