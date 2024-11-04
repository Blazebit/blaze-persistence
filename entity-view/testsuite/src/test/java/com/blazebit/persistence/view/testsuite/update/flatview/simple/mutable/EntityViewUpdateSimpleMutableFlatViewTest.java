/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.flatview.simple.mutable;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.tx.TxWork;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.spi.type.MutableStateTrackable;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateDocumentTest;
import com.blazebit.persistence.view.testsuite.update.flatview.simple.mutable.model.IntIdEntityCreateView;
import com.blazebit.persistence.view.testsuite.update.flatview.simple.mutable.model.IntIdEntityIdView;
import com.blazebit.persistence.view.testsuite.update.flatview.simple.mutable.model.UpdatableDocumentView;
import com.blazebit.persistence.view.testsuite.update.flatview.simple.mutable.model.UpdatableNameObjectView;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import jakarta.persistence.EntityManager;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateSimpleMutableFlatViewTest extends AbstractEntityViewUpdateDocumentTest<UpdatableDocumentView> {

    private Integer i1Id;

    public EntityViewUpdateSimpleMutableFlatViewTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        cfg.addEntityView(UpdatableNameObjectView.class);
        cfg.addEntityView(IntIdEntityIdView.class);
        cfg.addEntityView(IntIdEntityCreateView.class);
    }

    @Override
    protected void prepareData(EntityManager em) {
        super.prepareData(em);
        i1Id = transactional(new TxWork<Integer>() {
            @Override
            public Integer work(EntityManager em) {
                IntIdEntity i1 = new IntIdEntity("i1", 1);
                em.persist(i1);
                return i1.getId();
            }
        });
    }

    @Test
    public void testSimpleUpdate() {
        // Given
        final UpdatableDocumentView docView = getDoc1View();
        clearQueries();
        
        // When
        docView.getNameObject().setPrimaryName("newDoc");
        update(docView);

        // Then
        // Since the only the documents primaryName changed we only need to load the document
        // In full mode, the person also has to be loaded
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (!isQueryStrategy()) {
            if (isFullMode()) {
                fullFetch(builder);
            } else {
                builder.select(Document.class);
            }
        }

        builder.update(Document.class)
                .validate();

        assertNoUpdateAndReload(docView);
        Assert.assertEquals("newDoc", docView.getNameObject().getPrimaryName());
        Assert.assertEquals(doc1.getNameObject().getPrimaryName(), docView.getNameObject().getPrimaryName());
    }

    @Test
    public void testUpdateWithSubview() {
        // Given
        final UpdatableDocumentView docView = getDoc1View();
        clearQueries();

        // When
        IntIdEntityIdView idEntityIdView = evm.getReference(IntIdEntityIdView.class, i1Id);
        docView.getNameObject().setIntIdEntity(idEntityIdView);
        update(docView);

        // Then
        // Since the owner's name changed we, have to load the document and the owner
        // We apply the change which results in an update
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (!isQueryStrategy()) {
            fullFetch(builder);
        }

        builder.update(Document.class)
                .validate();

        assertNoUpdateAndReload(docView);
        Assert.assertEquals(idEntityIdView.getId(), doc1.getNameObject().getIntIdEntity().getId());
        Assert.assertEquals("doc1", doc1.getNameObject().getPrimaryName());
    }

    @Test
    public void testPartialUpdate() {
        // Given
        final UpdatableDocumentView docView = evm.getReference(UpdatableDocumentView.class, doc1.getId());
        // Set the version so that optimistic locking works, otherwise this will fail in full mode
        ((MutableStateTrackable) docView).$$_setVersion(1L);
        clearQueries();

        // When
        IntIdEntityIdView idEntityIdView = evm.getReference(IntIdEntityIdView.class, i1Id);
        UpdatableNameObjectView updatableNameObjectView = evm.create(UpdatableNameObjectView.class);
        docView.setNameObject(updatableNameObjectView);
        docView.getNameObject().setIntIdEntity(idEntityIdView);
        update(docView);

        // Then
        // Since the only the documents primaryName changed we only need to load the document
        // In full mode, the person also has to be loaded
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (!isQueryStrategy()) {
            if (isFullMode()) {
                fullFetch(builder);
            } else {
                builder.select(Document.class);
            }
        }

        builder.update(Document.class)
            .validate();

        assertNoUpdateAndReload(docView);
        Assert.assertEquals(idEntityIdView.getId(), doc1.getNameObject().getIntIdEntity().getId());
        // Since this is a test for a partial update, using the full mode will obviously not work
        if (isFullMode()) {
            Assert.assertNull(doc1.getNameObject().getPrimaryName());
        } else {
            Assert.assertEquals("doc1", doc1.getNameObject().getPrimaryName());
        }
    }

    @Override
    protected AssertStatementBuilder fullFetch(AssertStatementBuilder builder) {
        return builder.assertSelect()
                .fetching(Document.class)
                .and();
    }

    @Override
    protected AssertStatementBuilder fullUpdate(AssertStatementBuilder builder) {
        return builder.update(Document.class);
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder.update(Document.class);
    }
}
