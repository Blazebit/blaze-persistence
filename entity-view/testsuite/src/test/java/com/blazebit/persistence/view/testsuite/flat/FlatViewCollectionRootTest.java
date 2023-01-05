/*
 * Copyright 2014 - 2023 Blazebit.
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
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.flat.model.NameObjectView;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Christian Beikov
 * @since 1.6.8
 */
public class FlatViewCollectionRootTest extends AbstractEntityViewTest {

    protected EntityViewManager evm;

    @Before
    public void initEvm() {
        evm = build(
                NameObjectView.class
        );
    }

    @Before
    public void setUp() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Person o1 = new Person("pers1");
                em.persist(o1);

                Document doc1 = new Document("doc1");
                doc1.setOwner(o1);
                em.persist(doc1);
            }
        });
    }

    @Test
    public void queryFlatView() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d")
                .orderByAsc("id");
        CriteriaBuilder<NameObjectView> cb = evm.applySetting(EntityViewSetting.create(NameObjectView.class), criteria, "names");
        List<NameObjectView> results = cb.getResultList();

        assertEquals(0, results.size());
    }

}
