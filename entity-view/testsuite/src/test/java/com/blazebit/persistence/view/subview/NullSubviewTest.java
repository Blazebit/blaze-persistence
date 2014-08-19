/*
 * Copyright 2014 Blazebit.
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
package com.blazebit.persistence.view.subview;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.view.AbstractEntityViewTest;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.entity.Document;
import com.blazebit.persistence.view.entity.Person;
import com.blazebit.persistence.view.impl.EntityViewConfigurationImpl;
import com.blazebit.persistence.view.subview.model.DocumentMasterView;
import com.blazebit.persistence.view.subview.model.PersonSubView;
import com.blazebit.persistence.view.subview.model.PersonSubViewFiltered;
import java.util.List;
import javax.persistence.EntityTransaction;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class NullSubviewTest extends AbstractEntityViewTest {

    private Document doc1;

    @Before
    public void setUp() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            doc1 = new Document("doc1");

            Person o1 = new Person("pers1");
            o1.getLocalized().put(1, "localized1");

            doc1.setOwner(o1);

            em.persist(o1);

            em.persist(doc1);

            em.flush();
            tx.commit();
            em.clear();

            doc1 = em.find(Document.class, doc1.getId());
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testSubview() {
        EntityViewConfigurationImpl cfg = new EntityViewConfigurationImpl();
        cfg.addEntityView(DocumentMasterView.class);
        cfg.addEntityView(PersonSubView.class);
        cfg.addEntityView(PersonSubViewFiltered.class);
        EntityViewManager evm = cfg.createEntityViewManager();

        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d")
            .orderByAsc("id");
        CriteriaBuilder<DocumentMasterView> cb = evm.applyObjectBuilder(DocumentMasterView.class, criteria)
            .setParameter("contactPersonNumber", 2);
        System.out.println(cb.getQueryString());
        List<DocumentMasterView> results = cb.getResultList();

        assertEquals(1, results.size());
        DocumentMasterView res = results.get(0);
        // Doc1
        assertEquals(doc1.getName(), res.getName());
        assertEquals("PERS1", res.getOwner().getName());
        assertEquals(Integer.valueOf(2), res.getContactPersonNumber());
        assertEquals(Integer.valueOf(2), res.getTheContactPersonNumber());
        // Filtered subview
        assertNull(results.get(0).getMyContactPerson());

        assertTrue(results.get(0).getContacts().isEmpty());
        assertTrue(results.get(0).getPartners().isEmpty());
        assertTrue(results.get(0).getPersonList().isEmpty());
    }
}
