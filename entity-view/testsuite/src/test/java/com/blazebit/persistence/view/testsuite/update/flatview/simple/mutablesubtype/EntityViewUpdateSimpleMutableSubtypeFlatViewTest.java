/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.flatview.simple.mutablesubtype;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateDocumentTest;
import com.blazebit.persistence.view.testsuite.update.flatview.simple.mutablesubtype.model.ReadonlyNameObjectView;
import com.blazebit.persistence.view.testsuite.update.flatview.simple.mutablesubtype.model.UpdatableDocumentView;
import com.blazebit.persistence.view.testsuite.update.flatview.simple.mutablesubtype.model.UpdatableNameObjectView;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateSimpleMutableSubtypeFlatViewTest extends AbstractEntityViewUpdateDocumentTest<UpdatableDocumentView> {

    public EntityViewUpdateSimpleMutableSubtypeFlatViewTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        cfg.addEntityView(UpdatableNameObjectView.class);
        cfg.addEntityView(ReadonlyNameObjectView.class);
    }

    @Test
    public void testSimpleUpdate() {
        // Given
        final UpdatableDocumentView docView = getDoc1View();
        clearQueries();
        
        // When
        UpdatableNameObjectView updatableNameObjectView = evm.convert(docView.getNameObject(), UpdatableNameObjectView.class);
        updatableNameObjectView.setPrimaryName("newDoc");
        docView.setNameObject(updatableNameObjectView);
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
        UpdatableNameObjectView updatableNameObjectView = evm.convert(docView.getNameObject(), UpdatableNameObjectView.class);
        updatableNameObjectView.setPrimaryName("newPers");
        docView.setNameObject(updatableNameObjectView);
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
        Assert.assertEquals("newPers", docView.getNameObject().getPrimaryName());
        Assert.assertEquals(doc1.getNameObject().getPrimaryName(), docView.getNameObject().getPrimaryName());
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
