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
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.DocumentTupleEntity;
import com.blazebit.persistence.testsuite.entity.EmbeddedDocumentTupleEntity;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * This test is for issue #358
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class EntityInValuesClauseTest extends AbstractCoreTest {

    private Person p1;
    private Document d1;
    private Document d2;
    private Document d3;

    @Override
    protected Class<?>[] getEntityClasses() {
        return concat(super.getEntityClasses(), new Class<?>[]{
                DocumentTupleEntity.class,
                EmbeddedDocumentTupleEntity.class
        });
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
    public void entityClassWithMultipleRelationsInValuesClause() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);

        cb.fromValues(DocumentTupleEntity.class, "documentTuple", Arrays.asList(new DocumentTupleEntity(d1, d2), new DocumentTupleEntity(d2, d3)));
        cb.select("documentTuple.element1.id");
        cb.select("documentTuple.element2.id");
        cb.orderByAsc("documentTuple.element1.id");

        TypedQuery<Tuple> query = cb.getQuery();
        List<Tuple> resultList = query.getResultList();
        assertEquals(2, resultList.size());

        assertEquals(d1.getId(), resultList.get(0).get(0));
        assertEquals(d2.getId(), resultList.get(0).get(1));

        assertEquals(d2.getId(), resultList.get(1).get(0));
        assertEquals(d3.getId(), resultList.get(1).get(1));
    }

    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void entityClassWithEmbeddedIdInValuesClause() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);

        cb.fromValues(EmbeddedDocumentTupleEntity.class, "documentTuple", Arrays.asList(new EmbeddedDocumentTupleEntity(d1.getId(), d2.getId()), new EmbeddedDocumentTupleEntity(d2.getId(), d3.getId())));
        cb.select("documentTuple.id.element1");
        cb.select("documentTuple.id.element2");
        cb.orderByAsc("documentTuple.id.element1");

        TypedQuery<Tuple> query = cb.getQuery();
        List<Tuple> resultList = query.getResultList();
        assertEquals(2, resultList.size());

        assertEquals(d1.getId(), resultList.get(0).get(0));
        assertEquals(d2.getId(), resultList.get(0).get(1));

        assertEquals(d2.getId(), resultList.get(1).get(0));
        assertEquals(d3.getId(), resultList.get(1).get(1));
    }
}
