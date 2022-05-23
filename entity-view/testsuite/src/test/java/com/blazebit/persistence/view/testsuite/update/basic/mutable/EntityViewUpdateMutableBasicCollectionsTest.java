/*
 * Copyright 2014 - 2022 Blazebit.
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

package com.blazebit.persistence.view.testsuite.update.basic.mutable;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.change.ChangeModel;
import com.blazebit.persistence.view.change.PluralChangeModel;
import com.blazebit.persistence.view.change.SingularChangeModel;
import com.blazebit.persistence.view.spi.type.MutableStateTrackable;
import com.blazebit.persistence.view.testsuite.update.basic.AbstractEntityViewUpdateBasicCollectionsTest;
import com.blazebit.persistence.view.testsuite.update.basic.mutable.model.UpdatableDocumentBasicWithCollectionsView;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateMutableBasicCollectionsTest extends AbstractEntityViewUpdateBasicCollectionsTest<UpdatableDocumentBasicWithCollectionsView> {

    public EntityViewUpdateMutableBasicCollectionsTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentBasicWithCollectionsView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Test
    public void testUpdateReplaceCollection() {
        // Given
        final UpdatableDocumentBasicWithCollectionsView docView = updateReplaceCollection();
        Long oldVersion = docView.getVersion();
        assertEmptyChangeModel(docView);
        updateAndAssertChangesFlushed(docView);

        // Then
        // Assert that the document and the strings are loaded in full mode.
        // During dirty detection we should be able to figure out that nothing changed
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isFullMode()) {
            if (!isQueryStrategy()) {
                fullFetch(builder);
            }

            if (isQueryStrategy()) {
                assertReplaceAnd(builder);
                builder.update(Document.class);
            }
        }

        builder.validate();

        assertVersionDiff(oldVersion, docView.getVersion(), 0, isQueryStrategy() ? 1 : 0);
        assertNoUpdateAndReload(docView);
        assertVersionDiff(oldVersion, docView.getVersion(), 0, isQueryStrategy() ? 2 : 0);
        assertEquals(doc1.getStrings(), docView.getStrings());
        assertEquals(doc1.getVersion(), docView.getVersion());
    }

    @Test
    public void testUpdateAddToCollection() {
        // Given
        final UpdatableDocumentBasicWithCollectionsView docView = updateAddToCollection();
        Long oldVersion = docView.getVersion();
        assertChangesUpdateAndFlush(docView);

        // Then
        // Assert that the document and the strings are loaded, but only a relation insert is done
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (!isQueryStrategy()) {
            fullFetch(builder);
        }

        if (version || isQueryStrategy() && isFullMode()) {
            builder.update(Document.class);
        }
        if (isQueryStrategy() && isFullMode()) {
            assertReplaceAnd(builder);
        }

        builder.assertInsert()
                .forRelation(Document.class, "strings")
                .validate();

        assertVersionDiff(oldVersion, docView.getVersion(), 1, 1);
        assertNoUpdateAndReload(docView);
        assertVersionDiff(oldVersion, docView.getVersion(), 1, isQueryStrategy() ? 2 : 1);
        assertEquals(doc1.getStrings(), docView.getStrings());
        assertEquals(doc1.getVersion(), docView.getVersion());
    }

    @Test
    public void testUpdateAddToNewCollection() {
        // Given
        final UpdatableDocumentBasicWithCollectionsView docView = updateAddToNewCollection();
        Long oldVersion = docView.getVersion();
        assertChangesUpdateAndFlush(docView);

        // Then
        // In partial mode, only the document is loaded. In full mode, the strings are also loaded
        // Since we load the strings in the full mode, we do a proper diff and can compute that only a single item was added
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (!isQueryStrategy()) {
            if (isFullMode()) {
                fullFetch(builder);
            } else {
                if (preferLoadingAndDiffingOverRecreate()) {
                    fullFetch(builder);
                } else {
                    assertReplaceAnd(builder);
                }
            }
        }

        if (version || isQueryStrategy() && isFullMode()) {
            builder.update(Document.class);
        }
        if (isQueryStrategy() && isFullMode()) {
            assertReplaceAnd(builder);
        }

        builder.assertInsert()
                .forRelation(Document.class, "strings")
                .validate();

        assertVersionDiff(oldVersion, docView.getVersion(), 1, 1);
        assertNoUpdateAndReload(docView);
        assertVersionDiff(oldVersion, docView.getVersion(), 1, isQueryStrategy() ? 2 : 1);
        assertEquals(doc1.getStrings(), docView.getStrings());
        assertEquals(doc1.getVersion(), docView.getVersion());
    }

    @Test
    public void testUpdateRemoveNonExisting() {
        // Given
        final UpdatableDocumentBasicWithCollectionsView docView = updateRemoveNonExisting();

        // When
        update(docView);

        // Then
        assertNoUpdateAndReload(docView);
        assertEquals(doc1.getStrings(), docView.getStrings());
        assertEquals(doc1.getVersion(), docView.getVersion());
    }

    @Test
    public void testUpdateRemoveNull() {
        // Given
        final UpdatableDocumentBasicWithCollectionsView docView = updateRemoveNull();

        // When
        update(docView);

        // Then
        assertNoUpdateAndReload(docView);
        assertEquals(doc1.getStrings(), docView.getStrings());
        assertEquals(doc1.getVersion(), docView.getVersion());
    }

    @Test
    public void testUpdateReferenceAddToCollection() {
        // Given
        final UpdatableDocumentBasicWithCollectionsView docView = evm.getReference(UpdatableDocumentBasicWithCollectionsView.class, doc1.getId());
        ((MutableStateTrackable) docView).$$_setVersion(doc1.getVersion());

        // When
        docView.setName("doc1");
        docView.getStrings().add("test");
        update(docView);

        // Then
        clearPersistenceContextAndReload();
        assertEquals("doc1", doc1.getName());
        assertEquals(Collections.singletonList("test"), doc1.getStrings());
    }

    protected void assertChangesUpdateAndFlush(UpdatableDocumentBasicWithCollectionsView docView) {
        if (!isFullMode()) {
            SingularChangeModel<UpdatableDocumentBasicWithCollectionsView> changeModel = evm.getChangeModel(docView);
            assertTrue(changeModel.isDirty());
            assertEquals(ChangeModel.ChangeKind.MUTATED, changeModel.getKind());

            PluralChangeModel<?, ?> stringsChange = (PluralChangeModel<?, ?>) changeModel.get("strings");
            assertTrue(stringsChange.isDirty());
            assertEquals(ChangeModel.ChangeKind.MUTATED, stringsChange.getKind());

            assertEquals(1, stringsChange.getAddedElements().size());
            assertEquals(0, stringsChange.getRemovedElements().size());
            assertEquals(0, stringsChange.getMutatedElements().size());
            assertEquals(ChangeModel.ChangeKind.UPDATED, stringsChange.getAddedElements().get(0).getKind());
            assertNull(stringsChange.getAddedElements().get(0).getInitialState());
            assertEquals("newString", stringsChange.getAddedElements().get(0).getCurrentState());
            assertEquals(Arrays.asList(stringsChange), changeModel.getDirtyChanges());
        }
        updateAndAssertChangesFlushed(docView);
    }

    private AssertStatementBuilder assertReplaceAnd(AssertStatementBuilder builder) {
        return builder.assertDelete()
                    .forRelation(Document.class, "strings")
                .and()
                .assertInsert()
                    .forRelation(Document.class, "strings")
                .and();
    }

    @Override
    protected AssertStatementBuilder fullFetch(AssertStatementBuilder builder) {
        return builder.assertSelect()
                .fetching(Document.class)
                .fetching(Document.class, "strings")
                .and();
    }

    @Override
    protected AssertStatementBuilder fullUpdate(AssertStatementBuilder builder) {
        assertReplaceAnd(builder);
        if (doc1.getStrings().size() > 1) {
            builder.assertInsert()
                .forRelation(Document.class, "strings")
                .and();
        }
        return builder.assertUpdate()
                .forEntity(Document.class)
                .and();
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder.update(Document.class);
    }
}
