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

package com.blazebit.persistence.view.testsuite.collections.subview;

import static com.blazebit.persistence.view.testsuite.collections.subview.SubviewAssert.assertSubviewEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.DocumentForCollections;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.PersonForCollections;
import com.blazebit.persistence.view.testsuite.collections.subview.model.SubviewDocumentCollectionsView;
import com.blazebit.persistence.view.testsuite.collections.subview.model.SubviewDocumentListMapSetView;
import com.blazebit.persistence.view.testsuite.collections.subview.model.SubviewDocumentListSetMapView;
import com.blazebit.persistence.view.testsuite.collections.subview.model.SubviewDocumentMapListSetView;
import com.blazebit.persistence.view.testsuite.collections.subview.model.SubviewDocumentMapSetListView;
import com.blazebit.persistence.view.testsuite.collections.subview.model.SubviewDocumentSetListMapView;
import com.blazebit.persistence.view.testsuite.collections.subview.model.SubviewDocumentSetMapListView;
import com.blazebit.persistence.view.testsuite.collections.subview.model.SubviewPersonForCollectionsView;
import com.blazebit.persistence.view.testsuite.collections.subview.model.variations.PersonForCollectionsListMapSetMasterView;
import com.blazebit.persistence.view.testsuite.collections.subview.model.variations.PersonForCollectionsListSetMapMasterView;
import com.blazebit.persistence.view.testsuite.collections.subview.model.variations.PersonForCollectionsMapListSetMasterView;
import com.blazebit.persistence.view.testsuite.collections.subview.model.variations.PersonForCollectionsMapSetListMasterView;
import com.blazebit.persistence.view.testsuite.collections.subview.model.variations.PersonForCollectionsMasterView;
import com.blazebit.persistence.view.testsuite.collections.subview.model.variations.PersonForCollectionsSetListMapMasterView;
import com.blazebit.persistence.view.testsuite.collections.subview.model.variations.PersonForCollectionsSetMapListMasterView;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@RunWith(Parameterized.class)
public class NestedCollectionsTest<T extends PersonForCollectionsMasterView, U extends SubviewDocumentCollectionsView> extends AbstractEntityViewTest {

    private final Class<T> viewType;
    private final Class<U> subviewType;

    private PersonForCollections pers1;
    private PersonForCollections pers2;

    public NestedCollectionsTest(Class<T> viewType, Class<U> subviewType) {
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

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                DocumentForCollections doc1 = new DocumentForCollections("doc1");
                DocumentForCollections doc2 = new DocumentForCollections("doc2");
                DocumentForCollections doc3 = new DocumentForCollections("doc3");
                DocumentForCollections doc4 = new DocumentForCollections("doc4");

                pers1 = new PersonForCollections("pers1");
                pers2 = new PersonForCollections("pers2");
                pers1.setPartnerDocument(doc1);
                pers2.setPartnerDocument(doc2);

                PersonForCollections o1 = new PersonForCollections("pers1");
                PersonForCollections o2 = new PersonForCollections("pers2");
                PersonForCollections o3 = new PersonForCollections("pers3");
                PersonForCollections o4 = new PersonForCollections("pers4");
                o1.setPartnerDocument(doc1);
                o2.setPartnerDocument(doc2);
                o3.setPartnerDocument(doc3);
                o4.setPartnerDocument(doc4);

                doc1.setOwner(pers1);
                doc2.setOwner(pers2);
                doc3.setOwner(pers1);
                doc4.setOwner(pers2);

                doc1.getContacts().put(1, o1);
                doc2.getContacts().put(1, o2);
                doc1.getContacts().put(2, o3);
                doc2.getContacts().put(2, o4);

                em.persist(pers1);
                em.persist(pers2);

                em.persist(o1);
                em.persist(o2);
                em.persist(o3);
                em.persist(o4);

                doc1.getPartners().add(o1);
                doc1.getPartners().add(o3);
                doc2.getPartners().add(o2);
                doc2.getPartners().add(o4);

                doc1.getPersonList().add(o1);
                doc1.getPersonList().add(o2);
                doc2.getPersonList().add(o3);
                doc2.getPersonList().add(o4);

                em.persist(doc1);
                em.persist(doc2);
                em.persist(doc3);
                em.persist(doc4);
            }
        });
    }

    @Before
    public void setUp() {
        pers1 = cbf.create(em, PersonForCollections.class).from(DocumentForCollections.class).distinct().select("owner").where("owner.name").eq("pers1").getSingleResult();
        pers2 = cbf.create(em, PersonForCollections.class).from(DocumentForCollections.class).distinct().select("owner").where("owner.name").eq("pers2").getSingleResult();
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
        EntityViewManager evm = cfg.createEntityViewManager(cbf);

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
