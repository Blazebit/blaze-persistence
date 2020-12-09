/*
 * Copyright 2014 - 2020 Blazebit.
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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;

import org.junit.Test;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.DeletePersonCTE;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.PersonCTE;
import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.testsuite.entity.Workflow;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;

/**
 * @author Ritesh A
 * @since 1.6.0
 */
public class Issue1228Test extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
            Document.class,
            Version.class,
            Person.class,
            Workflow.class,
            IntIdEntity.class,
            DeletePersonCTE.class,
            PersonCTE.class
        };
    }

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Person o1 = new Person("P1");
                em.persist(o1);
                em.flush();

                Person o2 = new Person("P2");
                em.persist(o2);
                em.flush();

                Document d1 = new Document("D1");
                d1.setOwner(o1);
                em.persist(d1);

                Document d2 = new Document("D2");
                d2.setOwner(o1);
                em.persist(d2);

                Document d3 = new Document("D3");
                d3.setOwner(o2);
                em.persist(d3);

                Document d4 = new Document("D4");
                d4.setOwner(o2);
                em.persist(d4);
            }
        });
    }

    // Test for issue #1228
    @Test
    public void testTupleSingleAttribute() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d");
        CriteriaBuilder<Tuple> t = cb.select("d.name");
        
        String expected = "SELECT d.name FROM Document d";
        assertEquals(expected, cb.getQueryString());
        
        List<Tuple> result = t.getResultList();
        assertEquals(4L, result.size());
        
        List<String> expectedResult = new ArrayList<>();
        expectedResult.add("D1");
        expectedResult.add("D2");
        expectedResult.add("D3");
        expectedResult.add("D4");
        
        List<String> actualResult = new ArrayList<>();
        result.forEach(tuple -> {
        	actualResult.add((String)tuple.get(0));
        });
        assertEquals(expectedResult, actualResult);
    }
}
