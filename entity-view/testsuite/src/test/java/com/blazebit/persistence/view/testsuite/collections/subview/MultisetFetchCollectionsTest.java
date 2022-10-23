/*
 * Copyright 2014 - 2022 Blazebit.
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

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDB2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.DocumentForCollections;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.PersonForCollections;
import com.blazebit.persistence.view.testsuite.collections.subview.model.PersonForCollectionsMultisetFetchNestedView;
import com.blazebit.persistence.view.testsuite.collections.subview.model.SubviewDocumentCollectionsView;
import com.blazebit.persistence.view.testsuite.collections.subview.model.SubviewDocumentMultisetFetchView;
import com.blazebit.persistence.view.testsuite.collections.subview.model.SubviewPersonForCollectionsMultisetFetchView;
import com.blazebit.persistence.view.testsuite.collections.subview.model.SubviewPersonForCollectionsView;
import com.blazebit.persistence.view.testsuite.collections.subview.model.SubviewSimpleDocumentMultisetFetchView;
import com.blazebit.persistence.view.testsuite.collections.subview.model.variations.PersonForCollectionsMasterView;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class MultisetFetchCollectionsTest extends AbstractEntityViewTest {

    private PersonForCollections pers1;
    private PersonForCollections pers2;

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

                doc1.setOwner(pers1);
                doc2.setOwner(pers1);
                doc3.setOwner(pers1);
                doc4.setOwner(pers1);

                doc1.getPersonList().add(pers1);
                doc1.getPersonList().add(pers2);

                doc1.getContacts().put(1, pers1);
                doc1.getContacts().put(2, pers2);

                em.persist(pers1);
                em.persist(pers2);

                em.persist(doc1);
                em.persist(doc2);
                em.persist(doc3);
                em.persist(doc4);

                pers1.setPartnerDocument(doc1);
                pers2.setPartnerDocument(doc2);
            }
        });
    }

    @Before
    public void setUp() {
        pers1 = cbf.create(em, PersonForCollections.class).where("name").eq("pers1").getSingleResult();
        pers2 = cbf.create(em, PersonForCollections.class).where("name").eq("pers2").getSingleResult();
    }

    // NOTE: DB2 crashes when executing this test with the GROUP_CONCAT based implementation
    // NOTE: EclipseLink can't handle multiple subquery select items... Only one expression can be declared in a SELECT clause of a subquery
    // NOTE: DataNucleus can't handle multiple subquery select items... Number of result expressions in subquery should be 1
    @Test
    @Category({ NoDB2.class, NoDatanucleus.class, NoEclipselink.class })
    public void testCollections() {
        EntityViewManager evm = build(
                PersonForCollectionsMultisetFetchNestedView.class,
                SubviewSimpleDocumentMultisetFetchView.class,
                SubviewDocumentMultisetFetchView.class,
                SubviewPersonForCollectionsMultisetFetchView.class,
                SubviewPersonForCollectionsView.class
        );

        CriteriaBuilder<PersonForCollections> criteria = cbf.create(em, PersonForCollections.class, "p")
            .where("id").in(pers1.getId(), pers2.getId())
            .orderByAsc("id");
        CriteriaBuilder<PersonForCollectionsMultisetFetchNestedView> cb = evm.applySetting(EntityViewSetting.create(PersonForCollectionsMultisetFetchNestedView.class), criteria);
        List<PersonForCollectionsMultisetFetchNestedView> results = cb.getResultList();

        assertEquals(2, results.size());
        // Pers1
        assertEquals(pers1.getName(), results.get(0).getName());
        assertSubviewEquals(pers1.getPartnerDocument(), results.get(0).getPartnerDocument());
        assertSubviewCollectionEquals(pers1.getOwnedDocuments(), results.get(0).getOwnedDocuments());
        assertSubviewEquals(pers1.getPartnerDocument(), results.get(0).getCorrelatedPartnerDocument());
        assertSimpleSubviewCollectionEquals(pers1.getOwnedDocuments(), results.get(0).getCorrelatedOwnedDocuments());

        // Pers2
        assertEquals(pers2.getName(), results.get(1).getName());
        assertSubviewEquals(pers2.getPartnerDocument(), results.get(1).getPartnerDocument());
        assertSubviewCollectionEquals(pers2.getOwnedDocuments(), results.get(1).getOwnedDocuments());
        assertSubviewEquals(pers2.getPartnerDocument(), results.get(1).getCorrelatedPartnerDocument());
        assertSimpleSubviewCollectionEquals(pers2.getOwnedDocuments(), results.get(1).getCorrelatedOwnedDocuments());
    }

    private void assertSubviewCollectionEquals(Set<DocumentForCollections> ownedDocuments, Set<? extends SubviewDocumentMultisetFetchView> ownedSubviewDocuments) {
        assertEquals(ownedDocuments.size(), ownedSubviewDocuments.size());
        for (DocumentForCollections doc : ownedDocuments) {
            boolean found = false;
            for (SubviewDocumentMultisetFetchView docSub : ownedSubviewDocuments) {
                if (doc.getName().equals(docSub.getName())) {
                    found = true;

                    assertCollectionEquals(doc.getPartners(), docSub.getMultisetPartners());
                    assertCollectionEquals(doc.getPartners(), docSub.getJoinedPartners());
                    assertCollectionEquals(doc.getPartners(), docSub.getSelectPartners());
                    assertCollectionEquals(doc.getPartners(), docSub.getSubselectPartners());
                    assertListEquals(doc.getPersonList(), docSub.getPersonList());
                    assertMapEquals(doc.getContacts(), docSub.getContacts());
                    break;
                }
            }

            if (!found) {
                Assert.fail("Could not find a SubviewDocumentCollectionsView with the name: " + doc.getName());
            }
        }
    }

    private void assertMapEquals(Map<Integer, PersonForCollections> contacts, Map<Integer, SubviewPersonForCollectionsMultisetFetchView> subviewContacts) {
        assertEquals(contacts.size(), subviewContacts.size());
        for (Map.Entry<Integer, PersonForCollections> entry : contacts.entrySet()) {
            assertSubviewEquals(entry.getValue(), subviewContacts.get(entry.getKey()));
        }
    }

    private void assertListEquals(List<PersonForCollections> personList, List<SubviewPersonForCollectionsMultisetFetchView> subviewPersonList) {
        assertEquals(personList.size(), subviewPersonList.size());
        for (int i = 0; i < personList.size(); i++) {
            assertSubviewEquals(personList.get(i), subviewPersonList.get(i));
        }
    }

    private void assertCollectionEquals(Set<PersonForCollections> partners, Set<SubviewPersonForCollectionsMultisetFetchView> subviewPartners) {
        assertEquals(partners.size(), subviewPartners.size());
        OUTER: for (PersonForCollections partner : partners) {
            for (SubviewPersonForCollectionsMultisetFetchView subviewPartner : subviewPartners) {
                if (partner.getId().equals(subviewPartner.getId())) {
                    assertSubviewEquals(partner, subviewPartner);
                    continue OUTER;
                }
            }
            Assert.fail("Could not find a SubviewPersonForCollectionsMultisetFetchView with the name: " + partner.getName());
        }
    }

    private void assertSubviewEquals(PersonForCollections value, SubviewPersonForCollectionsMultisetFetchView subview) {
        assertEquals(value.getName(), subview.getName());
        assertEquals(value.getSomeCollection().size(), subview.getSomeCollection().size());
    }

    private void assertSubviewEquals(DocumentForCollections value, SubviewSimpleDocumentMultisetFetchView subview) {
        assertEquals(value.getId(), subview.getId());
        assertEquals(value.getName(), subview.getName());
    }

    private void assertSimpleSubviewCollectionEquals(Set<DocumentForCollections> ownedDocuments, Set<? extends SubviewSimpleDocumentMultisetFetchView> ownedSubviewDocuments) {
        assertEquals(ownedDocuments.size(), ownedSubviewDocuments.size());
        for (DocumentForCollections doc : ownedDocuments) {
            boolean found = false;
            for (SubviewSimpleDocumentMultisetFetchView docSub : ownedSubviewDocuments) {
                if (doc.getName().equals(docSub.getName())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                Assert.fail("Could not find a SubviewSimpleDocumentMultisetFetchView with the name: " + doc.getName());
            }
        }
    }
}
