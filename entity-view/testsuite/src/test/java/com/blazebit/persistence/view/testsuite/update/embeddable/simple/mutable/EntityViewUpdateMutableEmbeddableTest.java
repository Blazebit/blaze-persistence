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

package com.blazebit.persistence.view.testsuite.update.embeddable.simple.mutable;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.NameObject;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.change.ChangeModel;
import com.blazebit.persistence.view.change.SingularChangeModel;
import com.blazebit.persistence.view.spi.type.AbstractMutableBasicUserType;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.embeddable.simple.AbstractEntityViewUpdateEmbeddableTest;
import com.blazebit.persistence.view.testsuite.update.embeddable.simple.mutable.model.UpdatableDocumentEmbeddableView;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateMutableEmbeddableTest extends AbstractEntityViewUpdateEmbeddableTest<UpdatableDocumentEmbeddableView> {

    private final boolean registerType;

    public EntityViewUpdateMutableEmbeddableTest(FlushMode mode, FlushStrategy strategy, boolean version, boolean registerType) {
        super(mode, strategy, version, UpdatableDocumentEmbeddableView.class);
        this.registerType = registerType;
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2} - KNOWN_TYPE={3}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_TYPE_COMBINATIONS;
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        if (registerType) {
            cfg.registerBasicUserType(NameObject.class, new AbstractMutableBasicUserType<NameObject>() {

                @Override
                public boolean supportsDeepCloning() {
                    return true;
                }

                @Override
                public NameObject deepClone(NameObject object) {
                    return new NameObject(object.getPrimaryName(), object.getSecondaryName(), object.getIntIdEntity());
                }
            });
        }
    }

    @Test
    public void testSimpleUpdate() {
        // Given & When
        // We don't actually update the embeddable in a partial mode, when knowing the type and having a query strategy
        final UpdatableDocumentEmbeddableView docView = simpleUpdate();
        Long oldVersion = docView.getVersion();
        if (!isFullMode()) {
            SingularChangeModel<UpdatableDocumentEmbeddableView> changeModel = evm.getChangeModel(docView);
            assertTrue(changeModel.isDirty());
            assertEquals(ChangeModel.ChangeKind.MUTATED, changeModel.getKind());

            ChangeModel<?> nameChange = changeModel.get("name");
            assertTrue(nameChange.isDirty());
            assertEquals(ChangeModel.ChangeKind.UPDATED, nameChange.getKind());

            ChangeModel<?> nameObjectChange = changeModel.get("nameObject");
            if (registerType) {
                assertFalse(nameObjectChange.isDirty());
                assertEquals(ChangeModel.ChangeKind.NONE, nameObjectChange.getKind());
                assertUnorderedEquals(Arrays.asList(nameChange), changeModel.getDirtyChanges());
            } else {
                assertTrue(nameObjectChange.isDirty());
                assertEquals(ChangeModel.ChangeKind.MUTATED, nameObjectChange.getKind());
                assertUnorderedEquals(Arrays.asList(nameChange, nameObjectChange), changeModel.getDirtyChanges());
            }

            assertEquals("doc1", nameChange.getInitialState());
            assertEquals("newDoc", nameChange.getCurrentState());
        }
        update(docView);
        assertEmptySimpleChangeModel(docView);

        // Then
        // Assert that not only the document is loaded and finally also updated
        AssertStatementBuilder builder = assertQuerySequence();
        // In partial mode with knowing the type we can determine the embeddable didn't change
        // So we can still use an update query since we don't need to include that attribute in the query
        boolean canUpdateQueryOnly = super.isQueryStrategy() && (
                jpaProvider.supportsUpdateSetEmbeddable() || !isFullMode() && registerType
                );

        if (!canUpdateQueryOnly) {
            fullFetch(builder);
        }

        builder.update(Document.class)
                .validate();

        if (registerType) {
            // When we register a type that can check for the dirtyness, we can skip the reload
            assertVersionDiff(oldVersion, docView.getVersion(), 1, 1);
            assertNoUpdateAndReload(docView);
            assertVersionDiff(oldVersion, docView.getVersion(), 1, 2);
        } else {
            // Unfortunately we have to reload the document since we don't know if the embeddable is dirty
            assertVersionDiff(oldVersion, docView.getVersion(), 1, 1);
            assertNoUpdateFullFetchAndReload(docView);
            assertVersionDiff(oldVersion, docView.getVersion(), 2, 2);
        }
        assertEquals("newDoc", docView.getName());
        assertEquals(doc1.getName(), docView.getName());
    }

    @Test
    public void testUpdateMutable() {
        // Given & When
        final UpdatableDocumentEmbeddableView docView = updateMutable();
        Long oldVersion = docView.getVersion();
        if (!isFullMode()) {
            SingularChangeModel<UpdatableDocumentEmbeddableView> changeModel = evm.getChangeModel(docView);
            assertTrue(changeModel.isDirty());
            assertEquals(ChangeModel.ChangeKind.MUTATED, changeModel.getKind());

            ChangeModel<?> nameChange = changeModel.get("nameObject");
            assertTrue(nameChange.isDirty());
            if (registerType) {
                assertEquals(ChangeModel.ChangeKind.UPDATED, nameChange.getKind());
            } else {
                assertEquals(ChangeModel.ChangeKind.UPDATED, nameChange.getKind());
            }

            assertEquals(new NameObject("newPrimaryName", "newSecondaryName"), nameChange.getCurrentState());
            assertEquals(Arrays.asList(nameChange), changeModel.getDirtyChanges());
        }
        updateAndAssertChangesFlushed(docView);

        // Then
        assertVersionDiff(oldVersion, docView.getVersion(), 1, 1);
        fullFetchUpdateAndReload(docView);
        assertVersionDiff(oldVersion, docView.getVersion(), 1, 2);
        assertEquals("newPrimaryName", doc1.getNameObject().getPrimaryName());
        assertEquals("newSecondaryName", doc1.getNameObject().getSecondaryName());
    }

    @Test
    public void testMutateMutable() {
        // Given & When
        final UpdatableDocumentEmbeddableView docView = mutateMutable();
        Long oldVersion = docView.getVersion();
        if (!isFullMode()) {
            SingularChangeModel<UpdatableDocumentEmbeddableView> changeModel = evm.getChangeModel(docView);
            assertTrue(changeModel.isDirty());
            assertEquals(ChangeModel.ChangeKind.MUTATED, changeModel.getKind());

            ChangeModel<?> nameChange = changeModel.get("nameObject");
            assertTrue(nameChange.isDirty());
            if (registerType) {
                assertEquals(ChangeModel.ChangeKind.UPDATED, nameChange.getKind());
            } else {
                assertEquals(ChangeModel.ChangeKind.MUTATED, nameChange.getKind());
            }

            assertEquals(new NameObject("newPrimaryName", "newSecondaryName"), nameChange.getCurrentState());
            assertEquals(Arrays.asList(nameChange), changeModel.getDirtyChanges());
        }
        updateAndAssertChangesFlushed(docView);

        // Then
        assertVersionDiff(oldVersion, docView.getVersion(), 1, 1);
        fullFetchUpdateAndReload(docView);
        assertVersionDiff(oldVersion, docView.getVersion(), 1, 2);
        assertEquals("newPrimaryName", doc1.getNameObject().getPrimaryName());
        assertEquals("newSecondaryName", doc1.getNameObject().getSecondaryName());
    }

    protected void assertEmptySimpleChangeModel(UpdatableDocumentEmbeddableView docView) {
        if (!isFullMode()) {
            SingularChangeModel<UpdatableDocumentEmbeddableView> changeModel = evm.getChangeModel(docView);
            ChangeModel<?> nameChange = changeModel.get("nameObject");
            if (registerType) {
                assertFalse(changeModel.isDirty());
                assertEquals(ChangeModel.ChangeKind.NONE, changeModel.getKind());

                assertFalse(nameChange.isDirty());
                assertEquals(ChangeModel.ChangeKind.NONE, nameChange.getKind());

                assertTrue(changeModel.getDirtyChanges().isEmpty());
            } else {
                assertTrue(changeModel.isDirty());
                assertEquals(ChangeModel.ChangeKind.MUTATED, changeModel.getKind());

                assertTrue(nameChange.isDirty());
                assertEquals(ChangeModel.ChangeKind.MUTATED, nameChange.getKind());

                assertEquals(Arrays.asList(nameChange), changeModel.getDirtyChanges());
            }

            assertEquals(new NameObject("doc1", null), nameChange.getCurrentState());
        }
    }

    protected void assertEmptyChangeModel(UpdatableDocumentEmbeddableView docView) {
        if (!isFullMode()) {
            SingularChangeModel<UpdatableDocumentEmbeddableView> changeModel = evm.getChangeModel(docView);
            ChangeModel<?> nameChange = changeModel.get("nameObject");
            if (registerType) {
                assertFalse(changeModel.isDirty());
                assertEquals(ChangeModel.ChangeKind.NONE, changeModel.getKind());

                assertFalse(nameChange.isDirty());
                assertEquals(ChangeModel.ChangeKind.NONE, nameChange.getKind());

                assertTrue(changeModel.getDirtyChanges().isEmpty());
            } else {
                assertTrue(changeModel.isDirty());
                assertEquals(ChangeModel.ChangeKind.MUTATED, changeModel.getKind());

                assertTrue(nameChange.isDirty());
                assertEquals(ChangeModel.ChangeKind.MUTATED, nameChange.getKind());

                assertEquals(Arrays.asList(nameChange), changeModel.getDirtyChanges());
            }

            assertEquals(new NameObject("newPrimaryName", "newSecondaryName"), nameChange.getCurrentState());
        }
    }

    private void fullFetchUpdateAndReload(UpdatableDocumentEmbeddableView docView) {
        // Assert that not only the document is loaded and finally also updated
        AssertStatementBuilder builder = assertQuerySequence();

        if (!isQueryStrategy()) {
            fullFetch(builder);
        }

        builder.update(Document.class)
                .validate();

        if (registerType) {
            // When we register a type that can check for the dirtyness, we can skip the reload
            assertNoUpdateAndReload(docView);
        } else {
            // Unfortunately we have to reload the document since we don't know if the embeddable is dirty
            assertNoUpdateFullFetchAndReload(docView);
        }
    }

    protected void assertVersionDiff(long oldVersion, long currentVersion, long diff, long fullDiff) {
        if (registerType) {
            super.assertVersionDiff(oldVersion, currentVersion, diff, fullDiff);
        } else {
            super.assertVersionDiff(oldVersion, currentVersion, fullDiff, fullDiff);
        }
    }

    protected void assertNoUpdateFullFetchAndReload(UpdatableDocumentEmbeddableView docView) {
        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);

        if (isFullMode()) {
            if (isQueryStrategy()) {
                fullUpdate(afterBuilder);
            } else {
                fullFetch(afterBuilder);
                if (version) {
                    afterBuilder.update(Document.class);
                }
            }
        } else {
            if (version) {
                afterBuilder.select(Document.class)
                        .update(Document.class);
            } else {
                if (!registerType) {
                    fullFetch(afterBuilder);
                }
            }
        }
        afterBuilder.validate();
        restartTransactionAndReload();
    }

    @Override
    protected boolean isQueryStrategy() {
        return jpaProvider.supportsUpdateSetEmbeddable() && super.isQueryStrategy();
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
