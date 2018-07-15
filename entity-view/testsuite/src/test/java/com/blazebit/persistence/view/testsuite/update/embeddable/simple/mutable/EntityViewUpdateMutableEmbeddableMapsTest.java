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
import com.blazebit.persistence.view.change.MapChangeModel;
import com.blazebit.persistence.view.change.SingularChangeModel;
import com.blazebit.persistence.view.spi.type.AbstractMutableBasicUserType;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.embeddable.simple.AbstractEntityViewUpdateEmbeddableMapsTest;
import com.blazebit.persistence.view.testsuite.update.embeddable.simple.mutable.model.UpdatableDocumentEmbeddableWithMapsView;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
// NOTE: Only the latest Hibernate 5.2 properly implements support for selecting element collections
// NOTE: No Datanucleus support yet
@Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class, NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateMutableEmbeddableMapsTest extends AbstractEntityViewUpdateEmbeddableMapsTest<UpdatableDocumentEmbeddableWithMapsView> {

    private boolean registerType;

    public EntityViewUpdateMutableEmbeddableMapsTest(FlushMode mode, FlushStrategy strategy, boolean version, boolean registerType) {
        super(mode, strategy, version, UpdatableDocumentEmbeddableWithMapsView.class);
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
        final UpdatableDocumentEmbeddableWithMapsView docView = updateReplaceCollection();
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
        assertEquals(doc1.getNameMap(), docView.getNameMap());
    }

    @Test
    public void testUpdateAddToCollection() {
        // Given
        final UpdatableDocumentEmbeddableWithMapsView docView = updateAddToCollection();
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
                    .forRelation(Document.class, "nameMap")
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
                        .forRelation(Document.class, "nameMap")
                    .and();
            assertVersionDiff(oldVersion, docView.getVersion(), 2, 2);
        } else {
            assertVersionDiff(oldVersion, docView.getVersion(), 1, 2);
        }

        afterBuilder.validate();
        assertEquals(doc1.getNameMap(), docView.getNameMap());
    }

    @Test
    public void testUpdateAddToNewCollection() {
        // Given
        final UpdatableDocumentEmbeddableWithMapsView docView = updateAddToNewCollection();
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
                .forRelation(Document.class, "nameMap")
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
                        .forRelation(Document.class, "nameMap")
                    .and();
            assertVersionDiff(oldVersion, docView.getVersion(), 2, 2);
        } else {
            assertVersionDiff(oldVersion, docView.getVersion(), 1, 2);
        }

        afterBuilder.validate();
        assertEquals(doc1.getNameMap(), docView.getNameMap());
    }

    protected void assertChangesUpdateAndFlush(UpdatableDocumentEmbeddableWithMapsView docView) {
        if (!isFullMode()) {
            SingularChangeModel<UpdatableDocumentEmbeddableWithMapsView> changeModel = evm.getChangeModel(docView);
            assertTrue(changeModel.isDirty());
            assertEquals(ChangeModel.ChangeKind.MUTATED, changeModel.getKind());

            MapChangeModel<?, ?> nameMapChange = (MapChangeModel<Object, Object>) changeModel.<Map<Object, Object>>get("nameMap");
            assertTrue(nameMapChange.isDirty());
            assertEquals(ChangeModel.ChangeKind.MUTATED, nameMapChange.getKind());

            if (registerType) {
                assertEquals(1, nameMapChange.getAddedElements().size());
                assertEquals(0, nameMapChange.getRemovedElements().size());
                assertEquals(0, nameMapChange.getMutatedElements().size());
                assertEquals(1, nameMapChange.getElementChanges().size());

                assertEquals(ChangeModel.ChangeKind.UPDATED, nameMapChange.getAddedElements().get(0).getKind());
                assertNull(nameMapChange.getAddedElements().get(0).getInitialState());
                assertEquals(new NameObject("newPrimaryName", "newSecondaryName"), nameMapChange.getAddedElements().get(0).getCurrentState());

                assertUnorderedEquals(Arrays.asList(nameMapChange.getAddedElements().get(0)), nameMapChange.getElementChanges());

                assertEquals(1, nameMapChange.getAddedKeys().size());
                assertEquals(0, nameMapChange.getRemovedKeys().size());
                assertEquals(0, nameMapChange.getMutatedKeys().size());
                assertEquals(1, nameMapChange.getKeyChanges().size());

                assertEquals(ChangeModel.ChangeKind.UPDATED, nameMapChange.getAddedKeys().get(0).getKind());
                assertEquals("newPrimaryName", nameMapChange.getAddedKeys().get(0).getCurrentState());

                assertUnorderedEquals(Arrays.asList(nameMapChange.getAddedKeys().get(0)), nameMapChange.getKeyChanges());

                assertEquals(2, nameMapChange.getAddedObjects().size());
                assertEquals(0, nameMapChange.getRemovedObjects().size());
                assertEquals(0, nameMapChange.getMutatedObjects().size());
                assertEquals(2, nameMapChange.getObjectChanges().size());

                assertUnorderedEquals(Arrays.asList(
                        nameMapChange.getAddedKeys().get(0),
                        nameMapChange.getAddedElements().get(0)
                ), nameMapChange.getAddedObjects());

                assertEquals(0, nameMapChange.getMutatedObjects().size());

                assertUnorderedEquals(Arrays.asList(
                        nameMapChange.getAddedKeys().get(0),
                        nameMapChange.getAddedElements().get(0)
                ), nameMapChange.getObjectChanges());
            } else {
                assertEquals(1, nameMapChange.getAddedElements().size());
                assertEquals(0, nameMapChange.getRemovedElements().size());
                assertEquals(1, nameMapChange.getMutatedElements().size());
                assertEquals(2, nameMapChange.getElementChanges().size());

                assertEquals(ChangeModel.ChangeKind.UPDATED, nameMapChange.getAddedElements().get(0).getKind());
                assertNull(nameMapChange.getAddedElements().get(0).getInitialState());
                assertEquals(new NameObject("newPrimaryName", "newSecondaryName"), nameMapChange.getAddedElements().get(0).getCurrentState());

                assertEquals(ChangeModel.ChangeKind.MUTATED, nameMapChange.getMutatedElements().get(0).getKind());
                assertEquals(new NameObject("doc1", "doc1"), nameMapChange.getMutatedElements().get(0).getCurrentState());

                assertUnorderedEquals(Arrays.asList(
                        nameMapChange.getAddedElements().get(0),
                        nameMapChange.getMutatedElements().get(0)
                ), nameMapChange.getElementChanges());

                assertEquals(1, nameMapChange.getAddedKeys().size());
                assertEquals(0, nameMapChange.getRemovedKeys().size());
                assertEquals(0, nameMapChange.getMutatedKeys().size());
                assertEquals(1, nameMapChange.getKeyChanges().size());

                assertEquals(ChangeModel.ChangeKind.UPDATED, nameMapChange.getAddedKeys().get(0).getKind());
                assertEquals("newPrimaryName", nameMapChange.getAddedKeys().get(0).getCurrentState());

                assertUnorderedEquals(Arrays.asList(
                        nameMapChange.getAddedKeys().get(0)
                ), nameMapChange.getKeyChanges());

                assertEquals(2, nameMapChange.getAddedObjects().size());
                assertEquals(0, nameMapChange.getRemovedObjects().size());
                assertEquals(1, nameMapChange.getMutatedObjects().size());
                assertEquals(3, nameMapChange.getObjectChanges().size());

                assertUnorderedEquals(Arrays.asList(
                        nameMapChange.getAddedKeys().get(0),
                        nameMapChange.getAddedElements().get(0)
                ), nameMapChange.getAddedObjects());

                assertUnorderedEquals(Arrays.asList(
                        nameMapChange.getMutatedElements().get(0)
                ), nameMapChange.getMutatedObjects());

                assertUnorderedEquals(Arrays.asList(
                        nameMapChange.getAddedKeys().get(0),
                        nameMapChange.getAddedElements().get(0),
                        nameMapChange.getMutatedElements().get(0)
                ), nameMapChange.getObjectChanges());
            }

            assertUnorderedEquals(Arrays.asList(nameMapChange), changeModel.getDirtyChanges());
        }
        update(docView);
        if (!isFullMode()) {
            SingularChangeModel<UpdatableDocumentEmbeddableWithMapsView> changeModel = evm.getChangeModel(docView);
            MapChangeModel<?, ?> nameMapChange = (MapChangeModel<Object, Object>) changeModel.<Map<Object, Object>>get("nameMap");

            if (registerType) {
                assertFalse(changeModel.isDirty());
                assertEquals(ChangeModel.ChangeKind.NONE, changeModel.getKind());

                assertFalse(nameMapChange.isDirty());
                assertEquals(ChangeModel.ChangeKind.NONE, nameMapChange.getKind());

                assertEquals(0, nameMapChange.getAddedElements().size());
                assertEquals(0, nameMapChange.getRemovedElements().size());
                assertEquals(0, nameMapChange.getMutatedElements().size());
                assertEquals(0, nameMapChange.getElementChanges().size());

                assertEquals(0, nameMapChange.getAddedKeys().size());
                assertEquals(0, nameMapChange.getRemovedKeys().size());
                assertEquals(0, nameMapChange.getMutatedKeys().size());
                assertEquals(0, nameMapChange.getKeyChanges().size());

                assertEquals(0, nameMapChange.getAddedObjects().size());
                assertEquals(0, nameMapChange.getRemovedObjects().size());
                assertEquals(0, nameMapChange.getMutatedObjects().size());
                assertEquals(0, nameMapChange.getObjectChanges().size());

                assertEquals(0, changeModel.getDirtyChanges().size());
            } else {
                assertTrue(changeModel.isDirty());
                assertEquals(ChangeModel.ChangeKind.MUTATED, changeModel.getKind());

                assertTrue(nameMapChange.isDirty());
                assertEquals(ChangeModel.ChangeKind.MUTATED, nameMapChange.getKind());

                assertEquals(0, nameMapChange.getAddedElements().size());
                assertEquals(0, nameMapChange.getRemovedElements().size());
                assertEquals(2, nameMapChange.getMutatedElements().size());
                assertEquals(ChangeModel.ChangeKind.MUTATED, nameMapChange.getMutatedElements().get(0).getKind());
                assertEquals(ChangeModel.ChangeKind.MUTATED, nameMapChange.getMutatedElements().get(1).getKind());
                assertEquals(2, nameMapChange.getElementChanges().size());

                assertUnorderedEquals(Arrays.asList(
                        nameMapChange.getMutatedElements().get(0),
                        nameMapChange.getMutatedElements().get(1)
                ), nameMapChange.getElementChanges());

                assertUnorderedEquals(Arrays.asList(
                        new NameObject("doc1", "doc1"),
                        new NameObject("newPrimaryName", "newSecondaryName")
                ), Arrays.asList(
                        nameMapChange.getMutatedElements().get(0).getCurrentState(),
                        nameMapChange.getMutatedElements().get(1).getCurrentState()
                ));

                assertEquals(0, nameMapChange.getAddedKeys().size());
                assertEquals(0, nameMapChange.getRemovedKeys().size());
                assertEquals(0, nameMapChange.getMutatedKeys().size());
                assertEquals(0, nameMapChange.getKeyChanges().size());

                assertEquals(0, nameMapChange.getAddedObjects().size());
                assertEquals(0, nameMapChange.getRemovedObjects().size());
                assertEquals(2, nameMapChange.getMutatedObjects().size());
                assertEquals(2, nameMapChange.getObjectChanges().size());

                assertUnorderedEquals(Arrays.asList(
                        nameMapChange.getMutatedObjects().get(0),
                        nameMapChange.getMutatedObjects().get(1)
                ), nameMapChange.getObjectChanges());

                assertUnorderedEquals(Arrays.asList(nameMapChange), changeModel.getDirtyChanges());
            }
        }
    }

    private void assertMutableChangeModel(UpdatableDocumentEmbeddableWithMapsView docView) {
        if (!isFullMode()) {
            SingularChangeModel<UpdatableDocumentEmbeddableWithMapsView> changeModel = evm.getChangeModel(docView);
            MapChangeModel<?, ?> nameMapChange = (MapChangeModel<Object, Object>) changeModel.<Map<Object, Object>>get("nameMap");

            if (registerType) {
                assertFalse(changeModel.isDirty());
                assertEquals(ChangeModel.ChangeKind.NONE, changeModel.getKind());

                assertFalse(nameMapChange.isDirty());
                assertEquals(ChangeModel.ChangeKind.NONE, nameMapChange.getKind());

                assertEquals(0, nameMapChange.getAddedElements().size());
                assertEquals(0, nameMapChange.getRemovedElements().size());
                assertEquals(0, nameMapChange.getMutatedElements().size());
                assertEquals(0, nameMapChange.getElementChanges().size());

                assertEquals(0, nameMapChange.getAddedKeys().size());
                assertEquals(0, nameMapChange.getRemovedKeys().size());
                assertEquals(0, nameMapChange.getMutatedKeys().size());
                assertEquals(0, nameMapChange.getKeyChanges().size());

                assertEquals(0, nameMapChange.getAddedObjects().size());
                assertEquals(0, nameMapChange.getRemovedObjects().size());
                assertEquals(0, nameMapChange.getMutatedObjects().size());
                assertEquals(0, nameMapChange.getObjectChanges().size());

                assertEquals(0, changeModel.getDirtyChanges().size());
            } else {
                assertTrue(changeModel.isDirty());
                assertEquals(ChangeModel.ChangeKind.MUTATED, changeModel.getKind());

                assertTrue(nameMapChange.isDirty());
                assertEquals(ChangeModel.ChangeKind.MUTATED, nameMapChange.getKind());

                assertEquals(0, nameMapChange.getAddedElements().size());
                assertEquals(0, nameMapChange.getRemovedElements().size());
                assertEquals(1, nameMapChange.getMutatedElements().size());
                assertEquals(ChangeModel.ChangeKind.MUTATED, nameMapChange.getMutatedElements().get(0).getKind());
                assertEquals(new NameObject("doc1", "doc1"), nameMapChange.getMutatedElements().get(0).getCurrentState());

                assertEquals(0, nameMapChange.getAddedKeys().size());
                assertEquals(0, nameMapChange.getRemovedKeys().size());
                assertEquals(0, nameMapChange.getMutatedKeys().size());

                assertEquals(0, nameMapChange.getAddedObjects().size());
                assertEquals(0, nameMapChange.getRemovedObjects().size());
                assertEquals(1, nameMapChange.getMutatedObjects().size());
                assertTrue(nameMapChange.getMutatedObjects().contains(nameMapChange.getMutatedElements().get(0)));

                assertEquals(Arrays.asList(nameMapChange), changeModel.getDirtyChanges());
            }
        }
    }

    private AssertStatementBuilder assertReplaceAnd(AssertStatementBuilder builder) {
        return builder.assertDelete()
                    .forRelation(Document.class, "nameMap")
                .and()
                .assertInsert()
                    .forRelation(Document.class, "nameMap")
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
                .fetching(Document.class, "nameMap")
                .and();
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder.update(Document.class);
    }
}
