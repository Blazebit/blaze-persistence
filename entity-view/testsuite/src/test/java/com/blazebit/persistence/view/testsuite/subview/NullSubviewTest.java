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

package com.blazebit.persistence.view.testsuite.subview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.persistence.EntityManager;

import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.testsuite.subview.model.SimpleDocumentView;
import org.junit.Before;
import org.junit.Test;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.testsuite.subview.model.DocumentMasterView;
import com.blazebit.persistence.view.testsuite.subview.model.PersonSubView;
import com.blazebit.persistence.view.testsuite.subview.model.PersonSubViewFiltered;
import org.junit.experimental.categories.Category;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class NullSubviewTest extends AbstractEntityViewTest {

    private Document doc1;

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                doc1 = new Document("doc1");

                Person o1 = new Person("pers1");
                o1.getLocalized().put(1, "localized1");

                doc1.setOwner(o1);

                em.persist(o1);

                em.persist(doc1);
            }
        });
    }

    @Before
    public void setUp() {
        doc1 = cbf.create(em, Document.class).where("name").eq("doc1").getSingleResult();
    }

    @Test
    @Category({ NoEclipselink.class })
    // Eclipselink has a result set mapping bug in case of map keys
    public void testSubview() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(DocumentMasterView.class);
        cfg.addEntityView(SimpleDocumentView.class);
        cfg.addEntityView(PersonSubView.class);
        cfg.addEntityView(PersonSubViewFiltered.class);
        EntityViewManager evm = cfg.createEntityViewManager(cbf);

        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d")
            .orderByAsc("id");
        CriteriaBuilder<DocumentMasterView> cb = evm.applySetting(EntityViewSetting.create(DocumentMasterView.class), criteria)
            .setParameter("contactPersonNumber", 2);
        List<DocumentMasterView> results = cb.getResultList();

        assertEquals(1, results.size());
        DocumentMasterView res = results.get(0);
        // Doc1
        assertEquals(doc1.getName(), res.getName());
        assertEquals("PERS1", res.getOwner().getName());
        assertEquals(Integer.valueOf(2), res.getContactPersonNumber());
        assertEquals(Integer.valueOf(2), res.getTheContactPersonNumber());
        // Filtered subview
        assertNull(res.getMyContactPerson());

        assertTrue(res.getContacts().isEmpty());
        assertTrue(res.getPartners().isEmpty());
        assertTrue(res.getPeople().isEmpty());
    }
}
