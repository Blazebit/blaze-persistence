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

import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoH2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQLOld;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.entity.DeletePersonCTE;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.PersonCTE;
import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.testsuite.entity.Workflow;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public class Issue1114Test extends AbstractCoreTest {

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

    // Test for issue #1114
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQLOld.class, NoH2.class })
    public void testLimitedCteWithoutLimitInMainQueryButWithCollectionFetch() {
        Function<Integer, List<Person>> fetchPersonBatch = batch ->
            cbf.create(em, Person.class)
                .with(PersonCTE.class, false)
                    .from(Person.class)
                    .bind("id").select("id")
                    .bind("name").select("NULL")
                    .bind("age").select("NULL")
                    .bind("idx").select("NULL")
                    .bind("owner").select("NULL")
                    .orderByAsc("name")
                    .setFirstResult(batch)
                    .setMaxResults(1)
                .end()
                .from(Person.class)
                .fetch("ownedDocuments")
                .where("id").in().from(PersonCTE.class).select("id").end()
                .getResultList();

        List<Person> personBatch1 = fetchPersonBatch.apply(0);
        List<Person> personBatch2 = fetchPersonBatch.apply(1);

        assertEquals(1, new HashSet<>(personBatch1).size());
        assertEquals(2, personBatch1.get(0).getOwnedDocuments().size());
        assertEquals(1, new HashSet<>(personBatch2).size());
        assertEquals(2, personBatch2.get(0).getOwnedDocuments().size());
    }
}
