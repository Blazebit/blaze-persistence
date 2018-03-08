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

package com.blazebit.persistence.view.testsuite.collections.singleton;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.collections.singleton.model.SingletonDocumentCollectionsView;
import com.blazebit.persistence.view.testsuite.collections.singleton.model.SingletonPersonView;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests mapping relations as singleton collections
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SingletonCollectionsTest extends AbstractEntityViewTest {

    protected Document doc1;
    protected Document doc2;
    protected Document doc3;
    protected Document doc4;

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                doc1 = new Document("doc1");
                doc2 = new Document("doc2");
                doc3 = new Document("doc3");
                doc4 = new Document("doc4");

                Person o1 = new Person("pers1");
                Person o2 = new Person("pers2");
                Person o3 = new Person("pers3");

                doc1.setOwner(o1);
                doc2.setOwner(o2);
                doc3.setOwner(o2);
                doc4.setOwner(o2);

                em.persist(o1);
                em.persist(o2);
                em.persist(o3);

                em.persist(doc1);
                em.persist(doc2);
                em.persist(doc3);
                em.persist(doc4);
            }
        });
    }

    @Before
    public void setUp() {
        doc1 = cbf.create(em, Document.class).where("name").eq("doc1").getSingleResult();
        doc2 = cbf.create(em, Document.class).where("name").eq("doc2").getSingleResult();
        doc3 = cbf.create(em, Document.class).where("name").eq("doc3").getSingleResult();
        doc4 = cbf.create(em, Document.class).where("name").eq("doc4").getSingleResult();
    }

    @Test
    public void testSingletonCollections() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(SingletonDocumentCollectionsView.class);
        cfg.addEntityView(SingletonPersonView.class);
        EntityViewManager evm = cfg.createEntityViewManager(cbf);

        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d")
            .orderByAsc("id");
        CriteriaBuilder<SingletonDocumentCollectionsView> cb = evm.applySetting(EntityViewSetting.create(SingletonDocumentCollectionsView.class), criteria);
        List<SingletonDocumentCollectionsView> results = cb.getResultList();

        assertEquals(4, results.size());
        assertThisAndOwnerMappings(doc1, results.get(0));
        assertThisAndOwnerMappings(doc2, results.get(1));
        assertThisAndOwnerMappings(doc3, results.get(2));
        assertThisAndOwnerMappings(doc4, results.get(3));
    }

    private void assertThisAndOwnerMappings(Document doc, SingletonDocumentCollectionsView view) {
        assertEquals(doc.getId(), view.getId());
        assertEquals(doc.getName(), view.getName());

        assertEquals(doc.getOwner(), view.getOwnerEntity());
        assertEquals(doc.getOwner().getId(), view.getOwnerEntityId());
        assertPersonEqualsView(doc.getOwner(), view.getOwnerEntityView());

        assertEquals(1, view.getOwnerEntityList().size());
        assertEquals(doc.getOwner(), view.getOwnerEntityList().get(0));

        assertEquals(1, view.getOwnerEntityIdList().size());
        assertEquals(doc.getOwner().getId(), view.getOwnerEntityIdList().get(0));

        assertEquals(1, view.getOwnerEntityViewList().size());
        assertPersonEqualsView(doc.getOwner(), view.getOwnerEntityViewList().get(0));
    }

    private void assertPersonEqualsView(Person pers, SingletonPersonView view) {
        assertEquals(pers.getId(), view.getId());
        assertEquals(pers.getName().toUpperCase(), view.getName());
    }
}
