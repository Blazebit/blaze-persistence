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

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.persistence.EntityTransaction;
import javax.persistence.Tuple;

import org.junit.Before;
import org.junit.Test;

import com.blazebit.persistence.entity.Document;
import com.blazebit.persistence.entity.Person;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class KeysetPaginationTest extends AbstractCoreTest {

    @Before
    public void setUp() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Document doc1 = new Document("doc1");
            Document doc2 = new Document("doc2");
            Document doc3 = new Document("doc3");

            Person o1 = new Person("Karl1");
            Person o2 = new Person("Karl2");
            Person o3 = new Person("Karl3");

            doc1.setOwner(o1);
            doc2.setOwner(o2);
            doc3.setOwner(o3);

            em.persist(o1);
            em.persist(o2);
            em.persist(o3);

            em.persist(doc1);
            em.persist(doc2);
            em.persist(doc3);

            em.flush();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        }
    }

    @Test
    public void simpleNormalTest() {
        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class).from(Document.class, "d")
            .select("d.name").select("d.owner.name");
        crit.orderByDesc("d.owner.name")
            .orderByAsc("d.name")
            .orderByAsc("d.id");
        
        PaginatedCriteriaBuilder<Tuple> pcb = crit.page(null, 0, 1);
        PagedList<Tuple> result = pcb.getResultList();
        simpleTest(crit, pcb, result);
    }

    @Test
    public void testWithReferenceObject() {
        Document reference = cbf.create(em, Document.class).where("name").eq("doc3").getSingleResult();
        String expectedCountQuery =
                "SELECT COUNT(DISTINCT d.id), "
                + function("PAGE_POSITION",
                        "(SELECT _page_position_d.id "
                        + "FROM Document _page_position_d "
                        + "JOIN _page_position_d.owner _page_position_owner_1 "
                        + "GROUP BY _page_position_d.id, _page_position_owner_1.name, _page_position_d.name "
                        + "ORDER BY " + renderNullPrecedence("_page_position_owner_1.name", "DESC", "LAST") + ", " + renderNullPrecedence("_page_position_d.name", "ASC", "LAST") + ", " + renderNullPrecedence("_page_position_d.id", "ASC", "LAST") + ")", 
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
        assertEquals(0, list.getFirstResult());
        assertEquals(1, list.getPage());
        assertEquals(3, list.getTotalPages());
        assertEquals(3, list.getTotalSize());
        assertEquals(1, list.size());
        simpleTest(crit, pcb, list);
    }

    @Test
    public void testWithNotExistingReferenceObject() {
        Document reference = cbf.create(em, Document.class).where("name").eq("doc3").getSingleResult();
        String expectedCountQuery =
                "SELECT COUNT(DISTINCT d.id), "
                        + function("PAGE_POSITION",
                        "(SELECT _page_position_d.id "
                        + "FROM Document _page_position_d "
                        + "JOIN _page_position_d.owner _page_position_owner_1 "
                        + "WHERE _page_position_d.name <> :param_0 "
                        + "GROUP BY _page_position_d.id, _page_position_owner_1.name, _page_position_d.name "
                        + "ORDER BY " + renderNullPrecedence("_page_position_owner_1.name", "DESC", "LAST") + ", " + renderNullPrecedence("_page_position_d.name", "ASC", "LAST") + ", " + renderNullPrecedence("_page_position_d.id", "ASC", "LAST") + ")", 
                        ":_entityPagePositionParameter"
                )
                + " FROM Document d "
                + "WHERE d.name <> :param_0";
        
        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class).from(Document.class, "d")
            .select("d.name").select("d.owner.name")
            .where("d.name").notEq("doc3")
            .orderByDesc("d.owner.name")
            .orderByAsc("d.name")
            .orderByAsc("d.id");
        PaginatedCriteriaBuilder<Tuple> firstPageCb = cbf.create(em, Tuple.class).from(Document.class, "d")
            .select("d.name").select("d.owner.name")
            .where("d.name").notEq("doc3")
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
        assertEquals(2, list.getTotalPages());
        assertEquals(2, list.getTotalSize());
        assertEquals(1, list.size());
    }
    
    public void simpleTest(CriteriaBuilder<Tuple> crit, PaginatedCriteriaBuilder<Tuple> pcb, PagedList<Tuple> result) {
        // The first time we have to use the offset
        String expectedIdQuery = "SELECT d.id, owner_1.name, d.name, d.id FROM Document d JOIN d.owner owner_1 "
            + "GROUP BY d.id, owner_1.name, d.name "
            + "ORDER BY " + renderNullPrecedence("owner_1.name", "DESC", "LAST") + ", " + renderNullPrecedence("d.name", "ASC", "LAST") + ", " + renderNullPrecedence("d.id", "ASC", "LAST");
        assertEquals(expectedIdQuery, pcb.getPageIdQueryString());
        
        assertEquals(1, result.size());
        assertEquals(3, result.getTotalSize());
        assertEquals("doc3", result.get(0).get(0));
        
        pcb = crit.page(result.getKeysetPage(), 1, 1);
        result = pcb.getResultList();
        // Finally we can use the key set
        expectedIdQuery = "SELECT d.id, owner_1.name, d.name, d.id FROM Document d JOIN d.owner owner_1 "
            + "WHERE (owner_1.name < :_keysetParameter_0 OR (owner_1.name = :_keysetParameter_0 AND (d.name > :_keysetParameter_1 OR (d.name = :_keysetParameter_1 AND d.id > :_keysetParameter_2)))) "
            + "GROUP BY d.id, owner_1.name, d.name "
            + "ORDER BY " + renderNullPrecedence("owner_1.name", "DESC", "LAST") + ", " + renderNullPrecedence("d.name", "ASC", "LAST") + ", " + renderNullPrecedence("d.id", "ASC", "LAST");
        assertEquals(expectedIdQuery, pcb.getPageIdQueryString());
        
        pcb = crit.page(result.getKeysetPage(), 1, 1);
        result = pcb.getResultList();
        // Same page again key set
        expectedIdQuery = "SELECT d.id, owner_1.name, d.name, d.id FROM Document d JOIN d.owner owner_1 "
            + "WHERE (owner_1.name <= :_keysetParameter_0 OR (owner_1.name = :_keysetParameter_0 AND (d.name >= :_keysetParameter_1 OR (d.name = :_keysetParameter_1 AND d.id >= :_keysetParameter_2)))) "
            + "GROUP BY d.id, owner_1.name, d.name "
            + "ORDER BY " + renderNullPrecedence("owner_1.name", "DESC", "LAST") + ", " + renderNullPrecedence("d.name", "ASC", "LAST") + ", " + renderNullPrecedence("d.id", "ASC", "LAST");
        assertEquals(expectedIdQuery, pcb.getPageIdQueryString());
        
        assertEquals(1, result.size());
        assertEquals(3, result.getTotalSize());
        assertEquals("doc2", result.get(0).get(0));
        
        pcb = crit.page(result.getKeysetPage(), 0, 1);
        result = pcb.getResultList();
        // Now we scroll back
        expectedIdQuery = "SELECT d.id, owner_1.name, d.name, d.id FROM Document d JOIN d.owner owner_1 "
            + "WHERE (owner_1.name > :_keysetParameter_0 OR (owner_1.name = :_keysetParameter_0 AND (d.name < :_keysetParameter_1 OR (d.name = :_keysetParameter_1 AND d.id < :_keysetParameter_2)))) "
            + "GROUP BY d.id, owner_1.name, d.name "
            + "ORDER BY " + renderNullPrecedence("owner_1.name", "ASC", "FIRST") + ", " + renderNullPrecedence("d.name", "DESC", "FIRST") + ", " + renderNullPrecedence("d.id", "DESC", "FIRST");
        assertEquals(expectedIdQuery, pcb.getPageIdQueryString());
        
        assertEquals(1, result.size());
        assertEquals(3, result.getTotalSize());
        assertEquals("doc3", result.get(0).get(0));
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
