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
import com.blazebit.persistence.view.testsuite.flat.model.PersonFlatView;
import com.blazebit.persistence.view.testsuite.flat.model.UpdatableDocumentFlatView;
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
 * @since 1.2.0
 */
public class FlatViewTest extends AbstractEntityViewTest {

    protected EntityViewManager evm;

    @Before
    public void initEvm() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(UpdatableDocumentFlatView.class);
        cfg.addEntityView(DocumentFlatEmbeddingView.class);
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
                o1.getLocalized().put(1, "localized1");
                o2.getLocalized().put(1, "localized2");
                o1.setPartnerDocument(doc1);
                o2.setPartnerDocument(doc2);

                doc1.setOwner(o1);
                doc2.setOwner(o2);

                doc1.getContacts().put(1, o1);
                doc2.getContacts().put(1, o2);

                doc1.getContacts2().put(2, o1);
                doc2.getContacts2().put(2, o2);

                em.persist(o1);
                em.persist(o2);

                em.persist(doc1);
                em.persist(doc2);
            }
        });

        doc1 = cbf.create(em, Document.class)
                .where("id").eq(doc1.getId())
                .fetch("owner")
                .getSingleResult();
        doc2 = cbf.create(em, Document.class)
                .where("id").eq(doc2.getId())
                .fetch("owner")
                .getSingleResult();
    }

    @Test
    public void testMetamodel() {
        ViewMetamodel metamodel = evm.getMetamodel();
        ViewType<?> viewType = metamodel.view(DocumentFlatEmbeddingView.class);
        assertEquals("id", viewType.getIdAttribute().getName());
        assertTrue(metamodel.managedView(PersonFlatView.class) instanceof FlatViewType);
    }

    @Test
    public void queryFlatView() {
        CriteriaBuilder<Person> criteria = cbf.create(em, Person.class, "p")
                .orderByAsc("id");
        CriteriaBuilder<PersonFlatView> cb = evm.applySetting(EntityViewSetting.create(PersonFlatView.class), criteria);
        List<PersonFlatView> results = cb.getResultList();

        assertEquals(2, results.size());

        assertEquals(doc1.getOwner().getName(), results.get(0).getName());
        assertEquals(doc2.getOwner().getName(), results.get(1).getName());
    }

    @Test
    public void queryFlatEmbeddingView() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "p")
                .orderByAsc("id");
        CriteriaBuilder<DocumentFlatEmbeddingView> cb = evm.applySetting(EntityViewSetting.create(DocumentFlatEmbeddingView.class), criteria);
        List<DocumentFlatEmbeddingView> results = cb.getResultList();

        assertEquals(2, results.size());

        assertEquals(doc1.getId(), results.get(0).getId());
        assertEquals(doc1.getName(), results.get(0).getName());
        assertEquals(doc1.getOwner().getName(), results.get(0).getOwner().getName());
        assertEquals(doc2.getId(), results.get(1).getId());
        assertEquals(doc2.getName(), results.get(1).getName());
        assertEquals(doc2.getOwner().getName(), results.get(1).getOwner().getName());
    }

    @Test
    public void flatViewsUseAllPropertiesInEqualsHashCode() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "p")
                .orderByAsc("id");
        CriteriaBuilder<UpdatableDocumentFlatView> cb = evm.applySetting(EntityViewSetting.create(UpdatableDocumentFlatView.class), criteria);
        List<UpdatableDocumentFlatView> results = cb.getResultList();

        assertEquals(2, results.size());

        assertEquals(doc1.getName(), results.get(0).getName());
        assertEquals(doc2.getName(), results.get(1).getName());
        assertNotEquals(results.get(0), results.get(1));

        results.get(1).setName(doc1.getName());
        assertEquals(results.get(0), results.get(1));
    }

}
