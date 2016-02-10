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
package com.blazebit.persistence.view.update;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityTransaction;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.category.NoDatanucleus;
import com.blazebit.persistence.view.AbstractEntityViewTest;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.entity.Document;
import com.blazebit.persistence.view.entity.Person;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.update.model.FullUpdatableDocumentWithCollectionsView;
import com.blazebit.persistence.view.update.model.PartialUpdatableDocumentWithCollectionsView;
import com.blazebit.persistence.view.update.model.UpdatableDocumentWithCollectionsView;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
@RunWith(Parameterized.class)
public class EntityViewUpdateWithCollectionsTest<T extends UpdatableDocumentWithCollectionsView> extends AbstractEntityViewTest {

    private Class<T> viewType;
    private EntityViewManager evm;
    private Document doc;
    
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
        
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
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
            tx.commit();
            em.clear();

            doc = em.find(Document.class, doc.getId());
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        }
    }

    @Test
    @Category({ NoDatanucleus.class })
    public void testUpdateReplaceCollection() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d").orderByAsc("id");
        CriteriaBuilder<T> cb = evm.applySetting(EntityViewSetting.create(viewType), criteria);
        List<T> results = cb.getResultList();
        T docView = results.get(0);
        
        // When
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
	        docView.setPersonList(new ArrayList<Person>(docView.getPersonList()));
	        evm.update(em, docView);
            em.flush();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        }

        // Then
        em.clear();
        doc = em.find(Document.class, doc.getId());
        assertEquals(doc.getPersonList(), docView.getPersonList());
    }
}
