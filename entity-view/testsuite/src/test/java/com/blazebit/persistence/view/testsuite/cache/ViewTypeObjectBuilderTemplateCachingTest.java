/*
 * Copyright 2014 - 2024 Blazebit.
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

package com.blazebit.persistence.view.testsuite.cache;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.cache.model.DocumentCorrelatingOwner1View;
import com.blazebit.persistence.view.testsuite.cache.model.PersonView;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class ViewTypeObjectBuilderTemplateCachingTest extends AbstractEntityViewTest {

    private Document doc1;
    private Document doc2;

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {

            @Override
            public void work(EntityManager em) {
                doc1 = new Document("doc1");
                doc2 = new Document("doc2");

                Person o1 = new Person("pers1", 64);
                Person o2 = new Person("pers2", 32);
                Person o3 = new Person("pers3", 16);
                o1.getLocalized().put(1, "localized1");
                o2.getLocalized().put(1, "localized2");
                o3.getLocalized().put(1, "localized3");

                doc1.setAge(10);
                doc1.setOwner(o1);
                doc2.setAge(20);
                doc2.setOwner(o2);

                doc1.getContacts().put(1, o1);
                doc2.getContacts().put(1, o2);

                doc1.getContacts2().put(2, o1);
                doc2.getContacts2().put(2, o2);

                em.persist(o1);
                em.persist(o2);
                em.persist(o3);

                // Flush doc1 before so we get the ids we would expect
                em.persist(doc1);
                em.flush();

                em.persist(doc2);
                em.flush();

                o1.setPartnerDocument(doc1);
                o2.setPartnerDocument(doc2);
                o3.setPartnerDocument(doc2);
            }
        });
    }

    @Before
    public void setUp() {
        doc1 = cbf.create(em, Document.class).where("name").eq("doc1").getSingleResult();
        doc2 = cbf.create(em, Document.class).where("name").eq("doc2").getSingleResult();
    }

    @Test
    public void testViewTypeObjectBuilderTemplateCaching() {
        EntityViewManager evm = build(DocumentCorrelatingOwner1View.class, PersonView.class);

        CriteriaBuilder<Document> cb = cbf.create(em, Document.class).orderByAsc("id");
        evm.applySetting(EntityViewSetting.create(DocumentCorrelatingOwner1View.class), cb).getResultList();

        cb = cbf.create(em, Document.class).orderByAsc("id");
        evm.applySetting(EntityViewSetting.create(DocumentCorrelatingOwner1View.class), cb, "parent").getResultList();
    }
}
