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

package com.blazebit.persistence.view.testsuite.flat;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.flat.model.ConstructorOnlyPersonFlatView;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class ConstructorOnlyFlatViewTest extends AbstractEntityViewTest {

    protected EntityViewManager evm;

    @Before
    public void initEvm() {
        evm = build(ConstructorOnlyPersonFlatView.class);
    }

    @Before
    public void setUp() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Document doc1 = new Document("doc1");
                Document doc2 = new Document("doc2");

                Person o1 = new Person("pers1");

                doc1.setOwner(o1);
                doc2.setOwner(o1);

                em.persist(o1);
                em.persist(doc1);
                em.persist(doc2);
            }
        });
    }

    @Test
    public void queryFlatView() {
        CriteriaBuilder<Person> criteria = cbf.create(em, Person.class, "p")
                .orderByAsc("id");
        CriteriaBuilder<ConstructorOnlyPersonFlatView> cb = evm.applySetting(EntityViewSetting.create(ConstructorOnlyPersonFlatView.class), criteria);
        List<ConstructorOnlyPersonFlatView> results = cb.getResultList();

        assertEquals(1, results.size());

        assertEquals("pers1", results.get(0).getName());
        assertEquals(ConstructorOnlyPersonFlatView.class.getSimpleName() + "()", results.get(0).toString());
    }

}
