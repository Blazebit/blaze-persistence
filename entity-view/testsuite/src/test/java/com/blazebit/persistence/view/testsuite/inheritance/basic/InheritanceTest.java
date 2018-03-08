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

package com.blazebit.persistence.view.testsuite.inheritance.basic;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.view.testsuite.inheritance.basic.model.DocumentBaseView;
import com.blazebit.persistence.view.testsuite.inheritance.basic.model.NewDocumentView;
import com.blazebit.persistence.view.testsuite.inheritance.basic.model.NewSub1DocumentView;
import com.blazebit.persistence.view.testsuite.inheritance.basic.model.NewSub1Sub1DocumentView;
import com.blazebit.persistence.view.testsuite.inheritance.basic.model.NewSub2DocumentView;
import com.blazebit.persistence.view.testsuite.inheritance.basic.model.OldDocumentView;
import com.blazebit.persistence.view.testsuite.inheritance.basic.model.SimplePersonSubView;
import com.blazebit.persistence.view.testsuite.inheritance.basic.model.UnusedOldDocumentView;
import com.blazebit.persistence.view.testsuite.inheritance.basic.model.UsedOldDocumentView;
import com.blazebit.persistence.view.testsuite.inheritance.basic.model.UsedOldSub1DocumentView;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class InheritanceTest extends AbstractEntityViewTest {

    private Document doc1;
    private Document doc2;
    private Document doc3;
    private Document doc4;
    private Document doc5;
    private Document doc6;
    private EntityViewManager evm;

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                doc1 = new Document("doc1", new Person("owner1"), new Version(), new Version());
                doc2 = new Document("doc2", new Person("owner2"));
                doc3 = new Document("doc3", new Person("owner3"));
                doc4 = new Document("doc4", new Person("owner4"), new Version(), new Version());
                doc5 = new Document("doc5", new Person("owner5"));
                doc6 = new Document("doc6", new Person("owner6"));

                // New
                doc1.setAge(1);
                doc2.setAge(1);
                doc3.setAge(1);

                // Base
                doc4.setAge(15);

                // Old
                doc5.setAge(16);
                doc6.setAge(16);

                // NewSub1
                doc1.setDefaultContact(null);
                doc2.setDefaultContact(null);
                // NewSub2
                doc3.setDefaultContact(1);

                // Base
                doc4.setDefaultContact(1);
                doc4.setLastModified(Timestamp.valueOf("2000-01-01 00:00:00"));

                // UsedOld
                doc5.setLastModified(Timestamp.valueOf("2000-01-01 00:00:00"));
                // Old
                doc6.setLastModified(null);

                Person o1 = new Person("pers1");
                Person o2 = new Person("pers2");
                Person o3 = new Person("pers3");
                Person o4 = new Person("pers4");
                Person o5 = new Person("pers5");
                Person o6 = new Person("pers6");

                doc2.getContacts().put(1, o1);
                doc2.getContacts().put(1, o2);
                doc5.getContacts().put(1, o3);
                doc5.getContacts().put(1, o4);
                doc6.getContacts().put(1, o5);
                doc6.getContacts().put(1, o6);

                em.persist(o1);
                em.persist(o2);
                em.persist(o3);
                em.persist(o4);
                em.persist(o5);
                em.persist(o6);

                o1.setPartnerDocument(doc3);
                o2.setPartnerDocument(doc3);
                o3.setPartnerDocument(doc4);
                o4.setPartnerDocument(doc4);
                o5.setPartnerDocument(doc5);
                o6.setPartnerDocument(doc5);

                doc1.getPeople().add(o1);
                doc2.getPeople().add(o2);
                doc3.getPeople().add(o3);
                doc4.getPeople().add(o4);
                doc5.getPeople().add(o5);
                doc6.getPeople().add(o6);

                em.persist(doc1);
                em.persist(doc2);
                em.persist(doc3);
                em.persist(doc4);
                em.persist(doc5);
                em.persist(doc6);
            }
        });
    }

    @Before
    public void setUp() {
        doc1 = cbf.create(em, Document.class).where("name").eq("doc1").getSingleResult();
        doc2 = cbf.create(em, Document.class).where("name").eq("doc2").getSingleResult();
        doc3 = cbf.create(em, Document.class).where("name").eq("doc3").getSingleResult();
        doc4 = cbf.create(em, Document.class).where("name").eq("doc4").getSingleResult();
        doc5 = cbf.create(em, Document.class).where("name").eq("doc5").getSingleResult();
        doc6 = cbf.create(em, Document.class).where("name").eq("doc6").getSingleResult();

        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(SimplePersonSubView.class);
        cfg.addEntityView(DocumentBaseView.class);
        cfg.addEntityView(NewDocumentView.class);
        cfg.addEntityView(NewSub1DocumentView.class);
        cfg.addEntityView(NewSub2DocumentView.class);
        cfg.addEntityView(OldDocumentView.class);
        cfg.addEntityView(UnusedOldDocumentView.class);
        cfg.addEntityView(UsedOldDocumentView.class);
        cfg.addEntityView(NewSub1Sub1DocumentView.class);
        cfg.addEntityView(UsedOldSub1DocumentView.class);
        this.evm = cfg.createEntityViewManager(cbf);
    }

    @Test
    public void inheritanceMetamodel() {
        ManagedViewType<?> baseViewType = evm.getMetamodel().managedView(DocumentBaseView.class);
        ManagedViewType<?> newViewType = evm.getMetamodel().managedView(NewDocumentView.class);
        ManagedViewType<?> newSub1ViewType = evm.getMetamodel().managedView(NewSub1DocumentView.class);
        ManagedViewType<?> newSub1Sub1ViewType = evm.getMetamodel().managedView(NewSub1Sub1DocumentView.class);
        ManagedViewType<?> newSub2ViewType = evm.getMetamodel().managedView(NewSub2DocumentView.class);
        ManagedViewType<?> oldViewType = evm.getMetamodel().managedView(OldDocumentView.class);
        ManagedViewType<?> usedOldViewType = evm.getMetamodel().managedView(UsedOldDocumentView.class);
        ManagedViewType<?> usedOldSubViewType = evm.getMetamodel().managedView(UsedOldSub1DocumentView.class);
        ManagedViewType<?> unusedOldViewType = evm.getMetamodel().managedView(UnusedOldDocumentView.class);

        assertEquals(null, baseViewType.getInheritanceMapping());
        assertEquals(6, baseViewType.getInheritanceSubtypes().size());
        assertTrue(baseViewType.getInheritanceSubtypes().contains(baseViewType));
        assertTrue(baseViewType.getInheritanceSubtypes().contains(newViewType));
        assertTrue(baseViewType.getInheritanceSubtypes().contains(newSub1ViewType));
        assertFalse(baseViewType.getInheritanceSubtypes().contains(newSub1Sub1ViewType));
        assertTrue(baseViewType.getInheritanceSubtypes().contains(newSub2ViewType));
        assertTrue(baseViewType.getInheritanceSubtypes().contains(oldViewType));
        assertTrue(baseViewType.getInheritanceSubtypes().contains(usedOldViewType));
        assertFalse(baseViewType.getInheritanceSubtypes().contains(usedOldSubViewType));
        assertFalse(baseViewType.getInheritanceSubtypes().contains(unusedOldViewType));

        assertEquals("age < 15", newViewType.getInheritanceMapping());
        assertEquals(3, newViewType.getInheritanceSubtypes().size());
        assertTrue(newViewType.getInheritanceSubtypes().contains(newViewType));
        assertTrue(newViewType.getInheritanceSubtypes().contains(newSub1ViewType));
        assertTrue(newViewType.getInheritanceSubtypes().contains(newSub2ViewType));

        assertEquals("defaultContact IS NULL", newSub1ViewType.getInheritanceMapping());
        assertEquals(1, newSub1ViewType.getInheritanceSubtypes().size());
        assertTrue(newSub1ViewType.getInheritanceSubtypes().contains(newSub1ViewType));

        assertEquals("defaultContact IS NOT NULL", newSub2ViewType.getInheritanceMapping());
        assertEquals(1, newSub2ViewType.getInheritanceSubtypes().size());
        assertTrue(newSub2ViewType.getInheritanceSubtypes().contains(newSub2ViewType));

        assertEquals("age > 15", oldViewType.getInheritanceMapping());
        assertEquals(2, oldViewType.getInheritanceSubtypes().size());
        assertTrue(oldViewType.getInheritanceSubtypes().contains(oldViewType));
        assertTrue(oldViewType.getInheritanceSubtypes().contains(usedOldViewType));

        assertEquals("lastModified IS NOT NULL", usedOldViewType.getInheritanceMapping());
        assertEquals(1, usedOldViewType.getInheritanceSubtypes().size());
        assertTrue(usedOldViewType.getInheritanceSubtypes().contains(usedOldViewType));
    }

    @Test
    // TODO: report that datanucleus thinks a NULL literal is of type Integer and normal integral literals are of type Long
    @Category({ NoDatanucleus.class })
    public void inheritanceQuery() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d")
            .orderByAsc("id");
        CriteriaBuilder<DocumentBaseView> cb = evm.applySetting(EntityViewSetting.create(DocumentBaseView.class), criteria);
        List<DocumentBaseView> results = cb.getResultList();

        assertEquals(6, results.size());
        assertTypeMatches(results.get(0), evm, DocumentBaseView.class, NewSub1DocumentView.class);
        assertTypeMatches(results.get(1), evm, DocumentBaseView.class, NewSub1DocumentView.class);
        assertTypeMatches(results.get(2), evm, DocumentBaseView.class, NewSub2DocumentView.class);
        assertTypeMatches(results.get(3), evm, DocumentBaseView.class, DocumentBaseView.class);
        assertTypeMatches(results.get(4), evm, DocumentBaseView.class, UsedOldDocumentView.class);
        assertTypeMatches(results.get(5), evm, DocumentBaseView.class, OldDocumentView.class);

        NewSub1DocumentView docView1 = (NewSub1DocumentView) results.get(0);
        NewSub1DocumentView docView2 = (NewSub1DocumentView) results.get(1);
        NewSub2DocumentView docView3 = (NewSub2DocumentView) results.get(2);
        DocumentBaseView docView4 = results.get(3);
        UsedOldDocumentView docView5 = (UsedOldDocumentView) results.get(4);
        OldDocumentView docView6 = (OldDocumentView) results.get(5);

        assertDocumentEquals(doc1, docView1);
        assertDocumentEquals(doc2, docView2);
        assertDocumentEquals(doc3, docView3);
        assertDocumentEquals(doc4, docView4);
        assertDocumentEquals(doc5, docView5);
        assertDocumentEquals(doc6, docView6);

        assertSubviewEquals(doc1.getContacts().values(), docView1.getContacts());
        assertVersionsEquals(doc1.getVersions(), docView1.getVersionIds());
        assertSubviewEquals(doc2.getContacts().values(), docView2.getContacts());
        assertVersionsEquals(doc2.getVersions(), docView2.getVersionIds());

        assertEquals(doc3.getDefaultContact(), docView3.getDefaultContact());

        assertSubviewEquals(doc5.getContacts().values(), docView5.getContacts());
        assertSubviewEquals(doc5.getPartners(), docView5.getPartners());
        assertSubviewEquals(doc6.getPartners(), docView6.getPartners());
    }

    public static <T> void assertTypeMatches(T o, EntityViewManager evm, Class<T> baseType, Class<? extends T> subtype) {
        assertEquals(baseType.getName() + "_" + subtype.getSimpleName() + "_$$_javassist_entityview_", o.getClass().getName());
    }

    public static void assertDocumentEquals(Document doc, DocumentBaseView view) {
        assertEquals(doc.getId(), view.getId());
        assertEquals(doc.getName(), view.getName());
    }

    public static void assertVersionsEquals(Set<Version> versions, Set<Long> versionIds) {
        if (versions == null) {
            assertNull(versionIds);
            return;
        }

        assertNotNull(versionIds);
        assertEquals(versions.size(), versionIds.size());
        for (Version v : versions) {
            if (!versionIds.contains(v.getId())) {
                Assert.fail("Could not find a version id: " + v.getId());
            }
        }
    }

    public static void assertSubviewEquals(Collection<Person> persons, Set<SimplePersonSubView> personSubviews) {
        if (persons == null) {
            assertNull(personSubviews);
            return;
        }

        assertNotNull(personSubviews);
        assertEquals(persons.size(), personSubviews.size());
        for (Person p : persons) {
            boolean found = false;
            for (SimplePersonSubView pSub : personSubviews) {
                if (p.getName().equals(pSub.getName())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                Assert.fail("Could not find a SimplePersonSubView with the name: " + p.getName());
            }
        }
    }
}
