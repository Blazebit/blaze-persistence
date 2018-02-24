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
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Test;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class SelectNewObjectBuilderTest extends AbstractCoreTest {

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Person p = new Person("Karl");
                em.persist(p);

                Version v1 = new Version();
                Version v2 = new Version();
                Version v3 = new Version();
                em.persist(v1);
                em.persist(v2);
                em.persist(v3);

                em.persist(new Document("Doc1", p, v1, v3));
                em.persist(new Document("Doc2", p, v2));
            }
        });
    }

    @Test
    public void testSelectNewDocumentObjectBuilder() {
        CriteriaBuilder<String[]> criteria = cbf.create(em, Document.class, "d")
            .selectNew(new ObjectBuilder<String[]>() {

                @Override
                public <X extends SelectBuilder<X>> void applySelects(X queryBuilder) {
                    queryBuilder
                        .select("name", "name")
                        .select("UPPER(name)", "upperName")
                        .select("LOWER(name)", "lowerName")
                        .select("false");
                }

                @Override
                public String[] build(Object[] tuple) {
                    return new String[]{ (String) tuple[0], (String) tuple[1], (String) tuple[2] };
                }

                @Override
                public List<String[]> buildList(List<String[]> list) {
                    return list;
                }
            });
        assertEquals("SELECT d.name AS name, UPPER(d.name) AS upperName, LOWER(d.name) AS lowerName, " + STATIC_JPA_PROVIDER.getBooleanExpression(false)+ " FROM Document d", criteria.getQueryString());
        List<String[]> actual = criteria.getQuery().getResultList();

        assertArrayEquals(new String[]{ "Doc1", "DOC1", "doc1" }, actual.get(0));
        assertArrayEquals(new String[]{ "Doc2", "DOC2", "doc2" }, actual.get(1));
    }
}
