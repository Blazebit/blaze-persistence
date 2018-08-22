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
import com.blazebit.persistence.testsuite.base.jpa.category.NoMSSQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@Category({ NoOracle.class, NoMSSQL.class })
public class OptimizedKeysetPaginationRowValueConstructorTest extends AbstractCoreTest {

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Document doc1 = new Document("doc1");
                Document doc2 = new Document("doc2");
                Document doc3 = new Document("doc3");
                Document doc4 = new Document("doc4");
                Document doc5 = new Document("doc5");
                Document doc6 = new Document("doc6");

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
    protected CriteriaBuilderConfiguration configure(CriteriaBuilderConfiguration config) {
        config = super.configure(config);
        config.setProperty(ConfigurationProperties.OPTIMIZED_KEYSET_PREDICATE_RENDERING, "true");
        return config;
    }

    @Test
    public void simpleNormalTest() {
        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class).from(Document.class, "d")
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
        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class).from(Document.class, "d")
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
        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class).from(Document.class, "d")
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
        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("d.name").select("d.owner.name").select("CASE WHEN d.age = 18 THEN true ELSE false END", "underaged");
        crit.orderByDesc("d.owner.name")
                .orderByDesc("d.name")
                .orderByAsc("underaged")
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
        assertEquals(2, result.size());
        assertEquals("doc4", result.get(0).get(0));
        assertEquals("doc3", result.get(1).get(0));

        // scroll forward
        pcb = crit.page(result.getKeysetPage(), 4, 2);
        assertEquals(
                "SELECT d.name, owner_1.name, CASE WHEN d.age = 18 THEN true ELSE false END AS underaged, d.id FROM Document d JOIN d.owner owner_1 "
                        + "WHERE " + function("compare_row_value", "'<'", "CASE WHEN (1=NULLIF(1,1) AND 1=NULLIF(1,1)) THEN 1 ELSE 0 END,CASE WHEN (1=NULLIF(1,1) AND 1=NULLIF(1,1)) THEN 1 ELSE 0 END,CASE WHEN (1=NULLIF(1,1) AND :_keysetParameter_2=CASE WHEN d.age = 18 THEN true ELSE false END) THEN 1 ELSE 0 END,CASE WHEN (1=NULLIF(1,1) AND :_keysetParameter_3=d.id) THEN 1 ELSE 0 END,CASE WHEN (1=NULLIF(1,1) AND owner_1.name=:_keysetParameter_0) THEN 1 ELSE 0 END,CASE WHEN (1=NULLIF(1,1) AND d.name=:_keysetParameter_1) THEN 1 ELSE 0 END") + " = true"
                        + " ORDER BY owner_1.name DESC, d.name DESC, underaged ASC, d.id ASC",
                pcb.getQueryString()
        );

        result = pcb.getResultList();
        assertEquals(2, result.getSize());
        assertEquals("doc2", result.get(0).get(0));
        assertEquals("doc1", result.get(1).get(0));

        // scroll backwards
        pcb = crit.page(result.getKeysetPage(), 2, 2);
        assertEquals(
                "SELECT d.name, owner_1.name, CASE WHEN d.age = 18 THEN true ELSE false END AS underaged, d.id FROM Document d JOIN d.owner owner_1 "
                        + "WHERE " + function("compare_row_value", "'<'", "CASE WHEN (1=NULLIF(1,1) AND :_keysetParameter_0=owner_1.name) THEN 1 ELSE 0 END,CASE WHEN (1=NULLIF(1,1) AND :_keysetParameter_1=d.name) THEN 1 ELSE 0 END,CASE WHEN (1=NULLIF(1,1) AND 1=NULLIF(1,1)) THEN 1 ELSE 0 END,CASE WHEN (1=NULLIF(1,1) AND 1=NULLIF(1,1)) THEN 1 ELSE 0 END,CASE WHEN (1=NULLIF(1,1) AND CASE WHEN d.age = 18 THEN true ELSE false END=:_keysetParameter_2) THEN 1 ELSE 0 END,CASE WHEN (1=NULLIF(1,1) AND d.id=:_keysetParameter_3) THEN 1 ELSE 0 END") + " = true"
                        + " ORDER BY owner_1.name ASC, d.name ASC, underaged DESC, d.id DESC",
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
        Document reference = cbf.create(em, Document.class).where("name").eq("doc5").getSingleResult();
        String expectedCountQuery =
                "SELECT " + countPaginated("d.id", false) + ", "
                        + function("PAGE_POSITION",
                        "(SELECT _page_position_d.id "
                                + "FROM Document _page_position_d "
                                + "JOIN _page_position_d.owner _page_position_owner_1 "
                                + "GROUP BY " + groupBy("_page_position_d.name", "_page_position_owner_1.name", "_page_position_d.id")
                                + " ORDER BY _page_position_owner_1.name DESC, _page_position_d.name ASC, _page_position_d.id ASC)",
                        ":_entityPagePositionParameter"
                )
                        + " FROM Document d";

        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class).from(Document.class, "d")
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
        Document reference = cbf.create(em, Document.class).where("name").eq("doc4").getSingleResult();
        String expectedCountQuery =
                "SELECT " + countPaginated("d.id", false) + ", "
                        + function("PAGE_POSITION",
                        "(SELECT _page_position_d.id "
                                + "FROM Document _page_position_d "
                                + "JOIN _page_position_d.owner _page_position_owner_1 "
                                + "WHERE _page_position_d.name <> :param_0 "
                                + "GROUP BY " + groupBy("_page_position_d.name", "_page_position_owner_1.name", "_page_position_d.id")
                                + " ORDER BY _page_position_owner_1.name DESC, _page_position_d.name ASC, _page_position_d.id ASC)",
                        ":_entityPagePositionParameter"
                )
                        + " FROM Document d "
                        + "WHERE d.name <> :param_0";

        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("d.name").select("d.owner.name")
                .where("d.name").notEq("doc4")
                .orderByDesc("d.owner.name")
                .orderByAsc("d.name")
                .orderByAsc("d.id");
        PaginatedCriteriaBuilder<Tuple> firstPageCb = cbf.create(em, Tuple.class).from(Document.class, "d")
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
        // The first time we have to use the offset
        String expectedObjectQuery = "SELECT d.name, owner_1.name, d.id FROM Document d JOIN d.owner owner_1"
                + " ORDER BY owner_1.name DESC, d.name ASC, d.id ASC";
        assertEquals(expectedObjectQuery, pcb.getQueryString());

        assertEquals(1, result.size());
        assertEquals(6, result.getTotalSize());
        assertEquals("doc5", result.get(0).get(0));

        pcb = crit.page(result.getKeysetPage(), 2, 1);
        result = pcb.getResultList();
        // Finally we can use the key set
        expectedObjectQuery = "SELECT d.name, owner_1.name, d.id FROM Document d JOIN d.owner owner_1 "
                + "WHERE " + function("compare_row_value", "'<'", "CASE WHEN (1=NULLIF(1,1) AND 1=NULLIF(1,1)) THEN 1 ELSE 0 END,CASE WHEN (1=NULLIF(1,1) AND :_keysetParameter_1=d.name) THEN 1 ELSE 0 END,CASE WHEN (1=NULLIF(1,1) AND :_keysetParameter_2=d.id) THEN 1 ELSE 0 END,CASE WHEN (1=NULLIF(1,1) AND owner_1.name=:_keysetParameter_0) THEN 1 ELSE 0 END") + " = true"
                + " ORDER BY owner_1.name DESC, d.name ASC, d.id ASC";
        assertEquals(expectedObjectQuery, pcb.getQueryString());

        pcb = crit.page(result.getKeysetPage(), 2, 1);
        result = pcb.getResultList();
        // Same page again key set
        expectedObjectQuery = "SELECT d.name, owner_1.name, d.id FROM Document d JOIN d.owner owner_1 "
                + "WHERE " + function("compare_row_value", "'<='", "CASE WHEN (1=NULLIF(1,1) AND 1=NULLIF(1,1)) THEN 1 ELSE 0 END,CASE WHEN (1=NULLIF(1,1) AND :_keysetParameter_1=d.name) THEN 1 ELSE 0 END,CASE WHEN (1=NULLIF(1,1) AND :_keysetParameter_2=d.id) THEN 1 ELSE 0 END,CASE WHEN (1=NULLIF(1,1) AND owner_1.name=:_keysetParameter_0) THEN 1 ELSE 0 END") + " = true"
                + " ORDER BY owner_1.name DESC, d.name ASC, d.id ASC";
        assertEquals(expectedObjectQuery, pcb.getQueryString());

        assertEquals(1, result.size());
        assertEquals(6, result.getTotalSize());
        assertEquals("doc6", result.get(0).get(0));

        pcb = crit.page(result.getKeysetPage(), 0, 2);
        result = pcb.getResultList();
        // Now we scroll back with increased page size
        expectedObjectQuery = "SELECT d.name, owner_1.name, d.id FROM Document d JOIN d.owner owner_1"
                + " ORDER BY owner_1.name DESC, d.name ASC, d.id ASC";
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
                .from(Document.class, "d")
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
