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

package com.blazebit.persistence.view.testsuite.embedded;

import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import org.junit.Before;

import javax.persistence.EntityManager;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class AbstractEmbeddedMappingTest extends AbstractEntityViewTest {

    protected Document doc1;
    protected Document doc2;

    @Before
    public void setUp() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Person p = new Person("pers");
                doc1 = new Document("doc1", p);
                doc2 = new Document("doc2", p);

                em.persist(p);
                em.persist(doc1);
                em.persist(doc2);
            }
        });
    }
}
