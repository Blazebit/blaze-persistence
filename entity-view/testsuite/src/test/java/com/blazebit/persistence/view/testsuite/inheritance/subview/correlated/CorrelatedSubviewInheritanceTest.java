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

package com.blazebit.persistence.view.testsuite.inheritance.subview.correlated;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus4;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate50;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMSSQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.inheritance.subview.correlated.model.DocumentView;
import com.blazebit.persistence.view.testsuite.inheritance.subview.correlated.model.DocumentView1;
import com.blazebit.persistence.view.testsuite.inheritance.subview.correlated.model.DocumentView2;
import com.blazebit.persistence.view.testsuite.inheritance.subview.correlated.model.DocumentView3;
import com.blazebit.persistence.view.testsuite.inheritance.subview.correlated.model.DocumentView4;
import com.blazebit.persistence.view.testsuite.inheritance.subview.correlated.model.OldPersonView1;
import com.blazebit.persistence.view.testsuite.inheritance.subview.correlated.model.OldPersonView2;
import com.blazebit.persistence.view.testsuite.inheritance.subview.correlated.model.OldPersonView3;
import com.blazebit.persistence.view.testsuite.inheritance.subview.correlated.model.OldPersonView4;
import com.blazebit.persistence.view.testsuite.inheritance.subview.correlated.model.PersonBaseView;
import com.blazebit.persistence.view.testsuite.inheritance.subview.correlated.model.PersonBaseView1;
import com.blazebit.persistence.view.testsuite.inheritance.subview.correlated.model.PersonBaseView2;
import com.blazebit.persistence.view.testsuite.inheritance.subview.correlated.model.PersonBaseView3;
import com.blazebit.persistence.view.testsuite.inheritance.subview.correlated.model.PersonBaseView4;
import com.blazebit.persistence.view.testsuite.inheritance.subview.correlated.model.YoungPersonView1;
import com.blazebit.persistence.view.testsuite.inheritance.subview.correlated.model.YoungPersonView2;
import com.blazebit.persistence.view.testsuite.inheritance.subview.correlated.model.YoungPersonView3;
import com.blazebit.persistence.view.testsuite.inheritance.subview.correlated.model.YoungPersonView4;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Christian Beikov
 * @since 1.3.0
 */
// NOTE: SQL Server and Oracle can't convert integers to strings automatically when concatenating?
public class CorrelatedSubviewInheritanceTest extends AbstractEntityViewTest {

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
    // NOTE: Hibernate only supports entity joins from 5.1 onwards
    // NOTE: EclipseLink fails to handle the query because the ON clause contains function expressions CONCAT
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoEclipselink.class, NoDatanucleus4.class, NoMSSQL.class })
    public void inheritanceQueryJoined() {
        inheritanceQuery(DocumentView1.class, PersonBaseView1.class, OldPersonView1.class, YoungPersonView1.class);
    }

    @Test
    // NOTE: EclipseLink fails to handle the query because the ON clause contains function expressions CONCAT
    @Category({ NoEclipselink.class, NoDatanucleus4.class, NoMSSQL.class })
    public void inheritanceQuerySubselect() {
        inheritanceQuery(DocumentView2.class, PersonBaseView2.class, OldPersonView2.class, YoungPersonView2.class);
    }

    @Test
    @Category({ NoDatanucleus4.class, NoMSSQL.class })
    public void inheritanceQuerySelect() {
        inheritanceQuery(DocumentView3.class, PersonBaseView3.class, OldPersonView3.class, YoungPersonView3.class);
    }

    @Test
    // NOTE: This required the VALUES clause which is only supported for Hibernate for now
    // NOTE: For Oracle, we use to_clob in VALUES for cast_string which is problematic
    @Category({ NoEclipselink.class, NoDatanucleus.class, NoOpenJPA.class, NoMSSQL.class, NoOracle.class })
    public void inheritanceQuerySelectBatch() {
        inheritanceQuery(DocumentView4.class, PersonBaseView4.class, OldPersonView4.class, YoungPersonView4.class);
    }

    public <T extends PersonBaseView> void inheritanceQuery(Class<? extends DocumentView> docViewClass, Class<T> baseView, Class<? extends T> oldView, Class<? extends T> youngView) {
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(docViewClass);
        cfg.addEntityView(baseView);
        cfg.addEntityView(oldView);
        cfg.addEntityView(youngView);
        this.evm = cfg.createEntityViewManager(cbf);

        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d")
            .orderByAsc("id");
        // JDK 10+ javac bug doesn't allow the use of ? extends DocumentView ??
        CriteriaBuilder<DocumentView> cb = (CriteriaBuilder<DocumentView>) evm.applySetting(EntityViewSetting.create(docViewClass), criteria);
        List<DocumentView> results = cb.getResultList();

        assertEquals(6, results.size());

        assertDocumentEquals(doc1, results.get(0));
        assertDocumentEquals(doc2, results.get(1));
        assertDocumentEquals(doc3, results.get(2));
        assertDocumentEquals(doc4, results.get(3));
        assertDocumentEquals(doc5, results.get(4));
        assertDocumentEquals(doc6, results.get(5));

        assertTypeMatches(results.get(0).getOwner(), evm, baseView, youngView);
        assertTypeMatches(results.get(1).getOwner(), evm, baseView, youngView);
        assertTypeMatches(results.get(2).getOwner(), evm, baseView, baseView);
        assertTypeMatches(results.get(3).getOwner(), evm, baseView, baseView);
        assertTypeMatches(results.get(4).getOwner(), evm, baseView, oldView);
        assertTypeMatches(results.get(5).getOwner(), evm, baseView, oldView);

        PersonBaseView persView1 = results.get(0).getOwner();
        PersonBaseView persView2 = results.get(1).getOwner();
        PersonBaseView persView3 = results.get(2).getOwner();
        PersonBaseView persView4 = results.get(3).getOwner();
        PersonBaseView persView5 = results.get(4).getOwner();
        PersonBaseView persView6 = results.get(5).getOwner();

        assertEquals("Young pers1", persView1.getName());
        assertEquals("Young pers2", persView2.getName());
        assertEquals("pers3", persView3.getName());
        assertEquals("pers4", persView4.getName());
        assertEquals("Old pers5", persView5.getName());
        assertEquals("Old pers6", persView6.getName());
    }

    public static <T> void assertTypeMatches(T o, EntityViewManager evm, Class<? extends T> baseType, Class<? extends T> subtype) {
        assertEquals(baseType.getName() + "_" + subtype.getSimpleName() + "_$$_javassist_entityview_", o.getClass().getName());
    }

    public static void assertDocumentEquals(Document doc, DocumentView view) {
        if (doc == null) {
            assertNull(view);
        }
        Assert.assertEquals(doc.getId(), view.getId());
        Assert.assertEquals(doc.getName(), view.getName());
    }
}
