/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.subview.field;

import javax.persistence.EntityManager;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.IndexedEmbeddable;
import com.blazebit.persistence.testsuite.entity.IndexedNode2;
import com.blazebit.persistence.testsuite.entity.KeyedEmbeddable;
import com.blazebit.persistence.testsuite.entity.KeyedNode2;
import com.blazebit.persistence.testsuite.entity.Root2;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateTest;
import com.blazebit.persistence.view.testsuite.update.subview.field.model.IndexedNode2View;
import com.blazebit.persistence.view.testsuite.update.subview.field.model.Root2View;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.6.16
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateSimpleImmutableSubviewTest extends AbstractEntityViewUpdateTest<IndexedNode2View> {

    Root2 root1;

    public EntityViewUpdateSimpleImmutableSubviewTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, IndexedNode2View.class);
    }

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class[]{
                Root2.class,
                IndexedNode2.class,
                KeyedNode2.class,
                KeyedEmbeddable.class,
                IndexedEmbeddable.class
        };
    }

    @Override
    protected void prepareData(EntityManager em) {
        root1 = new Root2();
        root1.setId(1);
        root1.setName("root1");

        em.persist(root1);
    }

    @Override
    protected void cleanDatabase() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                em.createQuery("DELETE FROM IndexedNode2").executeUpdate();
                em.createQuery("DELETE FROM KeyedNode2").executeUpdate();
                em.createQuery("DELETE FROM Root2").executeUpdate();
            }
        });
    }

    @Override
    protected void reload() {
        cbf.create(em, Root2.class)
                .fetch("indexedNodesMappedBy", "keyedNodesMappedBy")
                .getResultList();
        root1 = em.find(Root2.class, root1.getId());
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        cfg.addEntityView(Root2View.class);
    }

    @Test
    public void testSimpleUpdate() {
        // Given
        final Root2View rootView = evm.find(em, Root2View.class, root1.getId());
        clearQueries();

        // When
        IndexedNode2View indexedNodeView = evm.create(IndexedNode2View.class);
        indexedNodeView.setId(1);
        indexedNodeView.setIndex(0);
        indexedNodeView.setParent(rootView);
        update(indexedNodeView);

        // Then
        // Assert that only the document is loaded and finally also updated
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        builder.insert(IndexedNode2.class)
                .validate();

        clearPersistenceContextAndReload();
        assertEquals(0, root1.getKeyedNodesMappedBy().size());
        assertEquals(1, root1.getIndexedNodesMappedBy().size());
        assertEquals(1, root1.getIndexedNodesMappedBy().get(0).getId().intValue());
    }

    @Override
    protected AssertStatementBuilder fullFetch(AssertStatementBuilder builder) {
        return builder;
    }

    @Override
    protected AssertStatementBuilder fullUpdate(AssertStatementBuilder builder) {
        return builder;
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder;
    }
}
