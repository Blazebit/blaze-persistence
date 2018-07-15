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

package com.blazebit.persistence.view.testsuite.update.embeddable.nested.mutable;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate50;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate51;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntity;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.entity.NameObject;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.change.ChangeModel;
import com.blazebit.persistence.view.change.MapChangeModel;
import com.blazebit.persistence.view.change.SingularChangeModel;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.spi.type.AbstractMutableBasicUserType;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.AbstractEntityViewUpdateNestedEmbeddableMapsTest;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.model.SimpleIntIdEntityView;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.model.SimpleNameObjectView;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.mutable.model.UpdatableEmbeddableEntityWithMapsView;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@RunWith(Parameterized.class)
// NOTE: Only the latest Hibernate 5.2 properly implements support for selecting element collections
// NOTE: No Datanucleus support yet
@Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class, NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateMutableNestedEmbeddableMapsTest extends AbstractEntityViewUpdateNestedEmbeddableMapsTest<UpdatableEmbeddableEntityWithMapsView> {

    public EntityViewUpdateMutableNestedEmbeddableMapsTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableEmbeddableEntityWithMapsView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Test
    public void testUpdateReplaceCollection() {
        // Given
        final UpdatableEmbeddableEntityWithMapsView docView = updateReplaceCollection();
        Long oldVersion = docView.getVersion();
        assertMutableChangeModel(docView);
        update(docView);
        assertMutableChangeModel(docView);

        // Then
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isFullMode()) {
            fullFetch(builder);
            // Since we detect nothing changed, there is no need to flush for version increments
            // See com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityEmbeddable.elementCollection2
            // Apparently, Hibernate has problems with handling this correctly
            builder.assertDelete()
                    .forRelation(EmbeddableTestEntity.class, "embeddable.elementCollection2")
                    .and();
        }

        builder.validate();
        assertVersionDiff(oldVersion, docView.getVersion(), 0, 0);

        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);

        if (isFullMode()) {
            fullFetch(afterBuilder);
            // Since we detect nothing changed, there is no need to flush for version increments
            // See com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityEmbeddable.elementCollection2
            // Apparently, Hibernate has problems with handling this correctly
            afterBuilder.assertDelete()
                    .forRelation(EmbeddableTestEntity.class, "embeddable.elementCollection2")
                    .and();
        }

        afterBuilder.validate();
        assertVersionDiff(oldVersion, docView.getVersion(), 0, 0);
        assertNameObjectSubviewEquals(entity1.getEmbeddable().getElementCollection(), docView.getEmbeddable().getElementCollection());
        assertSubviewEquals(entity1.getEmbeddable().getManyToMany(), docView.getEmbeddable().getManyToMany());
    }

    @Test
    public void testUpdateAddToCollection() {
        // Given
        final UpdatableEmbeddableEntityWithMapsView docView = updateAddToCollection();
        Long oldVersion = docView.getVersion();
        assertChangesUpdateAndFlush(docView);

        // Then
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isFullMode()) {
            fullFetch(builder);

            if (version) {
                builder.update(EmbeddableTestEntity.class);
            }
        } else {
            if (preferLoadingAndDiffingOverRecreate()) {
                fullFetch(builder);
                if (version) {
                    builder.update(EmbeddableTestEntity.class);
                }
            } else {
                builder.select(EmbeddableTestEntity.class);

                if (version) {
                    builder.update(EmbeddableTestEntity.class);
                }
                assertReplaceAnd(builder);
            }
        }

        if (!isQueryStrategy()) {
            // See com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityEmbeddable.elementCollection2
            // Apparently, Hibernate has problems with handling this correctly
            builder.assertDelete()
                    .forRelation(EmbeddableTestEntity.class, "embeddable.elementCollection2")
                    .and();
        }

        builder.assertInsert()
                    .forRelation(EmbeddableTestEntity.class, "embeddable.elementCollection")
                .and()
                .assertInsert()
                    .forRelation(EmbeddableTestEntity.class, "embeddable.manyToMany")
                .and()
                .validate();

        assertVersionDiff(oldVersion, docView.getVersion(), 1, 1);

        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);

        if (isFullMode()) {
            fullFetch(afterBuilder);
            // Since we detect nothing changed, there is no need to flush for version increments
            if (!isQueryStrategy()) {
                // See com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityEmbeddable.elementCollection2
                // Apparently, Hibernate has problems with handling this correctly
                afterBuilder.assertDelete()
                        .forRelation(EmbeddableTestEntity.class, "embeddable.elementCollection2")
                        .and();
            }
        }

        assertVersionDiff(oldVersion, docView.getVersion(), 1, 1);

        afterBuilder.validate();
        assertNameObjectSubviewEquals(entity1.getEmbeddable().getElementCollection(), docView.getEmbeddable().getElementCollection());
        assertSubviewEquals(entity1.getEmbeddable().getManyToMany(), docView.getEmbeddable().getManyToMany());
    }

    @Test
    public void testUpdateAddToNewCollection() {
        // Given
        final UpdatableEmbeddableEntityWithMapsView docView = updateAddToNewCollection();
        Long oldVersion = docView.getVersion();
        assertChangesUpdateAndFlush(docView);

        // Then
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isFullMode()) {
            fullFetch(builder);

            if (version) {
                builder.update(EmbeddableTestEntity.class);
            }
        } else {
            if (preferLoadingAndDiffingOverRecreate()) {
                fullFetch(builder);
                if (version) {
                    builder.update(EmbeddableTestEntity.class);
                }
            } else {
                builder.select(EmbeddableTestEntity.class);

                if (version) {
                    builder.update(EmbeddableTestEntity.class);
                }
                assertReplaceAnd(builder);
            }
        }

        if (!isQueryStrategy()) {
            // See com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityEmbeddable.elementCollection2
            // Apparently, Hibernate has problems with handling this correctly
            builder.assertDelete()
                    .forRelation(EmbeddableTestEntity.class, "embeddable.elementCollection2")
                    .and();
        }

        builder.assertInsert()
                    .forRelation(EmbeddableTestEntity.class, "embeddable.elementCollection")
                .and()
                .assertInsert()
                    .forRelation(EmbeddableTestEntity.class, "embeddable.manyToMany")
                .and()
                .validate();

        assertVersionDiff(oldVersion, docView.getVersion(), 1, 1);

        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);

        if (isFullMode()) {
            fullFetch(afterBuilder);
            // Since we detect nothing changed, there is no need to flush for version increments
            if (!isQueryStrategy()) {
                // See com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityEmbeddable.elementCollection2
                // Apparently, Hibernate has problems with handling this correctly
                afterBuilder.assertDelete()
                        .forRelation(EmbeddableTestEntity.class, "embeddable.elementCollection2")
                        .and();
            }
        }

        assertVersionDiff(oldVersion, docView.getVersion(), 1, 1);

        afterBuilder.validate();
        assertNameObjectSubviewEquals(entity1.getEmbeddable().getElementCollection(), docView.getEmbeddable().getElementCollection());
        assertSubviewEquals(entity1.getEmbeddable().getManyToMany(), docView.getEmbeddable().getManyToMany());
    }

    protected void assertChangesUpdateAndFlush(UpdatableEmbeddableEntityWithMapsView docView) {
//        if (!isFullMode()) {
//            SingularChangeModel<UpdatableEmbeddableEntityWithMapsView> changeModel = evm.getChangeModel(docView);
//            assertTrue(changeModel.isDirty());
//            assertEquals(ChangeModel.ChangeKind.MUTATED, changeModel.getKind());
//
//            MapChangeModel<?, ?> embeddable.elementCollectionChange = (MapChangeModel<Object, Object>) changeModel.<Map<Object, Object>>get("embeddable.elementCollection");
//            assertTrue(nameMapChange.isDirty());
//            assertEquals(ChangeModel.ChangeKind.MUTATED, nameMapChange.getKind());
//
//            if (registerType) {
//                assertEquals(1, nameMapChange.getAddedElements().size());
//                assertEquals(0, nameMapChange.getRemovedElements().size());
//                assertEquals(0, nameMapChange.getMutatedElements().size());
//                assertEquals(1, nameMapChange.getElementChanges().size());
//
//                assertEquals(ChangeModel.ChangeKind.UPDATED, nameMapChange.getAddedElements().get(0).getKind());
//                assertNull(nameMapChange.getAddedElements().get(0).getInitialState());
//                assertEquals(new NameObject("newPrimaryName", "newSecondaryName"), nameMapChange.getAddedElements().get(0).getCurrentState());
//
//                assertUnorderedEquals(Arrays.asList(nameMapChange.getAddedElements().get(0)), nameMapChange.getElementChanges());
//
//                assertEquals(1, nameMapChange.getAddedKeys().size());
//                assertEquals(0, nameMapChange.getRemovedKeys().size());
//                assertEquals(0, nameMapChange.getMutatedKeys().size());
//                assertEquals(1, nameMapChange.getKeyChanges().size());
//
//                assertEquals(ChangeModel.ChangeKind.UPDATED, nameMapChange.getAddedKeys().get(0).getKind());
//                assertEquals("newPrimaryName", nameMapChange.getAddedKeys().get(0).getCurrentState());
//
//                assertUnorderedEquals(Arrays.asList(nameMapChange.getAddedKeys().get(0)), nameMapChange.getKeyChanges());
//
//                assertEquals(2, nameMapChange.getAddedObjects().size());
//                assertEquals(0, nameMapChange.getRemovedObjects().size());
//                assertEquals(0, nameMapChange.getMutatedObjects().size());
//                assertEquals(2, nameMapChange.getObjectChanges().size());
//
//                assertUnorderedEquals(Arrays.asList(
//                        nameMapChange.getAddedKeys().get(0),
//                        nameMapChange.getAddedElements().get(0)
//                ), nameMapChange.getAddedObjects());
//
//                assertEquals(0, nameMapChange.getMutatedObjects().size());
//
//                assertUnorderedEquals(Arrays.asList(
//                        nameMapChange.getAddedKeys().get(0),
//                        nameMapChange.getAddedElements().get(0)
//                ), nameMapChange.getObjectChanges());
//            } else {
//                assertEquals(1, nameMapChange.getAddedElements().size());
//                assertEquals(0, nameMapChange.getRemovedElements().size());
//                assertEquals(1, nameMapChange.getMutatedElements().size());
//                assertEquals(2, nameMapChange.getElementChanges().size());
//
//                assertEquals(ChangeModel.ChangeKind.UPDATED, nameMapChange.getAddedElements().get(0).getKind());
//                assertNull(nameMapChange.getAddedElements().get(0).getInitialState());
//                assertEquals(new NameObject("newPrimaryName", "newSecondaryName"), nameMapChange.getAddedElements().get(0).getCurrentState());
//
//                assertEquals(ChangeModel.ChangeKind.MUTATED, nameMapChange.getMutatedElements().get(0).getKind());
//                assertEquals(new NameObject("doc1", "doc1"), nameMapChange.getMutatedElements().get(0).getCurrentState());
//
//                assertUnorderedEquals(Arrays.asList(
//                        nameMapChange.getAddedElements().get(0),
//                        nameMapChange.getMutatedElements().get(0)
//                ), nameMapChange.getElementChanges());
//
//                assertEquals(1, nameMapChange.getAddedKeys().size());
//                assertEquals(0, nameMapChange.getRemovedKeys().size());
//                assertEquals(0, nameMapChange.getMutatedKeys().size());
//                assertEquals(1, nameMapChange.getKeyChanges().size());
//
//                assertEquals(ChangeModel.ChangeKind.UPDATED, nameMapChange.getAddedKeys().get(0).getKind());
//                assertEquals("newPrimaryName", nameMapChange.getAddedKeys().get(0).getCurrentState());
//
//                assertUnorderedEquals(Arrays.asList(
//                        nameMapChange.getAddedKeys().get(0)
//                ), nameMapChange.getKeyChanges());
//
//                assertEquals(2, nameMapChange.getAddedObjects().size());
//                assertEquals(0, nameMapChange.getRemovedObjects().size());
//                assertEquals(1, nameMapChange.getMutatedObjects().size());
//                assertEquals(3, nameMapChange.getObjectChanges().size());
//
//                assertUnorderedEquals(Arrays.asList(
//                        nameMapChange.getAddedKeys().get(0),
//                        nameMapChange.getAddedElements().get(0)
//                ), nameMapChange.getAddedObjects());
//
//                assertUnorderedEquals(Arrays.asList(
//                        nameMapChange.getMutatedElements().get(0)
//                ), nameMapChange.getMutatedObjects());
//
//                assertUnorderedEquals(Arrays.asList(
//                        nameMapChange.getAddedKeys().get(0),
//                        nameMapChange.getAddedElements().get(0),
//                        nameMapChange.getMutatedElements().get(0)
//                ), nameMapChange.getObjectChanges());
//            }
//
//            assertUnorderedEquals(Arrays.asList(nameMapChange), changeModel.getDirtyChanges());
//        }
        update(docView);
//        if (!isFullMode()) {
//            SingularChangeModel<UpdatableEmbeddableEntityWithMapsView> changeModel = evm.getChangeModel(docView);
//            MapChangeModel<?, ?> nameMapChange = (MapChangeModel<Object, Object>) changeModel.<Map<Object, Object>>get("embeddable.elementCollection");
//
//            if (registerType) {
//                assertFalse(changeModel.isDirty());
//                assertEquals(ChangeModel.ChangeKind.NONE, changeModel.getKind());
//
//                assertFalse(nameMapChange.isDirty());
//                assertEquals(ChangeModel.ChangeKind.NONE, nameMapChange.getKind());
//
//                assertEquals(0, nameMapChange.getAddedElements().size());
//                assertEquals(0, nameMapChange.getRemovedElements().size());
//                assertEquals(0, nameMapChange.getMutatedElements().size());
//                assertEquals(0, nameMapChange.getElementChanges().size());
//
//                assertEquals(0, nameMapChange.getAddedKeys().size());
//                assertEquals(0, nameMapChange.getRemovedKeys().size());
//                assertEquals(0, nameMapChange.getMutatedKeys().size());
//                assertEquals(0, nameMapChange.getKeyChanges().size());
//
//                assertEquals(0, nameMapChange.getAddedObjects().size());
//                assertEquals(0, nameMapChange.getRemovedObjects().size());
//                assertEquals(0, nameMapChange.getMutatedObjects().size());
//                assertEquals(0, nameMapChange.getObjectChanges().size());
//
//                assertEquals(0, changeModel.getDirtyChanges().size());
//            } else {
//                assertTrue(changeModel.isDirty());
//                assertEquals(ChangeModel.ChangeKind.MUTATED, changeModel.getKind());
//
//                assertTrue(nameMapChange.isDirty());
//                assertEquals(ChangeModel.ChangeKind.MUTATED, nameMapChange.getKind());
//
//                assertEquals(0, nameMapChange.getAddedElements().size());
//                assertEquals(0, nameMapChange.getRemovedElements().size());
//                assertEquals(2, nameMapChange.getMutatedElements().size());
//                assertEquals(ChangeModel.ChangeKind.MUTATED, nameMapChange.getMutatedElements().get(0).getKind());
//                assertEquals(ChangeModel.ChangeKind.MUTATED, nameMapChange.getMutatedElements().get(1).getKind());
//                assertEquals(2, nameMapChange.getElementChanges().size());
//
//                assertUnorderedEquals(Arrays.asList(
//                        nameMapChange.getMutatedElements().get(0),
//                        nameMapChange.getMutatedElements().get(1)
//                ), nameMapChange.getElementChanges());
//
//                assertUnorderedEquals(Arrays.asList(
//                        new NameObject("doc1", "doc1"),
//                        new NameObject("newPrimaryName", "newSecondaryName")
//                ), Arrays.asList(
//                        nameMapChange.getMutatedElements().get(0).getCurrentState(),
//                        nameMapChange.getMutatedElements().get(1).getCurrentState()
//                ));
//
//                assertEquals(0, nameMapChange.getAddedKeys().size());
//                assertEquals(0, nameMapChange.getRemovedKeys().size());
//                assertEquals(0, nameMapChange.getMutatedKeys().size());
//                assertEquals(0, nameMapChange.getKeyChanges().size());
//
//                assertEquals(0, nameMapChange.getAddedObjects().size());
//                assertEquals(0, nameMapChange.getRemovedObjects().size());
//                assertEquals(2, nameMapChange.getMutatedObjects().size());
//                assertEquals(2, nameMapChange.getObjectChanges().size());
//
//                assertUnorderedEquals(Arrays.asList(
//                        nameMapChange.getMutatedObjects().get(0),
//                        nameMapChange.getMutatedObjects().get(1)
//                ), nameMapChange.getObjectChanges());
//
//                assertUnorderedEquals(Arrays.asList(nameMapChange), changeModel.getDirtyChanges());
//            }
//        }
    }

    private void assertMutableChangeModel(UpdatableEmbeddableEntityWithMapsView docView) {
//        if (!isFullMode()) {
//            SingularChangeModel<UpdatableEmbeddableEntityWithMapsView> changeModel = evm.getChangeModel(docView);
//            MapChangeModel<?, ?> nameMapChange = (MapChangeModel<Object, Object>) changeModel.<Map<Object, Object>>get("embeddable.elementCollection");
//
//            if (registerType) {
//                assertFalse(changeModel.isDirty());
//                assertEquals(ChangeModel.ChangeKind.NONE, changeModel.getKind());
//
//                assertFalse(nameMapChange.isDirty());
//                assertEquals(ChangeModel.ChangeKind.NONE, nameMapChange.getKind());
//
//                assertEquals(0, nameMapChange.getAddedElements().size());
//                assertEquals(0, nameMapChange.getRemovedElements().size());
//                assertEquals(0, nameMapChange.getMutatedElements().size());
//                assertEquals(0, nameMapChange.getElementChanges().size());
//
//                assertEquals(0, nameMapChange.getAddedKeys().size());
//                assertEquals(0, nameMapChange.getRemovedKeys().size());
//                assertEquals(0, nameMapChange.getMutatedKeys().size());
//                assertEquals(0, nameMapChange.getKeyChanges().size());
//
//                assertEquals(0, nameMapChange.getAddedObjects().size());
//                assertEquals(0, nameMapChange.getRemovedObjects().size());
//                assertEquals(0, nameMapChange.getMutatedObjects().size());
//                assertEquals(0, nameMapChange.getObjectChanges().size());
//
//                assertEquals(0, changeModel.getDirtyChanges().size());
//            } else {
//                assertTrue(changeModel.isDirty());
//                assertEquals(ChangeModel.ChangeKind.MUTATED, changeModel.getKind());
//
//                assertTrue(nameMapChange.isDirty());
//                assertEquals(ChangeModel.ChangeKind.MUTATED, nameMapChange.getKind());
//
//                assertEquals(0, nameMapChange.getAddedElements().size());
//                assertEquals(0, nameMapChange.getRemovedElements().size());
//                assertEquals(1, nameMapChange.getMutatedElements().size());
//                assertEquals(ChangeModel.ChangeKind.MUTATED, nameMapChange.getMutatedElements().get(0).getKind());
//                assertEquals(new NameObject("doc1", "doc1"), nameMapChange.getMutatedElements().get(0).getCurrentState());
//
//                assertEquals(0, nameMapChange.getAddedKeys().size());
//                assertEquals(0, nameMapChange.getRemovedKeys().size());
//                assertEquals(0, nameMapChange.getMutatedKeys().size());
//
//                assertEquals(0, nameMapChange.getAddedObjects().size());
//                assertEquals(0, nameMapChange.getRemovedObjects().size());
//                assertEquals(1, nameMapChange.getMutatedObjects().size());
//                assertTrue(nameMapChange.getMutatedObjects().contains(nameMapChange.getMutatedElements().get(0)));
//
//                assertEquals(Arrays.asList(nameMapChange), changeModel.getDirtyChanges());
//            }
//        }
    }

    private void assertNameObjectSubviewEquals(Map<String, NameObject> entities, Map<String, SimpleNameObjectView> views) {
        assertEquals(entities.size(), views.size());
        Map<String, SimpleNameObjectView> unmatched = new HashMap<>(views);
        for (Map.Entry<String, NameObject> entry : entities.entrySet()) {
            NameObject value = entry.getValue();
            SimpleNameObjectView view = unmatched.remove(entry.getKey());
            if (view == null || !Objects.equals(value.getPrimaryName(), view.getPrimaryName()) || !Objects.equals(value.getSecondaryName(), view.getSecondaryName())
                    || (value.getIntIdEntity() == null) != (view.getIntIdEntity() == null) && !Objects.equals(value.getIntIdEntity().getId(), view.getIntIdEntity().getId())) {
                Assert.fail("Unmatched name object: " + value);
            }
        }

        if (!unmatched.isEmpty()) {
            Assert.fail("Unmatched views: " + unmatched);
        }
    }

    private void assertSubviewEquals(Map<String, IntIdEntity> entities, Map<String, SimpleIntIdEntityView> views) {
        assertEquals(entities.size(), views.size());
        Map<String, SimpleIntIdEntityView> unmatched = new HashMap<>(views);
        for (Map.Entry<String, IntIdEntity> entry : entities.entrySet()) {
            IntIdEntity value = entry.getValue();
            SimpleIntIdEntityView view = unmatched.remove(entry.getKey());
            if (view == null || !value.getId().equals(view.getId())) {
                Assert.fail("Unmatched name object: " + value);
            }
        }

        if (!unmatched.isEmpty()) {
            Assert.fail("Unmatched views: " + unmatched);
        }
    }

    private AssertStatementBuilder assertReplaceAnd(AssertStatementBuilder builder) {
        return builder.assertDelete()
                    .forRelation(EmbeddableTestEntity.class, "embeddable.elementCollection")
                .and()
                .assertInsert()
                    .forRelation(EmbeddableTestEntity.class, "embeddable.elementCollection")
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
                .fetching(EmbeddableTestEntity.class)
                .fetching(EmbeddableTestEntity.class, "embeddable.elementCollection")
                .fetching(EmbeddableTestEntity.class, "embeddable.manyToMany")
                .fetching(IntIdEntity.class)
                .and();
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder.update(EmbeddableTestEntity.class);
    }
}
