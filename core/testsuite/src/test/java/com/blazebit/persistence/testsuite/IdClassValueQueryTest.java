/*
 * Copyright 2014 - 2022 Blazebit.
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
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.entity.IdClassEntity;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Alex Ben Hassine
 * @since 1.3.0
 */
public class IdClassValueQueryTest extends AbstractCoreTest {

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
                IdClassEntity e1 = new IdClassEntity(1,"1",  1);
                IdClassEntity e2 = new IdClassEntity(2,"2",  2);
                IdClassEntity e3 = new IdClassEntity(3,"3",  3);
                IdClassEntity e4 = new IdClassEntity(4,"4",  4);
                IdClassEntity e5 = new IdClassEntity(5,"5",  5);

                em.persist(e1);
                em.persist(e2);
                em.persist(e3);
                em.persist(e4);
                em.persist(e5);
            }
        });
    }

    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testValueQuery() {
        IdClassEntity e1 = new IdClassEntity(1,"1",  1);
        IdClassEntity e2 = new IdClassEntity(2,"2",  2);
        IdClassEntity e3 = new IdClassEntity(3,"3",  3);
        IdClassEntity e4 = new IdClassEntity(4,"4",  4);
        IdClassEntity e5 = new IdClassEntity(5,"5",  5);

        List<IdClassEntity> entities = new ArrayList<>();
        entities.add(e1);
        entities.add(e2);
        entities.add(e3);
        entities.add(e4);
        entities.add(e5);

        int i = 0;
        CriteriaBuilder<Integer> cb =  cbf.create(em, Integer.class)
                .fromIdentifiableValues(IdClassEntity.class,"myValue", entities)
                .select("myValue.key1");
        TypedQuery<Integer> typedQuery = cb.getQuery();
        List<Integer> idClassEntities = typedQuery.getResultList();

        for (Integer a : idClassEntities) {
            i = i + a;
        }

        assertEquals(15, i);

    }

}