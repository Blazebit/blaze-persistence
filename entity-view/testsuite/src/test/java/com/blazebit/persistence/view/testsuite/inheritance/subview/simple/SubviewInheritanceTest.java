/*
 * Copyright 2014 - 2023 Blazebit.
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

package com.blazebit.persistence.view.testsuite.inheritance.subview.simple;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.testsuite.inheritance.subview.simple.model.DocumentView1;
import com.blazebit.persistence.view.testsuite.inheritance.subview.simple.model.DocumentView3;
import com.blazebit.persistence.view.testsuite.inheritance.subview.simple.model.OldPersonView1;
import com.blazebit.persistence.view.testsuite.inheritance.subview.simple.model.OldPersonView2;
import com.blazebit.persistence.view.testsuite.inheritance.subview.simple.model.OldPersonView3;
import com.blazebit.persistence.view.testsuite.inheritance.subview.simple.model.PersonBaseView;
import com.blazebit.persistence.view.testsuite.inheritance.subview.simple.model.PersonBaseView1;
import com.blazebit.persistence.view.testsuite.inheritance.subview.simple.model.PersonBaseView2;
import com.blazebit.persistence.view.testsuite.inheritance.subview.simple.model.PersonBaseView3;
import com.blazebit.persistence.view.testsuite.inheritance.subview.simple.model.SimpleDocumentView;
import com.blazebit.persistence.view.testsuite.inheritance.subview.simple.model.SimplePersonSubView;
import com.blazebit.persistence.view.testsuite.inheritance.subview.simple.model.DocumentView2;
import com.blazebit.persistence.view.testsuite.inheritance.subview.simple.model.YoungPersonView1;
import com.blazebit.persistence.view.testsuite.inheritance.subview.simple.model.YoungPersonView2;
import com.blazebit.persistence.view.testsuite.inheritance.subview.simple.model.YoungPersonView3;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * The general idea is, that for every base view, we create a separate proxy.
 * The inheritance base provides an overall constructor containing attributes of all subtypes.
 * Every type has a static create method that maps the overall attributes to the subtype specific constructor.
 *
 * * public interface PersonBaseView {
 *     @IdMapping
 *     public Long getId();
 *     public String getName();
 * }
 *
 * @EntityView(Person.class)
 * @EntityViewInheritance({ YoungPersonView1.class, OldPersonView1.class })
 * public interface PersonBaseView1 extends PersonBaseView {
 * }
 *
 * @EntityView(Person.class)
 * @EntityViewInheritanceMapping("age > 15")
 * public interface OldPersonView1 extends PersonBaseView1 {
 *     public SimpleDocumentView getPartnerDocument();
 * }
 *
 * @EntityView(Person.class)
 * @EntityViewInheritanceMapping("age < 15")
 * public interface YoungPersonView1 extends PersonBaseView1 {
 *     public SimplePersonSubView getFriend();
 * }
 *
 * -----------------------------
 *
 * public class PersonBaseView1Impl implements PersonBaseView1 {
 *     PersonBaseView1Impl(EntityViewManager evm) {}
 *     PersonBaseView1Impl(Long id) {}
 *     PersonBaseView1Impl(Long id, String name) {}
 *     PersonBaseView1Impl(Long id, String name, SimpleDocumentView partnerDocument, SimplePersonSubView friend) {}
 *
 *     PersonBaseView1Impl create(Long id, String name, SimpleDocumentView partnerDocument, SimplePersonSubView friend) {}
 * }
 *
 * public class OldPersonView1Impl extends OldPersonView1 {
 *     OldPersonView1Impl(EntityViewManager evm) {}
 *     OldPersonView1Impl(Long id) {}
 *     OldPersonView1Impl(Long id, String name, SimpleDocumentView partnerDocument) {}
 *
 *     OldPersonView1Impl create(Long id, String name, SimpleDocumentView partnerDocument, SimplePersonSubView friend) {}
 * }
 *
 * public class YoungPersonView1Impl extends YoungPersonView1 {
 *     YoungPersonView1Impl(EntityViewManager evm) {}
 *     YoungPersonView1Impl(Long id) {}
 *     YoungPersonView1Impl(Long id, String name, SimplePersonSubView friend) {}
 *
 *     YoungPersonView1Impl create(Long id, String name, SimpleDocumentView partnerDocument, SimplePersonSubView friend) {}
 * }
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SubviewInheritanceTest extends AbstractEntityViewTest {

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
                Person o1 = new Person("pers1", 1);
                Person o2 = new Person("pers2", 1);
                Person o3 = new Person("pers3", 15);
                Person o4 = new Person("pers4", 15);
                Person o5 = new Person("pers5", 16);
                Person o6 = new Person("pers6", 16);

                doc1 = new Document("doc1", o1);
                doc2 = new Document("doc2", o2);
                doc3 = new Document("doc3", o3);
                doc4 = new Document("doc4", o4);
                doc5 = new Document("doc5", o5);
                doc6 = new Document("doc6", o6);

                em.persist(o1);
                em.persist(o2);
                em.persist(o3);
                em.persist(o4);
                em.persist(o5);
                em.persist(o6);

                em.persist(doc1);
                em.persist(doc2);
                em.persist(doc3);
                em.persist(doc4);
                em.persist(doc5);
                em.persist(doc6);

                o1.setFriend(o2);
                o2.setPartnerDocument(doc1);
                o3.setFriend(o4);
                o4.setPartnerDocument(doc3);
                o5.setFriend(o6);
                o6.setPartnerDocument(doc5);
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
    }

    @Test
    public void inheritanceQuery() {
        this.evm = build(
                DocumentView1.class,
                SimpleDocumentView.class,
                SimplePersonSubView.class,
                PersonBaseView1.class,
                OldPersonView1.class,
                YoungPersonView1.class
        );

        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d")
            .orderByAsc("id");
        CriteriaBuilder<DocumentView1> cb = evm.applySetting(EntityViewSetting.create(DocumentView1.class), criteria);
        List<DocumentView1> results = cb.getResultList();

        assertEquals(6, results.size());

        assertDocumentEquals(doc1, results.get(0));
        assertDocumentEquals(doc2, results.get(1));
        assertDocumentEquals(doc3, results.get(2));
        assertDocumentEquals(doc4, results.get(3));
        assertDocumentEquals(doc5, results.get(4));
        assertDocumentEquals(doc6, results.get(5));

        assertTypeMatches(results.get(0).getOwner(), evm, PersonBaseView1.class, YoungPersonView1.class);
        assertTypeMatches(results.get(1).getOwner(), evm, PersonBaseView1.class, YoungPersonView1.class);
        assertTypeMatches(results.get(2).getOwner(), evm, PersonBaseView1.class, PersonBaseView1.class);
        assertTypeMatches(results.get(3).getOwner(), evm, PersonBaseView1.class, PersonBaseView1.class);
        assertTypeMatches(results.get(4).getOwner(), evm, PersonBaseView1.class, OldPersonView1.class);
        assertTypeMatches(results.get(5).getOwner(), evm, PersonBaseView1.class, OldPersonView1.class);

        YoungPersonView1 persView1 = (YoungPersonView1) results.get(0).getOwner();
        YoungPersonView1 persView2 = (YoungPersonView1) results.get(1).getOwner();
        PersonBaseView persView3 = results.get(2).getOwner();
        PersonBaseView persView4 = results.get(3).getOwner();
        OldPersonView1 persView5 = (OldPersonView1) results.get(4).getOwner();
        OldPersonView1 persView6 = (OldPersonView1) results.get(5).getOwner();

        assertPersonEquals(doc1.getOwner(), persView1);
        assertPersonEquals(doc2.getOwner(), persView2);
        assertPersonEquals(doc3.getOwner(), persView3);
        assertPersonEquals(doc4.getOwner(), persView4);
        assertPersonEquals(doc5.getOwner(), persView5);
        assertPersonEquals(doc6.getOwner(), persView6);

        assertPersonEquals(doc1.getOwner().getFriend(), persView1.getFriend());
        assertNull(persView2.getFriend());

        assertNull(persView5.getPartnerDocument());
        assertDocumentEquals(doc5, persView6.getPartnerDocument());
    }

    @Test
    public void inheritanceQuerySubviewInheritanceMapping() {
        this.evm = build(
                DocumentView2.class,
                SimpleDocumentView.class,
                SimplePersonSubView.class,
                PersonBaseView2.class,
                OldPersonView2.class,
                YoungPersonView2.class
        );

        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d")
                .orderByAsc("id");
        CriteriaBuilder<DocumentView2> cb = evm.applySetting(EntityViewSetting.create(DocumentView2.class), criteria);
        List<DocumentView2> results = cb.getResultList();

        assertEquals(6, results.size());

        assertDocumentEquals(doc1, results.get(0));
        assertDocumentEquals(doc2, results.get(1));
        assertDocumentEquals(doc3, results.get(2));
        assertDocumentEquals(doc4, results.get(3));
        assertDocumentEquals(doc5, results.get(4));
        assertDocumentEquals(doc6, results.get(5));

        assertTypeMatches(results.get(0).getOwner(), evm, PersonBaseView2.class, YoungPersonView2.class);
        assertTypeMatches(results.get(1).getOwner(), evm, PersonBaseView2.class, YoungPersonView2.class);
        assertTypeMatches(results.get(2).getOwner(), evm, PersonBaseView2.class, PersonBaseView2.class);
        assertTypeMatches(results.get(3).getOwner(), evm, PersonBaseView2.class, PersonBaseView2.class);
        assertTypeMatches(results.get(4).getOwner(), evm, PersonBaseView2.class, PersonBaseView2.class);
        assertTypeMatches(results.get(5).getOwner(), evm, PersonBaseView2.class, PersonBaseView2.class);

        YoungPersonView2 persView1 = (YoungPersonView2) results.get(0).getOwner();
        YoungPersonView2 persView2 = (YoungPersonView2) results.get(1).getOwner();
        PersonBaseView persView3 = results.get(2).getOwner();
        PersonBaseView persView4 = results.get(3).getOwner();
        PersonBaseView persView5 = results.get(4).getOwner();
        PersonBaseView persView6 = results.get(5).getOwner();

        assertPersonEquals(doc1.getOwner(), persView1);
        assertPersonEquals(doc2.getOwner(), persView2);
        assertPersonEquals(doc3.getOwner(), persView3);
        assertPersonEquals(doc4.getOwner(), persView4);
        assertPersonEquals(doc5.getOwner(), persView5);
        assertPersonEquals(doc6.getOwner(), persView6);

        assertPersonEquals(doc1.getOwner().getFriend(), persView1.getFriend());
        assertNull(persView2.getFriend());
    }

    @Test
    // TODO: report that datanucleus thinks a NULL literal is of type Integer and normal integral literals are of type Long
    @Category({ NoDatanucleus.class })
    public void inheritanceQuerySubviewInheritanceMappingWithoutBaseType() {
        this.evm = build(
                DocumentView3.class,
                SimpleDocumentView.class,
                SimplePersonSubView.class,
                PersonBaseView3.class,
                OldPersonView3.class,
                YoungPersonView3.class
        );

        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d")
                .orderByAsc("id");
        CriteriaBuilder<DocumentView3> cb = evm.applySetting(EntityViewSetting.create(DocumentView3.class), criteria);
        List<DocumentView3> results = cb.getResultList();

        assertEquals(6, results.size());

        assertDocumentEquals(doc1, results.get(0));
        assertDocumentEquals(doc2, results.get(1));
        assertDocumentEquals(doc3, results.get(2));
        assertDocumentEquals(doc4, results.get(3));
        assertDocumentEquals(doc5, results.get(4));
        assertDocumentEquals(doc6, results.get(5));

        assertTypeMatches(results.get(0).getOwner(), evm, PersonBaseView3.class, YoungPersonView3.class);
        assertTypeMatches(results.get(1).getOwner(), evm, PersonBaseView3.class, YoungPersonView3.class);
        assertTypeMatches(results.get(2).getOwner(), evm, PersonBaseView3.class, YoungPersonView3.class);
        assertTypeMatches(results.get(3).getOwner(), evm, PersonBaseView3.class, YoungPersonView3.class);
        assertNull(results.get(4).getOwner());
        assertNull(results.get(5).getOwner());

        YoungPersonView3 persView1 = (YoungPersonView3) results.get(0).getOwner();
        YoungPersonView3 persView2 = (YoungPersonView3) results.get(1).getOwner();
        YoungPersonView3 persView3 = (YoungPersonView3) results.get(2).getOwner();
        YoungPersonView3 persView4 = (YoungPersonView3) results.get(3).getOwner();

        assertPersonEquals(doc1.getOwner(), persView1);
        assertPersonEquals(doc2.getOwner(), persView2);
        assertPersonEquals(doc3.getOwner(), persView3);
        assertPersonEquals(doc4.getOwner(), persView4);

        assertPersonEquals(doc1.getOwner().getFriend(), persView1.getFriend());
        assertNull(persView2.getFriend());
        assertPersonEquals(doc3.getOwner().getFriend(), persView3.getFriend());
        assertNull(persView4.getFriend());
    }

    public static <T> void assertTypeMatches(T o, EntityViewManager evm, Class<T> baseType, Class<? extends T> subtype) {
        String name = o.getClass().getName();
        if (name.endsWith("_")) {
            assertEquals(subtype.getName() + "_$$_javassist_entityview_", name);
        } else {
            assertEquals(subtype.getName() + "Impl", name);
        }
    }

    public static void assertDocumentEquals(Document doc, SimpleDocumentView view) {
        if (doc == null) {
            assertNull(view);
        }
        Assert.assertEquals(doc.getId(), view.getId());
        Assert.assertEquals(doc.getName(), view.getName());
    }

    public static void assertPersonEquals(Person pers, PersonBaseView view) {
        if (pers == null) {
            assertNull(view);
        }
        Assert.assertEquals(pers.getId(), view.getId());
        Assert.assertEquals(pers.getName(), view.getName());
    }
}
