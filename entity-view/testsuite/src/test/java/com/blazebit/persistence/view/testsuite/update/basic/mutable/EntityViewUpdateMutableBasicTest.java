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
import com.blazebit.persistence.view.change.SingularChangeModel;
import com.blazebit.persistence.view.testsuite.update.basic.AbstractEntityViewUpdateBasicTest;
import com.blazebit.persistence.view.testsuite.update.basic.mutable.model.UpdatableDocumentBasicView;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateMutableBasicTest extends AbstractEntityViewUpdateBasicTest<UpdatableDocumentBasicView> {

    public EntityViewUpdateMutableBasicTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentBasicView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Test
    public void testSimpleUpdate() {
        // Given & When
        final UpdatableDocumentBasicView docView = simpleUpdate();
        Long oldVersion = docView.getVersion();
        if (!isFullMode()) {
            SingularChangeModel<UpdatableDocumentBasicView> changeModel = evm.getChangeModel(docView);
            assertTrue(changeModel.isDirty());
            assertEquals(ChangeModel.ChangeKind.MUTATED, changeModel.getKind());

            ChangeModel<?> nameChange = changeModel.get("name");
            assertTrue(nameChange.isDirty());
            assertEquals(ChangeModel.ChangeKind.UPDATED, nameChange.getKind());

            assertEquals("doc1", nameChange.getInitialState());
            assertEquals("newDoc", nameChange.getCurrentState());
            assertEquals(Arrays.asList(nameChange), changeModel.getDirtyChanges());
        }
        updateAndAssertChangesFlushed(docView);

        // Then
        assertVersionDiff(oldVersion, docView.getVersion(), 1, 1);
        fullFetchUpdateAndReload(docView);
        assertVersionDiff(oldVersion, docView.getVersion(), 1, 2);
        assertEquals("newDoc", docView.getName());
        assertEquals(doc1.getName(), docView.getName());
        assertEquals(doc1.getVersion(), docView.getVersion());
    }

    @Test
    public void testUpdateMutable() {
        // Given & When
        final UpdatableDocumentBasicView docView = updateMutable();
        Long oldVersion = docView.getVersion();
        if (!isFullMode()) {
            SingularChangeModel<UpdatableDocumentBasicView> changeModel = evm.getChangeModel(docView);
            assertTrue(changeModel.isDirty());
            assertEquals(ChangeModel.ChangeKind.MUTATED, changeModel.getKind());

            ChangeModel<?> lastModifiedChange = changeModel.get("lastModified");
            assertTrue(lastModifiedChange.isDirty());
            assertEquals(ChangeModel.ChangeKind.UPDATED, lastModifiedChange.getKind());

            assertEquals(new Date(EPOCH_2K), lastModifiedChange.getInitialState());
            assertEquals(new Date(0), lastModifiedChange.getCurrentState());
            assertEquals(Arrays.asList(lastModifiedChange), changeModel.getDirtyChanges());
        }
        updateAndAssertChangesFlushed(docView);

        // Then
        assertVersionDiff(oldVersion, docView.getVersion(), 1, 1);
        fullFetchUpdateAndReload(docView);
        assertVersionDiff(oldVersion, docView.getVersion(), 1, 2);
        assertEquals(0, doc1.getLastModified().getTime());
        assertEquals(doc1.getVersion(), docView.getVersion());
    }

    @Test
    public void testMutateMutable() {
        // Given & When
        final UpdatableDocumentBasicView docView = mutateMutable();
        Long oldVersion = docView.getVersion();
        if (!isFullMode()) {
            SingularChangeModel<UpdatableDocumentBasicView> changeModel = evm.getChangeModel(docView);
            assertTrue(changeModel.isDirty());
            assertEquals(ChangeModel.ChangeKind.MUTATED, changeModel.getKind());

            ChangeModel<?> lastModifiedChange = changeModel.get("lastModified");
            assertTrue(lastModifiedChange.isDirty());
            assertEquals(ChangeModel.ChangeKind.UPDATED, lastModifiedChange.getKind());

            assertEquals(new Date(EPOCH_2K), lastModifiedChange.getInitialState());
            assertEquals(new Date(0), lastModifiedChange.getCurrentState());
            assertEquals(Arrays.asList(lastModifiedChange), changeModel.getDirtyChanges());
        }
        updateAndAssertChangesFlushed(docView);

        // Then
        assertVersionDiff(oldVersion, docView.getVersion(), 1, 1);
        fullFetchUpdateAndReload(docView);
        assertVersionDiff(oldVersion, docView.getVersion(), 1, 2);
        assertEquals(0, doc1.getLastModified().getTime());
        assertEquals(doc1.getVersion(), docView.getVersion());
    }

    private void fullFetchUpdateAndReload(UpdatableDocumentBasicView docView) {
        // Assert that not only the document is loaded and finally also updated
        AssertStatementBuilder builder = assertQuerySequence();

        if (!isQueryStrategy()) {
            fullFetch(builder);
        }

        builder.update(Document.class)
                .validate();

        assertNoUpdateAndReload(docView);
    }

    @Override
    protected AssertStatementBuilder fullFetch(AssertStatementBuilder builder) {
        return builder.assertSelect()
                .fetching(Document.class)
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
