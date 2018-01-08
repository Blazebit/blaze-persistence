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

package com.blazebit.persistence.view.testsuite.flat;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.metamodel.FlatViewType;
import com.blazebit.persistence.view.metamodel.ViewMetamodel;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.testsuite.flat.model.DocumentFlatEmbeddingView;
import com.blazebit.persistence.view.testsuite.flat.model.DocumentWithFlatCollectionView;
import com.blazebit.persistence.view.testsuite.flat.model.PersonFlatView;
import com.blazebit.persistence.view.testsuite.flat.model.UpdatableDocumentFlatView;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class FlatViewCollectionTest extends AbstractEntityViewTest {

    protected EntityViewManager evm;

    @Before
    public void initEvm() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(DocumentWithFlatCollectionView.class);
        cfg.addEntityView(PersonFlatView.class);
        evm = cfg.createEntityViewManager(cbf);
    }

    private Document doc1;
    private Document doc2;

    @Before
    public void setUp() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                doc1 = new Document("doc1");
                doc2 = new Document("doc2");

                Person o1 = new Person("pers1");
                Person o2 = new Person("pers2");
                Person o3 = new Person("pers3");
                Person o4 = new Person("pers4");

                doc1.setOwner(o1);
                doc2.setOwner(o2);

                doc1.getContacts().put(1, o1);
                doc1.getContacts().put(2, o2);
                doc2.getContacts().put(1, o3);
                doc2.getContacts().put(2, o4);

                em.persist(o1);
                em.persist(o2);
                em.persist(o3);
                em.persist(o4);

                em.persist(doc1);
                em.persist(doc2);
            }
        });

        // Apparently EclipseLink and DataNucleus aren't smart enough to figure out
        // there is only a single entity so we have to use getResultList().get(0) instead of getSingleResult()..
        doc1 = cbf.create(em, Document.class)
                .where("id").eq(doc1.getId())
                .fetch("contacts")
                .getResultList().get(0);
        doc2 = cbf.create(em, Document.class)
                .where("id").eq(doc2.getId())
                .fetch("contacts")
                .getResultList().get(0);
    }

    @Test
    public void flatViewInCollection() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "p")
                .orderByAsc("id")
                .orderByAsc("contacts.name");
        CriteriaBuilder<DocumentWithFlatCollectionView> cb = evm.applySetting(EntityViewSetting.create(DocumentWithFlatCollectionView.class), criteria);
        List<DocumentWithFlatCollectionView> results = cb.getResultList();

        assertEquals(2, results.size());

        assertEquals(doc1.getId(), results.get(0).getId());
        assertEquals(doc1.getName(), results.get(0).getName());
        assertEquals(2, results.get(0).getContacts().size());
        assertEquals("pers1", results.get(0).getContacts().get(0).getName());
        assertEquals("pers2", results.get(0).getContacts().get(1).getName());
        assertEquals(doc2.getId(), results.get(1).getId());
        assertEquals(doc2.getName(), results.get(1).getName());
        assertEquals(2, results.get(1).getContacts().size());
        assertEquals("pers3", results.get(1).getContacts().get(0).getName());
        assertEquals("pers4", results.get(1).getContacts().get(1).getName());
    }

}
