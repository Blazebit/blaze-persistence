/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.flat;

import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.NameObject;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.UpdatableMapping;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import java.util.Map;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.6.3
 */
public class UpdatableFlatViewTest extends AbstractEntityViewTest {

    protected EntityViewManager evm;

    @Before
    public void initEvm() {
        evm = build(
                UpdatableDocumentWithMapsJoinView.class,
                UpdatableDocumentWithMapsSelectView.class,
                UpdatableDocumentWithMapsSubselectView.class,
                UpdatableDocumentWithMapsMultisetView.class,
                UpdatableNameObjectView.class
        );
    }

    private Document doc1;

    @Before
    public void setUp() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                doc1 = new Document("doc1");
                Person o1 = new Person("pers1");

                doc1.setOwner(o1);
                doc1.getNameMap().put("doc1", new NameObject("doc1", "doc1"));
                doc1.getNameMap().put("doc2", new NameObject("doc1", "doc1"));

                em.persist(o1);
                em.persist(doc1);
            }
        });

        doc1 = cbf.create(em, Document.class)
                .where("id").eq(doc1.getId())
                .fetch("owner")
                .getSingleResult();
    }

    @Test
    public void queryJoin() {
        test(UpdatableDocumentWithMapsJoinView.class);
    }

    @Test
    public void querySelect() {
        test(UpdatableDocumentWithMapsSelectView.class);
    }

    @Test
    public void querySubselect() {
        test(UpdatableDocumentWithMapsSubselectView.class);
    }

    // NOTE: EclipseLink can't handle multiple subquery select items... Only one expression can be declared in a SELECT clause of a subquery
    // NOTE: DataNucleus can't handle multiple subquery select items... Number of result expressions in subquery should be 1
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class })
    public void queryMultiset() {
        test(UpdatableDocumentWithMapsMultisetView.class);
    }

    private <T extends UpdatableDocumentWithMapsView> void test(Class<T> clazz) {
        T view = evm.find(em, clazz, doc1.getId());
        assertEquals(2, view.getNameMap().size());
        assertNotSame(view.getNameMap().get("doc1"), view.getNameMap().get("doc2"));
    }

    /**
     *
     * @author Christian Beikov
     * @since 1.6.3
     */
    @UpdatableEntityView
    @EntityView(Document.class)
    public interface UpdatableDocumentWithMapsView {
        @IdMapping
        public Long getId();
        @UpdatableMapping
        public Map<String, UpdatableNameObjectView> getNameMap();
    }

    /**
     *
     * @author Christian Beikov
     * @since 1.6.3
     */
    @UpdatableEntityView
    @EntityView(Document.class)
    public interface UpdatableDocumentWithMapsJoinView extends UpdatableDocumentWithMapsView {
        @Mapping(fetch = FetchStrategy.JOIN)
        public Map<String, UpdatableNameObjectView> getNameMap();
    }

    /**
     *
     * @author Christian Beikov
     * @since 1.6.3
     */
    @UpdatableEntityView
    @EntityView(Document.class)
    public interface UpdatableDocumentWithMapsSelectView extends UpdatableDocumentWithMapsView {
        @Mapping(fetch = FetchStrategy.SELECT)
        public Map<String, UpdatableNameObjectView> getNameMap();
    }

    /**
     *
     * @author Christian Beikov
     * @since 1.6.3
     */
    @UpdatableEntityView
    @EntityView(Document.class)
    public interface UpdatableDocumentWithMapsSubselectView extends UpdatableDocumentWithMapsView {
        @Mapping(fetch = FetchStrategy.SUBSELECT)
        public Map<String, UpdatableNameObjectView> getNameMap();
    }

    /**
     *
     * @author Christian Beikov
     * @since 1.6.3
     */
    @UpdatableEntityView
    @EntityView(Document.class)
    public interface UpdatableDocumentWithMapsMultisetView extends UpdatableDocumentWithMapsView {
        @Mapping(fetch = FetchStrategy.MULTISET)
        public Map<String, UpdatableNameObjectView> getNameMap();
    }

    /**
     *
     * @author Christian Beikov
     * @since 1.6.3
     */
    @UpdatableEntityView
    @EntityView(NameObject.class)
    public interface UpdatableNameObjectView {
        public String getPrimaryName();
        public void setPrimaryName(String primaryName);
    }
}
