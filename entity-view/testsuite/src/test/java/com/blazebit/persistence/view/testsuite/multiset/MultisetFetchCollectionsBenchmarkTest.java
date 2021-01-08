/*
 * Copyright 2014 - 2021 Blazebit.
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

package com.blazebit.persistence.view.testsuite.multiset;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.MutablePersistenceUnitInfo;
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
import com.blazebit.persistence.view.testsuite.multiset.model.PersonForCollectionsMultisetFetchNestedView;
import com.blazebit.persistence.view.testsuite.multiset.model.SubviewDocumentMultisetFetchView;
import com.blazebit.persistence.view.testsuite.multiset.model.SubviewPersonForCollectionsMultisetFetchView;
import com.blazebit.persistence.view.testsuite.multiset.model.SubviewPersonForCollectionsView;
import com.blazebit.persistence.view.testsuite.multiset.model.join.PersonForCollectionsMultisetFetchNestedViewJoin;
import com.blazebit.persistence.view.testsuite.multiset.model.join.SubviewDocumentMultisetFetchViewJoin;
import com.blazebit.persistence.view.testsuite.multiset.model.join.SubviewPersonForCollectionsMultisetFetchViewJoin;
import com.blazebit.persistence.view.testsuite.multiset.model.multiset.PersonForCollectionsMultisetFetchNestedViewMultiset;
import com.blazebit.persistence.view.testsuite.multiset.model.multiset.SubviewDocumentMultisetFetchViewMultiset;
import com.blazebit.persistence.view.testsuite.multiset.model.multiset.SubviewPersonForCollectionsMultisetFetchViewMultiset;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestRule;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class MultisetFetchCollectionsBenchmarkTest extends AbstractEntityViewTest {

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    private EntityViewManager joinEvm;
    private EntityViewManager multisetEvm;
    private PersonForCollections pers1;

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
            DocumentForCollections.class,
            PersonForCollections.class
        };
    }

    @Override
    protected void configurePersistenceUnitInfo(MutablePersistenceUnitInfo persistenceUnitInfo) {
        persistenceUnitInfo.getProperties().setProperty("hibernate.generate_statistics", "true");
        super.configurePersistenceUnitInfo(persistenceUnitInfo);
    }

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                List<DocumentForCollections> docs = new ArrayList<>();

                for (int i = 1; i <= 20; i++) {
                    docs.add(new DocumentForCollections("doc" + i));
                }

                DocumentForCollections doc1 = docs.get(0);
                pers1 = new PersonForCollections("pers1");

                for (DocumentForCollections doc : docs) {
                    doc.setOwner(pers1);
                }

                doc1.getPersonList().add(pers1);
                for (int i = 2; i <= 20; i++) {
                    doc1.getPersonList().add(new PersonForCollections("listPers" + i));
                }

                doc1.getContacts().put(1, pers1);
                for (int i = 2; i <= 20; i++) {
                    doc1.getContacts().put(i, new PersonForCollections("contactsPers" + i));
                }

                em.persist(pers1);
                for (PersonForCollections personForCollections : doc1.getPersonList()) {
                    em.persist(personForCollections);
                }
                for (PersonForCollections personForCollections : doc1.getContacts().values()) {
                    em.persist(personForCollections);
                }

                for (DocumentForCollections doc : docs) {
                    em.persist(doc);
                }

                pers1.setPartnerDocument(doc1);
                for (int i = 2; i <= 20; i++) {
                    PersonForCollections partner = new PersonForCollections("partnerPers" + i);
                    partner.setPartnerDocument(doc1);
                    em.persist(partner);
                }
            }
        });
    }

    @Before
    public void setUp() {
        pers1 = cbf.create(em, PersonForCollections.class).where("name").eq("pers1").getSingleResult();

        joinEvm = build(
                PersonForCollectionsMultisetFetchNestedViewJoin.class,
                SubviewDocumentMultisetFetchViewJoin.class,
                SubviewPersonForCollectionsMultisetFetchViewJoin.class,
                SubviewPersonForCollectionsView.class
        );

        multisetEvm = build(
                PersonForCollectionsMultisetFetchNestedViewMultiset.class,
                SubviewDocumentMultisetFetchViewMultiset.class,
                SubviewPersonForCollectionsMultisetFetchViewMultiset.class,
                SubviewPersonForCollectionsView.class
        );
    }

    // NOTE: DB2 crashes when executing this test with the GROUP_CONCAT based implementation
    // NOTE: EclipseLink cant' handle multiple subquery select items... Only one expression can be declared in a SELECT clause of a subquery
    // NOTE: DataNucleus cant' handle multiple subquery select items... Number of result expressions in subquery should be 1
    @Test
    @Category({ NoDB2.class, NoDatanucleus.class, NoEclipselink.class })
    @BenchmarkOptions(benchmarkRounds = 5, warmupRounds = 4)
    public void testJoinFetch() {
        testCollections(joinEvm, PersonForCollectionsMultisetFetchNestedViewJoin.class);
    }

    // NOTE: DB2 crashes when executing this test with the GROUP_CONCAT based implementation
    // NOTE: EclipseLink cant' handle multiple subquery select items... Only one expression can be declared in a SELECT clause of a subquery
    // NOTE: DataNucleus cant' handle multiple subquery select items... Number of result expressions in subquery should be 1
    @Test
    @Category({ NoDB2.class, NoDatanucleus.class, NoEclipselink.class })
    @BenchmarkOptions(benchmarkRounds = 5, warmupRounds = 4)
    public void testMultisetFetch() {
        testCollections(multisetEvm, PersonForCollectionsMultisetFetchNestedViewMultiset.class);
    }

    private void testCollections(EntityViewManager evm, Class<? extends PersonForCollectionsMultisetFetchNestedView> view) {
        CriteriaBuilder<PersonForCollections> criteria = cbf.create(em, PersonForCollections.class, "p")
            .where("id").in(pers1.getId())
            .orderByAsc("id");
        List<? extends PersonForCollectionsMultisetFetchNestedView> results = evm.applySetting(EntityViewSetting.create(view).withOptionalParameter("test", Locale.ENGLISH), criteria).getResultList();

        assertEquals(1, results.size());
        assertSubviewCollectionEquals(pers1.getOwnedDocuments(), results.get(0).getOwnedDocuments());
    }

    private String find(String[] queries, String needle) {
        for (String query : queries) {
            if (query.contains(needle)) {
                return query;
            }
        }

        return null;
    }

    private void assertSubviewCollectionEquals(Set<DocumentForCollections> ownedDocuments, Set<? extends SubviewDocumentMultisetFetchView> ownedSubviewDocuments) {
        assertEquals(ownedDocuments.size(), ownedSubviewDocuments.size());
        for (DocumentForCollections doc : ownedDocuments) {
            boolean found = false;
            for (SubviewDocumentMultisetFetchView docSub : ownedSubviewDocuments) {
                if (doc.getName().equals(docSub.getName())) {
                    found = true;

                    assertCollectionEquals(doc.getPartners(), docSub.getPartners());
                    assertListEquals(doc.getPersonList(), docSub.getPersonList());
                    assertMapEquals(doc.getContacts(), docSub.getContacts());
                    assertEquals(Locale.ENGLISH, docSub.getTest());
                    break;
                }
            }

            if (!found) {
                Assert.fail("Could not find a SubviewDocumentCollectionsView with the name: " + doc.getName());
            }
        }
    }

    private void assertMapEquals(Map<Integer, PersonForCollections> contacts, Map<Integer, ? extends SubviewPersonForCollectionsMultisetFetchView> subviewContacts) {
        assertEquals(contacts.size(), subviewContacts.size());
        for (Map.Entry<Integer, PersonForCollections> entry : contacts.entrySet()) {
            assertSubviewEquals(entry.getValue(), subviewContacts.get(entry.getKey()));
        }
    }

    private void assertListEquals(List<PersonForCollections> personList, List<? extends SubviewPersonForCollectionsMultisetFetchView> subviewPersonList) {
        assertEquals(personList.size(), subviewPersonList.size());
        for (int i = 0; i < personList.size(); i++) {
            assertSubviewEquals(personList.get(i), subviewPersonList.get(i));
        }
    }

    private void assertCollectionEquals(Set<PersonForCollections> partners, Set<? extends SubviewPersonForCollectionsMultisetFetchView> subviewPartners) {
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
}
