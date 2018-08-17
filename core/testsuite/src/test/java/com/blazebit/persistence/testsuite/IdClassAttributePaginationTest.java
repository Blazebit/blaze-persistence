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

import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.IdClassEntity;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;

import static com.googlecode.catchexception.CatchException.verifyException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class IdClassAttributePaginationTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
                IdClassEntity.class
        };
    }

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                IdClassEntity e1 = new IdClassEntity(1, "1", 1);
                em.persist(e1);
            }
        });
    }

    @Test
    public void testPaginateIdClassAttribute() {
        String expectedCountQuery = "SELECT " + countPaginated("d.key1, d.key2", false) + " FROM IdClassEntity d";
        String expectedObjectQuery = "SELECT d.value FROM IdClassEntity d"
                + " ORDER BY d.value ASC, d.key1 ASC, d.key2 ASC";
        PaginatedCriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class)
                .from(IdClassEntity.class, "d")
                .select("d.value")
                .orderByAsc("value").orderByAsc("key1").orderByAsc("key2")
                .page(0, 1);
        assertEquals(expectedCountQuery, cb.getPageCountQueryString());
        assertNull(cb.getPageIdQueryString());
        assertEquals(expectedObjectQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testPaginateIdClassAttributeWithIdQuery() {
        String expectedCountQuery = "SELECT " + countPaginated("d.key1, d.key2", false) + " FROM IdClassEntity d";
        String expectedIdQuery = "SELECT d.key1, d.key2 FROM IdClassEntity d GROUP BY d.value, d.key1, d.key2 ORDER BY d.value ASC, d.key1 ASC, d.key2 ASC";
        String expectedObjectQuery = "SELECT d.value, children_1.value FROM IdClassEntity d " +
                "LEFT JOIN d.children children_1"
                + " WHERE (d.key1 = :ids_0_0 AND d.key2 = :ids_1_0) ORDER BY d.value ASC, d.key1 ASC, d.key2 ASC";
        PaginatedCriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class)
                .from(IdClassEntity.class, "d")
                .select("d.value").select("children.value")
                .orderByAsc("value").orderByAsc("key1").orderByAsc("key2")
                .page(0, 1);
        assertEquals(expectedCountQuery, cb.getPageCountQueryString());
        assertEquals(expectedIdQuery, cb.getPageIdQueryString());
        assertEquals(expectedObjectQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testPaginateNonUniqueIdClassAttributePart() {
        PaginatedCriteriaBuilder<IdClassEntity> cb = cbf.create(em, IdClassEntity.class, "d")
                .orderByAsc("value").orderByAsc("key1")
                .page(0, 1);
        verifyException(cb, IllegalStateException.class).getQueryString();
    }
}
