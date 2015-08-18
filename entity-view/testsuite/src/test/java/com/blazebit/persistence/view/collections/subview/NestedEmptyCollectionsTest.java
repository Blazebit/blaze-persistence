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
package com.blazebit.persistence.view.collections.subview;

import static com.blazebit.persistence.view.collections.subview.SubviewAssert.assertSubviewEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityTransaction;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.view.AbstractEntityViewTest;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.collections.entity.DocumentForCollections;
import com.blazebit.persistence.view.collections.entity.PersonForCollections;
import com.blazebit.persistence.view.collections.subview.model.SubviewDocumentCollectionsView;
import com.blazebit.persistence.view.collections.subview.model.SubviewDocumentListMapSetView;
import com.blazebit.persistence.view.collections.subview.model.SubviewDocumentListSetMapView;
import com.blazebit.persistence.view.collections.subview.model.SubviewDocumentMapListSetView;
import com.blazebit.persistence.view.collections.subview.model.SubviewDocumentMapSetListView;
import com.blazebit.persistence.view.collections.subview.model.SubviewDocumentSetListMapView;
import com.blazebit.persistence.view.collections.subview.model.SubviewDocumentSetMapListView;
import com.blazebit.persistence.view.collections.subview.model.SubviewPersonForCollectionsView;
import com.blazebit.persistence.view.collections.subview.model.variations.PersonForCollectionsListMapSetMasterView;
import com.blazebit.persistence.view.collections.subview.model.variations.PersonForCollectionsListSetMapMasterView;
import com.blazebit.persistence.view.collections.subview.model.variations.PersonForCollectionsMapListSetMasterView;
import com.blazebit.persistence.view.collections.subview.model.variations.PersonForCollectionsMapSetListMasterView;
import com.blazebit.persistence.view.collections.subview.model.variations.PersonForCollectionsMasterView;
import com.blazebit.persistence.view.collections.subview.model.variations.PersonForCollectionsSetListMapMasterView;
import com.blazebit.persistence.view.collections.subview.model.variations.PersonForCollectionsSetMapListMasterView;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
@RunWith(Parameterized.class)
public class NestedEmptyCollectionsTest<T extends PersonForCollectionsMasterView, U extends SubviewDocumentCollectionsView> extends AbstractEntityViewTest {

    private final Class<T> viewType;
    private final Class<U> subviewType;

    private PersonForCollections pers1;
    private PersonForCollections pers2;

    public NestedEmptyCollectionsTest(Class<T> viewType, Class<U> subviewType) {
        this.viewType = viewType;
        this.subviewType = subviewType;
    }

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
            DocumentForCollections.class,
            PersonForCollections.class
        };
    }

    @Before
    public void setUp() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            DocumentForCollections doc1 = new DocumentForCollections("doc1");
            DocumentForCollections doc2 = new DocumentForCollections("doc2");
            DocumentForCollections doc3 = new DocumentForCollections("doc3");
            DocumentForCollections doc4 = new DocumentForCollections("doc4");

            pers1 = new PersonForCollections("pers1");
            pers2 = new PersonForCollections("pers2");

            doc1.setOwner(pers1);
            doc2.setOwner(pers1);
            doc3.setOwner(pers1);
            doc4.setOwner(pers1);

            em.persist(pers1);
            em.persist(pers2);

            em.persist(doc1);
            em.persist(doc2);
            em.persist(doc3);
            em.persist(doc4);

            em.flush();
            tx.commit();
            em.clear();

            pers1 = em.find(PersonForCollections.class, pers1.getId());
            pers2 = em.find(PersonForCollections.class, pers2.getId());
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        }
    }

    @Parameterized.Parameters
    public static Collection<?> entityViewCombinations() {
        return Arrays.asList(new Object[][]{
            { PersonForCollectionsListMapSetMasterView.class, SubviewDocumentListMapSetView.class },
            { PersonForCollectionsListSetMapMasterView.class, SubviewDocumentListSetMapView.class },
            { PersonForCollectionsMapListSetMasterView.class, SubviewDocumentMapListSetView.class },
            { PersonForCollectionsMapSetListMasterView.class, SubviewDocumentMapSetListView.class },
            { PersonForCollectionsSetListMapMasterView.class, SubviewDocumentSetListMapView.class },
            { PersonForCollectionsSetMapListMasterView.class, SubviewDocumentSetMapListView.class }
        });
    }

    @Test
    public void testCollections() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(viewType);
        cfg.addEntityView(subviewType);
        cfg.addEntityView(SubviewPersonForCollectionsView.class);
        EntityViewManager evm = cfg.createEntityViewManager();

        CriteriaBuilder<PersonForCollections> criteria = cbf.create(em, PersonForCollections.class, "p")
            .where("id").in(pers1.getId(), pers2.getId())
            .orderByAsc("id");
        CriteriaBuilder<T> cb = evm.applySetting(EntityViewSetting.create(viewType), criteria);
        List<T> results = cb.getResultList();

        assertEquals(2, results.size());
        // Pers1
        assertEquals(pers1.getName(), results.get(0).getName());
        assertSubviewCollectionEquals(pers1.getOwnedDocuments(), results.get(0).getOwnedDocuments());

        // Pers1
        assertEquals(pers2.getName(), results.get(1).getName());
        assertSubviewCollectionEquals(pers2.getOwnedDocuments(), results.get(1).getOwnedDocuments());
    }

    private void assertSubviewCollectionEquals(Set<DocumentForCollections> ownedDocuments, Set<? extends SubviewDocumentCollectionsView> ownedSubviewDocuments) {
        assertEquals(ownedDocuments.size(), ownedSubviewDocuments.size());
        for (DocumentForCollections doc : ownedDocuments) {
            boolean found = false;
            for (SubviewDocumentCollectionsView docSub : ownedSubviewDocuments) {
                if (doc.getName().equals(docSub.getName())) {
                    found = true;

                    assertSubviewEquals(doc.getContacts(), docSub.getContacts());
                    assertSubviewEquals(doc.getPartners(), docSub.getPartners());
                    assertSubviewEquals(doc.getPersonList(), docSub.getPersonList());
                    break;
                }
            }

            if (!found) {
                Assert.fail("Could not find a SubviewDocumentCollectionsView with the name: " + doc.getName());
            }
        }
    }
}
