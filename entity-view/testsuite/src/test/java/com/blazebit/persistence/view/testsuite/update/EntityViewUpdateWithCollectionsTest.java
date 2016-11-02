/*
 * Copyright 2014 - 2016 Blazebit.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;

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
import com.blazebit.persistence.view.testsuite.update.model.FullUpdatableDocumentWithCollectionsView;
import com.blazebit.persistence.view.testsuite.update.model.PartialUpdatableDocumentWithCollectionsView;
import com.blazebit.persistence.view.testsuite.update.model.UpdatableDocumentWithCollectionsView;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
@RunWith(Parameterized.class)
public class EntityViewUpdateWithCollectionsTest<T extends UpdatableDocumentWithCollectionsView> extends AbstractEntityViewUpdateTest {

    private Class<T> viewType;
    private EntityViewManager evm;
    private Document doc;
    private Person p1;
    private Person p2;
    
    public EntityViewUpdateWithCollectionsTest(Class<T> viewType) {
        this.viewType = viewType;
    }

    @Parameterized.Parameters
    public static Collection<?> entityViewCombinations() {
        return Arrays.asList(new Object[][]{
            { PartialUpdatableDocumentWithCollectionsView.class },
            { FullUpdatableDocumentWithCollectionsView.class }
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
    
                p1 = new Person("pers1");
                p1.getLocalized().put(1, "localized1");
                p2 = new Person("pers2");
                p2.getLocalized().put(1, "localized2");
    
                doc.setOwner(p1);
                doc.getPersonList().add(p1);
                doc.getContacts().put(1, p1);
                doc.getContacts2().put(2, p1);
    
                em.persist(p1);
                em.persist(p2);
                em.persist(doc);
                
                p1.setPartnerDocument(doc);
    
                em.flush();
            }
        });
        
        em.clear();
        doc = em.find(Document.class, doc.getId());
    }

    @Test
    @Category({ NoDatanucleus.class })
    public void testUpdateReplaceCollection() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d").orderByAsc("id");
        CriteriaBuilder<T> cb = evm.applySetting(EntityViewSetting.create(viewType), criteria);
        List<T> results = cb.getResultList();
        final T docView = results.get(0);
        
        // When
        transactional(new TxVoidWork() {

            @Override
            public void doWork(EntityManager em) {
                docView.setPersonList(new ArrayList<Person>(docView.getPersonList()));
                evm.update(em, docView);
                em.flush();
            }
        });

        // Then
        em.clear();
        doc = cbf.create(em, Document.class).fetch("personList").where("id").eq(doc.getId()).getSingleResult();
        assertEquals(doc.getPersonList(), docView.getPersonList());
    }

    @Test
    @Category({ NoDatanucleus.class })
    public void testUpdateAddToCollection() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d").orderByAsc("id");
        CriteriaBuilder<T> cb = evm.applySetting(EntityViewSetting.create(viewType), criteria);
        List<T> results = cb.getResultList();
        final T docView = results.get(0);
        
        // When
        transactional(new TxVoidWork() {

            @Override
            public void doWork(EntityManager em) {
                docView.getPersonList().add(em.find(Person.class, p2.getId()));
                evm.update(em, docView);
                em.flush();
            }
        });

        // Then
        em.clear();
        doc = cbf.create(em, Document.class).fetch("personList").where("id").eq(doc.getId()).getSingleResult();
        assertEquals(doc.getPersonList(), docView.getPersonList());
    }
}
