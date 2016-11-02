/*
 * Copyright 2015 Blazebit.
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
package com.blazebit.persistence.view.testsuite.update;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.category.NoDatanucleus;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.entity.Document;
import com.blazebit.persistence.view.testsuite.entity.Person;
import com.blazebit.persistence.view.testsuite.update.model.FullDocumentView;
import com.blazebit.persistence.view.testsuite.update.model.PartialDocumentView;
import com.blazebit.persistence.view.testsuite.update.model.UpdatableDocumentView;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
@RunWith(Parameterized.class)
public class EntityViewUpdateTest<T extends UpdatableDocumentView> extends AbstractEntityViewUpdateTest {

    private Class<T> viewType;
    private EntityViewManager evm;
    private Document doc;
    
    public EntityViewUpdateTest(Class<T> viewType) {
        this.viewType = viewType;
    }

    @Parameterized.Parameters
    public static Collection<?> entityViewCombinations() {
        return Arrays.asList(new Object[][]{
            { PartialDocumentView.class },
            { FullDocumentView.class }
        });
    }

    @Before
    public void setUp() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(viewType);
        evm = cfg.createEntityViewManager(cbf, em.getEntityManagerFactory());

        transactional(new TxVoidWork() {

            @Override
            public void doWork(EntityManager em) {
                doc = new Document("doc");
    
                Person o1 = new Person("pers1");
                o1.getLocalized().put(1, "localized1");
                Person o2 = new Person("pers2");
                o2.getLocalized().put(1, "localized2");
    
                doc.setOwner(o1);
                doc.getContacts().put(1, o1);
                doc.getContacts2().put(2, o1);
    
                em.persist(o1);
                em.persist(o2);
                em.persist(doc);
                
                o1.setPartnerDocument(doc);
    
                em.flush();
            }
        });
        
        em.clear();
        doc = em.find(Document.class, doc.getId());
    }

    @Test
    @Category({ NoDatanucleus.class })
    public void testUpdateWithEntity() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d").orderByAsc("id");
        CriteriaBuilder<T> cb = evm.applySetting(EntityViewSetting.create(viewType), criteria);
        List<T> results = cb.getResultList();
        final T docView = results.get(0);
        
        // When
        transactional(new TxVoidWork() {

            @Override
            public void doWork(EntityManager em) {
                docView.setOwner(cbf.create(em, Person.class).where("name").eq("pers2").getSingleResult());
                evm.update(em, docView);
                em.flush();
            }
        });

        // Then
        em.clear();
        doc = em.find(Document.class, doc.getId());
        assertEquals(doc.getOwner().getId(), docView.getOwner().getId());
    }

    @Test
    @Category({ NoDatanucleus.class })
    public void testSimpleUpdate() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d").orderByAsc("id");
        CriteriaBuilder<T> cb = evm.applySetting(EntityViewSetting.create(viewType), criteria);
        List<T> results = cb.getResultList();
        final T docView = results.get(0);
        
        // When
        transactional(new TxVoidWork() {

            @Override
            public void doWork(EntityManager em) {
                docView.setName("newDoc");
                evm.update(em, docView);
                em.flush();
            }
        });

        // Then
        em.clear();
        doc = em.find(Document.class, doc.getId());
        assertEquals(doc.getName(), docView.getName());
    }

    @Test
    @Category({ NoDatanucleus.class })
    public void testUpdateRollbacked() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d").orderByAsc("id");
        CriteriaBuilder<T> cb = evm.applySetting(EntityViewSetting.create(viewType), criteria);
        List<T> results = cb.getResultList();
        final T docView = results.get(0);
        
        // When
        transactional(new TxVoidWork() {

            @Override
            public void doWork(EntityManager em) {
                EntityTransaction tx = em.getTransaction();
                docView.setName("newDoc");
                evm.update(em, docView);
                em.flush();
                tx.rollback();
    
                tx.begin();
                evm.update(em, docView);
                em.flush();
            }
        });

        // Then
        em.clear();
        doc = em.find(Document.class, doc.getId());
        assertEquals(doc.getName(), docView.getName());
    }

    @Test
    @Category({ NoDatanucleus.class })
    public void testModifyAndUpdateRollbacked() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d").orderByAsc("id");
        CriteriaBuilder<T> cb = evm.applySetting(EntityViewSetting.create(viewType), criteria);
        List<T> results = cb.getResultList();
        final T docView = results.get(0);
        
        // When
        transactional(new TxVoidWork() {

            @Override
            public void doWork(EntityManager em) {
                EntityTransaction tx = em.getTransaction();
                docView.setName("newDoc");
                evm.update(em, docView);
                em.flush();
                tx.rollback();
                
                docView.setName("newDoc1");
                // Remove milliseconds because MySQL doesn't use that precision by default
                Date date = new Date();
                date.setTime(1000 * (date.getTime() / 1000));
                docView.setLastModified(date);
    
                tx.begin();
                evm.update(em, docView);
                em.flush();
            }
        });

        // Then
        em.clear();
        doc = em.find(Document.class, doc.getId());
        assertEquals(doc.getName(), docView.getName());
        assertEquals(doc.getLastModified().getTime(), docView.getLastModified().getTime());
    }
}
