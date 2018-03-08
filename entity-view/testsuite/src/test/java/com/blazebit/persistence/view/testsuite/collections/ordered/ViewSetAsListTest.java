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

package com.blazebit.persistence.view.testsuite.collections.ordered;

import static org.junit.Assert.assertEquals;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.EntityManager;

import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.DocumentForCollections;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.PersonForCollections;
import com.blazebit.persistence.view.testsuite.collections.ordered.model.BaseDocumentView;
import com.blazebit.persistence.view.testsuite.collections.ordered.model.DocumentWithSetAsListView;
import com.blazebit.persistence.view.testsuite.collections.ordered.model.PersonForCollectionsView;
import com.blazebit.persistence.view.testsuite.collections.ordered.model.PersonWithSetAsListView;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ViewSetAsListTest extends AbstractEntityViewTest {

    private PersonForCollections pers1;
    private PersonForCollections pers2;

    private DocumentForCollections d1;
    private DocumentForCollections d2;
    
    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
            DocumentForCollections.class,
            PersonForCollections.class
        };
    }

    @Before
    public void setUp() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                DocumentForCollections doc1 = new DocumentForCollections("doc1");
                DocumentForCollections doc2 = new DocumentForCollections("doc2");
                DocumentForCollections doc3 = new DocumentForCollections("doc3");
                DocumentForCollections doc4 = new DocumentForCollections("doc4");

                pers1 = new PersonForCollections("p1");
                pers2 = new PersonForCollections("p2");
                pers1.setPartnerDocument(doc1);
                pers2.setPartnerDocument(doc2);

                d1 = new DocumentForCollections("d1");
                d2 = new DocumentForCollections("d2");
                d1.setOwner(pers1);
                d2.setOwner(pers2);

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

                em.persist(pers1);
                em.persist(pers2);
                em.persist(d1);
                em.persist(d2);

                em.persist(doc1);
                em.persist(doc2);
                em.persist(doc3);
                em.persist(doc4);

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

            }
        });

        pers1 = em.find(PersonForCollections.class, pers1.getId());
        pers2 = em.find(PersonForCollections.class, pers2.getId());
    }

    @Test
    public void testCollections() {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(BaseDocumentView.class);
        cfg.addEntityView(PersonWithSetAsListView.class);
        cfg.addEntityView(DocumentWithSetAsListView.class);
        cfg.addEntityView(PersonForCollectionsView.class);
        EntityViewManager evm = cfg.createEntityViewManager(cbf);

        CriteriaBuilder<DocumentForCollections> criteria = cbf.create(em, DocumentForCollections.class, "d")
            .where("id").in(d1.getId(), d2.getId())
            .orderByAsc("d.owner.name")
            .orderByDesc("d.owner.ownedDocuments.name")
            .orderByDesc("d.owner.ownedDocuments.partners.name");
        CriteriaBuilder<BaseDocumentView> cb = evm.applySetting(EntityViewSetting.create(BaseDocumentView.class), criteria);
        List<BaseDocumentView> results = cb.getResultList();

        assertEquals(2, results.size());
        // Pers1
        assertEquals(d1.getName(), results.get(0).getName());
        assertSubviewCollectionEquals(pers1.getOwnedDocuments(), results.get(0).getOwner().getOwnedDocuments());

        // Pers2
        assertEquals(d2.getName(), results.get(1).getName());
        assertSubviewCollectionEquals(pers2.getOwnedDocuments(), results.get(1).getOwner().getOwnedDocuments());
    }

    private void assertSubviewCollectionEquals(Set<DocumentForCollections> ownedDocuments, List<? extends DocumentWithSetAsListView> ownedSubviewDocuments) {
        assertEquals(ownedDocuments.size(), ownedSubviewDocuments.size());
        DocumentForCollections[] sortedDocuments = sortedByDocumentName(ownedDocuments);
        for (int i = 0; i < sortedDocuments.length; i++) {
            DocumentForCollections doc = sortedDocuments[i];
            if (!doc.getName().equals(ownedSubviewDocuments.get(i).getName())) {
                Assert.fail("Could not find a DocumentWithSetAsListView with the name: " + doc.getName());
            }
            
            assertSubviewPersonCollectionEquals(doc.getPartners(), ownedSubviewDocuments.get(i).getPartners());
        }
    }

    private void assertSubviewPersonCollectionEquals(Set<PersonForCollections> partnerPersons, List<? extends PersonForCollectionsView> persons) {
        assertEquals(partnerPersons.size(), persons.size());
        PersonForCollections[] sortedPersons = sortedByPersonName(partnerPersons);
        for (int i = 0; i < sortedPersons.length; i++) {
            PersonForCollections pers = sortedPersons[i];
            if (!pers.getName().equals(persons.get(i).getName())) {
                Assert.fail("Could not find a PersonForCollectionsView with the name: " + pers.getName());
            }
        }
    }

    private DocumentForCollections[] sortedByDocumentName(Set<DocumentForCollections> ownedDocuments) {
        Set<DocumentForCollections> set = new TreeSet<DocumentForCollections>(new Comparator<DocumentForCollections>() {

            @Override
            public int compare(DocumentForCollections o1, DocumentForCollections o2) {
                return o2.getName().compareTo(o1.getName());
            }
            
        });
        
        set.addAll(ownedDocuments);
        return set.toArray(new DocumentForCollections[set.size()]);
    }

    private PersonForCollections[] sortedByPersonName(Set<PersonForCollections> persons) {
        Set<PersonForCollections> set = new TreeSet<PersonForCollections>(new Comparator<PersonForCollections>() {

            @Override
            public int compare(PersonForCollections o1, PersonForCollections o2) {
                return o2.getName().compareTo(o1.getName());
            }
            
        });
        
        set.addAll(persons);
        return set.toArray(new PersonForCollections[set.size()]);
    }
}
