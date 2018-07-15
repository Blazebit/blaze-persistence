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
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate50;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate51;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.NameObject;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.change.ChangeModel;
import com.blazebit.persistence.view.change.PluralChangeModel;
import com.blazebit.persistence.view.change.SingularChangeModel;
import com.blazebit.persistence.view.spi.type.AbstractMutableBasicUserType;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.embeddable.simple.AbstractEntityViewUpdateEmbeddableCollectionsTest;
import com.blazebit.persistence.view.testsuite.update.embeddable.simple.mutable.model.UpdatableDocumentEmbeddableWithCollectionsView;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
// NOTE: Only the latest Hibernate 5.2 properly implements support for selecting element collections
// NOTE: No Datanucleus support yet
@Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class, NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateMutableEmbeddableCollectionsTest extends AbstractEntityViewUpdateEmbeddableCollectionsTest<UpdatableDocumentEmbeddableWithCollectionsView> {

    private boolean registerType;

    public EntityViewUpdateMutableEmbeddableCollectionsTest(FlushMode mode, FlushStrategy strategy, boolean version, boolean registerType) {
        super(mode, strategy, version, UpdatableDocumentEmbeddableWithCollectionsView.class);
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
    public void testUpdateReplaceCollection() {
        // Given
        final UpdatableDocumentEmbeddableWithCollectionsView docView = updateReplaceCollection();
        Long oldVersion = docView.getVersion();
        assertMutableChangeModel(docView);
        update(docView);
        assertMutableChangeModel(docView);

        // Then
        AssertStatementBuilder builder = assertQuerySequence();

        if (isFullMode()) {
            fullFetch(builder);

            if (version) {
                builder.update(Document.class);
            }
            if (!registerType) {
                assertReplaceAnd(builder);
            }
        } else {
            if (registerType && preferLoadingAndDiffingOverRecreate()) {
                fullFetch(builder);
            } else {
                builder.select(Document.class);
                if (version) {
                    builder.update(Document.class);
                }
                assertReplaceAnd(builder);
            }
        }

        builder.validate();
        if (registerType) {
            assertVersionDiff(oldVersion, docView.getVersion(), 0, 1);
        } else {
            assertVersionDiff(oldVersion, docView.getVersion(), 1, 1);
        }

        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);

        if (isFullMode()) {
            fullFetch(afterBuilder);
            if (version) {
                afterBuilder.update(Document.class);
            }
            if (!registerType) {
                assertReplaceAnd(afterBuilder);
            }
        } else {
            if (registerType && preferLoadingAndDiffingOverRecreate()) {
                fullFetch(afterBuilder);
            } else {
                afterBuilder.select(Document.class);
                if (version) {
                    afterBuilder.update(Document.class);
                }
                assertReplaceAnd(afterBuilder);
            }
        }

        afterBuilder.validate();
        if (registerType) {
            assertVersionDiff(oldVersion, docView.getVersion(), 0, 2);
        } else {
            assertVersionDiff(oldVersion, docView.getVersion(), 2, 2);
        }
        assertEquals(doc1.getNames(), docView.getNames());
    }

    @Test
    public void testUpdateAddToCollection() {
        // Given
        final UpdatableDocumentEmbeddableWithCollectionsView docView = updateAddToCollection();
        Long oldVersion = docView.getVersion();
        assertChangesUpdateAndFlush(docView);

        // Then
        AssertStatementBuilder builder = assertQuerySequence();

        if (isFullMode()) {
            fullFetch(builder);

            if (version) {
                builder.update(Document.class);
            }
            if (!registerType) {
                assertReplaceAnd(builder);
            }
        } else {
            if (registerType && preferLoadingAndDiffingOverRecreate()) {
                fullFetch(builder);

                if (version) {
                    builder.update(Document.class);
                }
            } else {
                builder.select(Document.class);

                if (version) {
                    builder.update(Document.class);
                }
                assertReplaceAnd(builder);
            }
        }

        builder.assertInsert()
                    .forRelation(Document.class, "names")
                .validate();
        assertVersionDiff(oldVersion, docView.getVersion(), 1, 1);

        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);

        if (isFullMode()) {
            fullFetch(afterBuilder);
            if (version) {
                afterBuilder.update(Document.class);
            }
            if (!registerType) {
                assertReplaceAnd(afterBuilder);
            }
        } else {
            if (registerType && preferLoadingAndDiffingOverRecreate()) {
                fullFetch(afterBuilder);
            } else {
                afterBuilder.select(Document.class);
                if (version) {
                    afterBuilder.update(Document.class);
                }
                assertReplaceAnd(afterBuilder);
            }
        }

        if (!registerType) {
            afterBuilder.assertInsert()
                        .forRelation(Document.class, "names")
                    .and();
            assertVersionDiff(oldVersion, docView.getVersion(), 2, 2);
        } else {
            assertVersionDiff(oldVersion, docView.getVersion(), 1, 2);
        }

        afterBuilder.validate();
        assertEquals(doc1.getNames(), docView.getNames());
    }

    @Test
    public void testUpdateAddToNewCollection() {
        // Given
        final UpdatableDocumentEmbeddableWithCollectionsView docView = updateAddToNewCollection();
        Long oldVersion = docView.getVersion();
        assertChangesUpdateAndFlush(docView);

        // Then
        AssertStatementBuilder builder = assertQuerySequence();

        if (isFullMode()) {
            fullFetch(builder);

            if (version) {
                builder.update(Document.class);
            }
            if (!registerType) {
                assertReplaceAnd(builder);
            }
        } else {
            if (registerType && preferLoadingAndDiffingOverRecreate()) {
                fullFetch(builder);

                if (version) {
                    builder.update(Document.class);
                }
            } else {
                builder.select(Document.class);

                if (version) {
                    builder.update(Document.class);
                }
                assertReplaceAnd(builder);
            }
        }

        builder.assertInsert()
                .forRelation(Document.class, "names")
            .validate();
        assertVersionDiff(oldVersion, docView.getVersion(), 1, 1);

        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);

        if (isFullMode()) {
            fullFetch(afterBuilder);
            if (version) {
                afterBuilder.update(Document.class);
            }
            if (!registerType) {
                assertReplaceAnd(afterBuilder);
            }
        } else {
            if (registerType && preferLoadingAndDiffingOverRecreate()) {
                fullFetch(afterBuilder);
            } else {
                afterBuilder.select(Document.class);
                if (version) {
                    afterBuilder.update(Document.class);
                }
                assertReplaceAnd(afterBuilder);
            }
        }

        if (!registerType) {
            afterBuilder.assertInsert()
                        .forRelation(Document.class, "names")
                    .and();
            assertVersionDiff(oldVersion, docView.getVersion(), 2, 2);
        } else {
            assertVersionDiff(oldVersion, docView.getVersion(), 1, 2);
        }

        afterBuilder.validate();
        assertEquals(doc1.getNames(), docView.getNames());
    }

    protected void assertChangesUpdateAndFlush(UpdatableDocumentEmbeddableWithCollectionsView docView) {
        if (!isFullMode()) {
            SingularChangeModel<UpdatableDocumentEmbeddableWithCollectionsView> changeModel = evm.getChangeModel(docView);
            PluralChangeModel<?, ?> namesChange = (PluralChangeModel<?, ?>) changeModel.get("names");

            assertTrue(changeModel.isDirty());
            assertEquals(ChangeModel.ChangeKind.MUTATED, changeModel.getKind());

            assertTrue(namesChange.isDirty());
            assertEquals(ChangeModel.ChangeKind.MUTATED, namesChange.getKind());

            if (registerType) {
                assertEquals(1, namesChange.getAddedElements().size());
                assertEquals(0, namesChange.getRemovedElements().size());
                assertEquals(0, namesChange.getMutatedElements().size());

                assertUnorderedEquals(Arrays.asList(namesChange.getAddedElements().get(0)), namesChange.getElementChanges());
            } else {
                assertEquals(2, namesChange.getAddedElements().size());
                assertEquals(1, namesChange.getRemovedElements().size());
                assertEquals(0, namesChange.getMutatedElements().size());
                assertEquals(ChangeModel.ChangeKind.UPDATED, namesChange.getAddedElements().get(0).getKind());
                assertEquals(new NameObject("doc1", "doc1"), namesChange.getAddedElements().get(0).getCurrentState());
                assertEquals(ChangeModel.ChangeKind.UPDATED, namesChange.getAddedElements().get(1).getKind());
                assertEquals(new NameObject("newPrimaryName", "newSecondaryName"), namesChange.getAddedElements().get(1).getCurrentState());
                assertEquals(ChangeModel.ChangeKind.UPDATED, namesChange.getRemovedElements().get(0).getKind());
                assertEquals(new NameObject("doc1", "doc1"), namesChange.getRemovedElements().get(0).getInitialState());

                assertUnorderedEquals(Arrays.asList(namesChange.getAddedElements().get(0), namesChange.getAddedElements().get(1), namesChange.getRemovedElements().get(0)), namesChange.getElementChanges());
            }

            assertEquals(ChangeModel.ChangeKind.UPDATED, namesChange.getAddedElements().get(namesChange.getAddedElements().size() - 1).getKind());
            assertNull(namesChange.getAddedElements().get(namesChange.getAddedElements().size() - 1).getInitialState());
            assertEquals(new NameObject("newPrimaryName", "newSecondaryName"), namesChange.getAddedElements().get(namesChange.getAddedElements().size() - 1).getCurrentState());
            assertEquals(Arrays.asList(namesChange), changeModel.getDirtyChanges());
        }
        update(docView);
        if (!isFullMode()) {
            SingularChangeModel<UpdatableDocumentEmbeddableWithCollectionsView> changeModel = evm.getChangeModel(docView);
            PluralChangeModel<?, ?> namesChange = (PluralChangeModel<?, ?>) changeModel.get("names");

            if (registerType) {
                assertFalse(changeModel.isDirty());
                assertEquals(ChangeModel.ChangeKind.NONE, changeModel.getKind());

                assertFalse(namesChange.isDirty());
                assertEquals(ChangeModel.ChangeKind.NONE, namesChange.getKind());

                assertEquals(0, namesChange.getAddedElements().size());
                assertEquals(0, namesChange.getRemovedElements().size());
                assertEquals(0, namesChange.getMutatedElements().size());
                assertEquals(0, namesChange.getElementChanges().size());

                assertEquals(0, changeModel.getDirtyChanges().size());
            } else {
                assertTrue(changeModel.isDirty());
                assertEquals(ChangeModel.ChangeKind.MUTATED, changeModel.getKind());

                assertTrue(namesChange.isDirty());
                assertEquals(ChangeModel.ChangeKind.MUTATED, namesChange.getKind());

                assertEquals(2, namesChange.getAddedElements().size());
                assertEquals(2, namesChange.getRemovedElements().size());
                assertEquals(0, namesChange.getMutatedElements().size());
                assertEquals(ChangeModel.ChangeKind.UPDATED, namesChange.getAddedElements().get(0).getKind());
                assertEquals(ChangeModel.ChangeKind.UPDATED, namesChange.getAddedElements().get(1).getKind());
                assertUnorderedEquals(Arrays.asList(namesChange.getAddedElements().get(0), namesChange.getAddedElements().get(1), namesChange.getRemovedElements().get(0), namesChange.getRemovedElements().get(1)), namesChange.getElementChanges());

                assertEquals(new NameObject("doc1", "doc1"), namesChange.getAddedElements().get(0).getCurrentState());
                assertEquals(new NameObject("newPrimaryName", "newSecondaryName"), namesChange.getAddedElements().get(1).getCurrentState());
                assertEquals(new NameObject("doc1", "doc1"), namesChange.getRemovedElements().get(0).getInitialState());
                assertEquals(new NameObject("newPrimaryName", "newSecondaryName"), namesChange.getRemovedElements().get(1).getInitialState());
                assertEquals(Arrays.asList(namesChange), changeModel.getDirtyChanges());
            }
        }
    }

    private void assertMutableChangeModel(UpdatableDocumentEmbeddableWithCollectionsView docView) {
        if (!isFullMode()) {
            SingularChangeModel<UpdatableDocumentEmbeddableWithCollectionsView> changeModel = evm.getChangeModel(docView);
            PluralChangeModel<?, ?> namesChange = (PluralChangeModel<?, ?>) changeModel.get("names");

            if (registerType) {
                assertFalse(changeModel.isDirty());
                assertEquals(ChangeModel.ChangeKind.NONE, changeModel.getKind());

                assertFalse(namesChange.isDirty());
                assertEquals(ChangeModel.ChangeKind.NONE, namesChange.getKind());

                assertEquals(0, namesChange.getAddedElements().size());
                assertEquals(0, namesChange.getRemovedElements().size());
                assertEquals(0, namesChange.getMutatedElements().size());
                assertEquals(0, namesChange.getElementChanges().size());

                assertEquals(0, changeModel.getDirtyChanges().size());
            } else {
                assertTrue(changeModel.isDirty());
                assertEquals(ChangeModel.ChangeKind.MUTATED, changeModel.getKind());

                assertTrue(namesChange.isDirty());
                assertEquals(ChangeModel.ChangeKind.MUTATED, namesChange.getKind());

                assertEquals(1, namesChange.getAddedElements().size());
                assertEquals(1, namesChange.getRemovedElements().size());
                assertEquals(0, namesChange.getMutatedElements().size());
                assertEquals(ChangeModel.ChangeKind.UPDATED, namesChange.getAddedElements().get(0).getKind());
                assertEquals(ChangeModel.ChangeKind.UPDATED, namesChange.getRemovedElements().get(0).getKind());

                assertUnorderedEquals(Arrays.asList(namesChange.getAddedElements().get(0), namesChange.getRemovedElements().get(0)), namesChange.getElementChanges());
                assertEquals(new NameObject("doc1", "doc1"), namesChange.getAddedElements().get(0).getCurrentState());
                assertEquals(new NameObject("doc1", "doc1"), namesChange.getRemovedElements().get(0).getInitialState());
                assertEquals(Arrays.asList(namesChange), changeModel.getDirtyChanges());
            }
        }
    }

    private AssertStatementBuilder assertReplaceAnd(AssertStatementBuilder builder) {
        return builder.assertDelete()
                    .forRelation(Document.class, "names")
                .and()
                .assertInsert()
                    .forRelation(Document.class, "names")
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
                .fetching(Document.class, "names")
                .and();
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder.update(Document.class);
    }
}
