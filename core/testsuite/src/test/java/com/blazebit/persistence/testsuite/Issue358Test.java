/*
 * Copyright 2014 - 2017 Blazebit.
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
import com.blazebit.persistence.impl.ConfigurationProperties;
import com.blazebit.persistence.testsuite.base.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.category.NoHibernate50;
import com.blazebit.persistence.testsuite.base.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.DocumentNodeCTE;
import com.blazebit.persistence.testsuite.entity.DocumentTupleEntity;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.PersonCTE;
import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.testsuite.entity.Workflow;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Moritz Becker (moritz.becker@gmx.at)
 * @since 1.2
 */
public class Issue358Test extends AbstractCoreTest {

    private Person p1;
    private Document d1;
    private Document d2;
    private Document d3;

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
                Document.class,
                Version.class,
                Person.class,
                IntIdEntity.class,
                DocumentTupleEntity.class
        };
    }

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                p1 = new Person("p1");
                d1 = new Document("doc1", p1);
                d2 = new Document("doc2", p1);
                d3 = new Document("doc3", p1);

                em.persist(p1);
                em.persist(d1);
                em.persist(d2);
                em.persist(d3);
            }
        });
    }

    @Before
    public void setUp() {
        p1 = cbf.create(em, Person.class).getSingleResult();
        d1 = cbf.create(em, Document.class).where("name").eq("doc1").getSingleResult();
        d2 = cbf.create(em, Document.class).where("name").eq("doc2").getSingleResult();
        d3 = cbf.create(em, Document.class).where("name").eq("doc3").getSingleResult();
    }

    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testValuesEntityFunctionMultiRelation() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);
        cb.setProperty(ConfigurationProperties.VALUES_CLAUSE_FILTER_NULLS, "false");

        cb.fromValues(DocumentTupleEntity.class, "documentTuple", Arrays.asList(new DocumentTupleEntity(d1, d2), new DocumentTupleEntity(d2, d3)));
        cb.select("documentTuple.element1.id");
        cb.select("documentTuple.element2.id");

        // Empty values
        List<Tuple> resultList = cb.getResultList();
        assertEquals(1, resultList.size());

        assertNull(resultList.get(0).get(0));
        assertNull(resultList.get(0).get(1));
    }
}
