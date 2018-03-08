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

package com.blazebit.persistence.view.testsuite.update.basic.mutable;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.change.ChangeModel;
import com.blazebit.persistence.view.change.MapChangeModel;
import com.blazebit.persistence.view.change.SingularChangeModel;
import com.blazebit.persistence.view.testsuite.update.basic.AbstractEntityViewUpdateBasicMapsTest;
import com.blazebit.persistence.view.testsuite.update.basic.mutable.model.UpdatableDocumentBasicWithMapsView;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateMutableBasicMapsTest extends AbstractEntityViewUpdateBasicMapsTest<UpdatableDocumentBasicWithMapsView> {

    public EntityViewUpdateMutableBasicMapsTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentBasicWithMapsView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Test
    public void testUpdateReplaceCollection() {
        // Given
        final UpdatableDocumentBasicWithMapsView docView = updateReplaceCollection();
        Long oldVersion = docView.getVersion();
        assertEmptyChangeModel(docView);
        updateAndAssertChangesFlushed(docView);

        // Then
        // Assert that the document and the strings are loaded in full mode.
        // During dirty detection we should be able to figure out that nothing changed
        AssertStatementBuilder builder = assertQuerySequence();

        if (isFullMode()) {
            fullFetch(builder);

            if (version) {
                builder.update(Document.class);
            }
        }

        builder.validate();

        assertVersionDiff(oldVersion, docView.getVersion(), 0, 1);
        assertNoUpdateAndReload(docView);
        assertVersionDiff(oldVersion, docView.getVersion(), 0, 2);
        assertEquals(doc1.getStringMap(), docView.getStringMap());
        assertEquals(doc1.getVersion(), docView.getVersion());
    }

    @Test
    public void testUpdateAddToCollection() {
        // Given
        final UpdatableDocumentBasicWithMapsView docView = updateAddToCollection();
        Long oldVersion = docView.getVersion();
        assertChangesUpdateAndFlush(docView);

        // Then
        // Assert that the document and the strings are loaded, but only a relation insert is done
        AssertStatementBuilder builder = assertQuerySequence();

        fullFetch(builder);

        if (version) {
            builder.update(Document.class);
        }

        builder.assertInsert()
                .forRelation(Document.class, "stringMap")
                .validate();

        assertVersionDiff(oldVersion, docView.getVersion(), 1, 1);
        assertNoUpdateAndReload(docView);
        assertVersionDiff(oldVersion, docView.getVersion(), 1, 2);
        assertEquals(doc1.getStringMap(), docView.getStringMap());
        assertEquals(doc1.getVersion(), docView.getVersion());
    }

    @Test
    public void testUpdateAddToNewCollection() {
        // Given
        final UpdatableDocumentBasicWithMapsView docView = updateAddToNewCollection();
        Long oldVersion = docView.getVersion();
        assertChangesUpdateAndFlush(docView);

        // Then
        // In partial mode, only the document is loaded. In full mode, the strings are also loaded
        // Since we load the strings in the full mode, we do a proper diff and can compute that only a single item was added
        AssertStatementBuilder builder = assertQuerySequence();

        if (isFullMode()) {
            fullFetch(builder);
        } else {
            if (preferLoadingAndDiffingOverRecreate()) {
                fullFetch(builder);
            } else {
                assertReplaceAnd(builder);
            }
        }

        if (version) {
            builder.update(Document.class);
        }

        builder.assertInsert()
                .forRelation(Document.class, "stringMap")
                .validate();

        assertVersionDiff(oldVersion, docView.getVersion(), 1, 1);
        assertNoUpdateAndReload(docView);
        assertVersionDiff(oldVersion, docView.getVersion(), 1, 2);
        assertEquals(doc1.getStringMap(), docView.getStringMap());
        assertEquals(doc1.getVersion(), docView.getVersion());
    }

    protected void assertChangesUpdateAndFlush(UpdatableDocumentBasicWithMapsView docView) {
        if (!isFullMode()) {
            SingularChangeModel<UpdatableDocumentBasicWithMapsView> changeModel = evm.getChangeModel(docView);
            assertTrue(changeModel.isDirty());
            assertEquals(ChangeModel.ChangeKind.MUTATED, changeModel.getKind());

            MapChangeModel<?, ?> stringMapChange = (MapChangeModel<Object, Object>) changeModel.<Map<Object, Object>>get("stringMap");
            assertTrue(stringMapChange.isDirty());
            assertEquals(ChangeModel.ChangeKind.MUTATED, stringMapChange.getKind());

            assertEquals(1, stringMapChange.getAddedElements().size());
            assertEquals(ChangeModel.ChangeKind.UPDATED, stringMapChange.getAddedElements().get(0).getKind());
            assertNull(stringMapChange.getAddedElements().get(0).getInitialState());
            assertEquals("newString", stringMapChange.getAddedElements().get(0).getCurrentState());

            assertEquals(1, stringMapChange.getAddedKeys().size());
            assertEquals(ChangeModel.ChangeKind.UPDATED, stringMapChange.getAddedKeys().get(0).getKind());
            assertNull(stringMapChange.getAddedKeys().get(0).getInitialState());
            assertEquals("newString", stringMapChange.getAddedKeys().get(0).getCurrentState());

            assertEquals(2, stringMapChange.getAddedObjects().size());
            assertTrue(stringMapChange.getAddedObjects().contains(stringMapChange.getAddedKeys().get(0)));
            assertTrue(stringMapChange.getAddedObjects().contains(stringMapChange.getAddedElements().get(0)));

            assertEquals(Arrays.asList(stringMapChange), changeModel.getDirtyChanges());
        }
        updateAndAssertChangesFlushed(docView);
    }

    private AssertStatementBuilder assertReplaceAnd(AssertStatementBuilder builder) {
        return builder.assertDelete()
                    .forRelation(Document.class, "stringMap")
                .and()
                .assertInsert()
                    .forRelation(Document.class, "stringMap")
                .and();
    }

    @Override
    protected boolean isQueryStrategy() {
        // Collection changes always need to be applied on the entity model, can't do that via a query
        return false;
    }

    @Override
    protected AssertStatementBuilder fullFetch(AssertStatementBuilder builder) {
        return builder.assertSelect()
                .fetching(Document.class)
                .fetching(Document.class, "stringMap")
                .and();
    }

    @Override
    protected AssertStatementBuilder fullUpdate(AssertStatementBuilder builder) {
        return builder.assertUpdate()
                .forEntity(Document.class)
                .and();
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder.update(Document.class);
    }
}
