/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.entity.updatableonly;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.testsuite.update.entity.AbstractEntityViewUpdateEntityTest;
import com.blazebit.persistence.view.testsuite.update.entity.updatableonly.model.UpdatableDocumentEntityView;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.persistence.PersistenceException;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateUpdatableOnlyEntityTest extends AbstractEntityViewUpdateEntityTest<UpdatableDocumentEntityView> {

    public EntityViewUpdateUpdatableOnlyEntityTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentEntityView.class);
    }

    @Test
    public void testSimpleUpdate() {
        // Given & When
        final UpdatableDocumentEntityView docView = simpleUpdate();

        // Then
        // Assert that not only the document is loaded
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (!isQueryStrategy()) {
            fullFetch(builder);
        }

        builder.update(Document.class)
                .validate();

        assertNoUpdateAndReload(docView);
        assertEquals("newDoc", docView.getName());
        assertEquals(doc1.getName(), docView.getName());
    }

    @Test
    public void testUpdateWithEntity() {
        // Given & When
        final UpdatableDocumentEntityView docView = updateWithEntity();

        // Then
        // Since the responsiblePerson changed we don't need to load the old responsiblePerson
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (!isQueryStrategy()) {
            fullFetch(builder);
        }

        builder.update(Document.class)
                .validate();

        assertNoUpdateAndReload(docView);
        assertEquals(p2.getId(), docView.getResponsiblePerson().getId());
        assertEquals(doc1.getResponsiblePerson().getId(), docView.getResponsiblePerson().getId());
    }

    @Test
    public void testUpdateWithModifyEntity() {
        // Given & When
        final UpdatableDocumentEntityView docView = updateWithModifyEntity();

        // Then
        // Since the responsiblePerson changed we don't need to load the old responsiblePerson
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (!isQueryStrategy()) {
            fullFetch(builder);
        }

        builder.update(Document.class)
                .validate();

        // Unfortunately, even after an update, we have to reload the entity to merge again
        assertNoUpdateAndReload(docView);
        assertEquals(p2.getId(), docView.getResponsiblePerson().getId());
        assertEquals(doc1.getResponsiblePerson().getId(), docView.getResponsiblePerson().getId());
        assertEquals("pers2", doc1.getResponsiblePerson().getName());
    }

    @Test
    public void testUpdateWithModifyExisting() {
        // Given & When
        final UpdatableDocumentEntityView docView = updateWithModifyExisting();

        // Then
        // Since updates aren't cascaded, the responsiblePerson does not need to be loaded
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isQueryStrategy()) {
            if (isFullMode()) {
                fullUpdate(builder);
            }
        } else {
            if (isFullMode()) {
                fullFetch(builder);
            }
        }

        builder.validate();

        assertNoUpdateAndReload(docView);
        assertEquals(doc1.getResponsiblePerson().getId(), docView.getResponsiblePerson().getId());
        assertEquals("pers1", doc1.getResponsiblePerson().getName());
    }

    @Test
    public void testUpdateToNull() {
        // Given & When
        final UpdatableDocumentEntityView docView = updateToNull();

        // Then
        // Since the responsiblePerson changed we don't need to load the old responsiblePerson
        // Since the new responsiblePerson is null, we don't need to do anything further
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (!isQueryStrategy()) {
            fullFetch(builder);
        }

        builder.update(Document.class)
                .validate();

        // Updating to null thankfully does not require reloading the relation
        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);

        if (isQueryStrategy()) {
            if (isFullMode()) {
                fullUpdate(afterBuilder);
            }
        } else {
            if (isFullMode()) {
                fullFetch(afterBuilder);
            }
        }
        afterBuilder.validate();
        assertNull(doc1.getResponsiblePerson());
    }

    @Test
    public void testUpdateToNewPerson() {
        try {
            // Given & When
            final UpdatableDocumentEntityView docView = updateToNewPerson(true);
            fail("Expected a transient reference error for the new person!");
        } catch (PersistenceException | IllegalStateException ex) {
            // Then
            assertEquals(1L, updateCommitCount.get());
            assertTrue(ex.getMessage().contains("transient"));
            AssertStatementBuilder builder = assertUnorderedQuerySequence();

            if (!isQueryStrategy()) {
                if (isFullMode()) {
                    fullFetch(builder);
                } else {
                    builder.select(Document.class);
                }
            }

            // We need to load the rollback view
            builder.select(Document.class);
            builder.validate();
            clearPersistenceContextAndReload();
            assertEquals(p1.getId(), doc1.getResponsiblePerson().getId());
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
