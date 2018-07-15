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
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntity;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.change.ChangeModel;
import com.blazebit.persistence.view.change.SingularChangeModel;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.AbstractEntityViewUpdateNestedEmbeddableTest;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.model.SimpleEmbeddableEntityView;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.model.UpdatableEmbeddableEntityEmbeddableViewBase;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.mutable.model.UpdatableEmbeddableEntityView;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateMutableNestedEmbeddableTest extends AbstractEntityViewUpdateNestedEmbeddableTest<UpdatableEmbeddableEntityView> {

    public EntityViewUpdateMutableNestedEmbeddableTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableEmbeddableEntityView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Test
    public void testSimpleUpdate() {
        // Given & When
        // We don't actually update the embeddable in a partial mode, when knowing the type and having a query strategy
        final UpdatableEmbeddableEntityView entView = simpleUpdate();
        Long oldVersion = entView.getVersion();
        if (!isFullMode()) {
            SingularChangeModel<UpdatableEmbeddableEntityView> changeModel = evm.getChangeModel(entView);
            assertTrue(changeModel.isDirty());
            assertEquals(ChangeModel.ChangeKind.MUTATED, changeModel.getKind());

            ChangeModel<UpdatableEmbeddableEntityEmbeddableViewBase> embeddableChange = changeModel.get("embeddable");
            assertTrue(embeddableChange.isDirty());
            assertEquals(ChangeModel.ChangeKind.UPDATED, embeddableChange.getKind());

            assertUnorderedEquals(Arrays.asList(embeddableChange), changeModel.getDirtyChanges());

            assertNotNull(embeddableChange.getInitialState().getManyToOne());
            assertNull(embeddableChange.getCurrentState());
        }
        update(entView);
        assertEmptyChangeModel(entView);

        // Then
        // Assert that not only the document is loaded and finally also updated
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (!isQueryStrategy()) {
            fullFetch(builder);
        }

        builder.update(EmbeddableTestEntity.class)
                .assertDelete()
                    .forRelation(EmbeddableTestEntity.class, "embeddable.elementCollection")
                .and()
                .assertDelete()
                    .forRelation(EmbeddableTestEntity.class, "embeddable.elementCollection2")
                .and()
                .assertDelete()
                    .forRelation(EmbeddableTestEntity.class, "embeddable.manyToMany")
                .and()
                .assertDelete()
                    .forRelation(EmbeddableTestEntity.class, "embeddable.oneToMany2")
                .and()
                .assertDelete()
                    .forRelation(EmbeddableTestEntity.class, "embeddable.nestedEmbeddable.nestedOneToMany")
                .and()
                .validate();

        // When we register a type that can check for the dirtyness, we can skip the reload
        assertVersionDiff(oldVersion, entView.getVersion(), 1, 1);
        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(entView);

        if (isFullMode()) {
            if (isQueryStrategy()) {
                fullUpdate(afterBuilder);
            } else {
                fullFetch(afterBuilder);
                // We always need to update because the embeddable was set to null but hibernate always instantiates the embeddable
                afterBuilder.update(EmbeddableTestEntity.class);
            }

            // Since the embeddable was set to null and hibernate initializes the element anyway, we set null on the entity during full flushing
            // This triggers full deletes of associations contained in the embeddable when using the entity flush mode
            afterBuilder.assertDelete()
                        .forRelation(EmbeddableTestEntity.class, "embeddable.elementCollection")
                    .and()
                    .assertDelete()
                        .forRelation(EmbeddableTestEntity.class, "embeddable.elementCollection2")
                    .and()
                    .assertDelete()
                        .forRelation(EmbeddableTestEntity.class, "embeddable.manyToMany")
                    .and()
                    .assertDelete()
                        .forRelation(EmbeddableTestEntity.class, "embeddable.oneToMany2")
                    .and()
                    .assertDelete()
                        .forRelation(EmbeddableTestEntity.class, "embeddable.nestedEmbeddable.nestedOneToMany")
                    .and();
        }
        afterBuilder.validate();
        assertVersionDiff(oldVersion, entView.getVersion(), 1, 2);

        assertNull(entity1.getEmbeddable().getManyToOne());
    }

    @Test
    public void testUpdateMutable() {
        // Given & When
        final UpdatableEmbeddableEntityView entView = updateMutable();
        Long oldVersion = entView.getVersion();
        if (!isFullMode()) {
            SingularChangeModel<UpdatableEmbeddableEntityView> changeModel = evm.getChangeModel(entView);
            assertTrue(changeModel.isDirty());
            assertEquals(ChangeModel.ChangeKind.MUTATED, changeModel.getKind());

            ChangeModel<UpdatableEmbeddableEntityEmbeddableViewBase> embeddableChange = changeModel.get("embeddable");
            assertTrue(embeddableChange.isDirty());
            assertEquals(ChangeModel.ChangeKind.MUTATED, embeddableChange.getKind());

            ChangeModel<SimpleEmbeddableEntityView> manyToOneChange = changeModel.get("embeddable.manyToOne");
            assertTrue(manyToOneChange.isDirty());
            assertEquals(ChangeModel.ChangeKind.UPDATED, manyToOneChange.getKind());

            assertEquals("e1", manyToOneChange.getInitialState().getId().getKey());
            assertEquals("e2", embeddableChange.getCurrentState().getManyToOne().getId().getKey());
            assertEquals(Arrays.asList(embeddableChange), changeModel.getDirtyChanges());
        }
        updateAndAssertChangesFlushed(entView);

        // Then
        assertVersionDiff(oldVersion, entView.getVersion(), 1, 1);
        fullFetchUpdateAndReload(entView);
        if (!isQueryStrategy() && isFullMode() && version) {
            // Entity flushing will detect nothing changed
            assertEquals(oldVersion + 1L, (long) entView.getVersion());
        } else {
            assertVersionDiff(oldVersion, entView.getVersion(), 1, 2);
        }
        assertEquals(entity2.getId(), entity1.getEmbeddable().getManyToOne().getId());
    }

    protected void assertEmptyChangeModel(UpdatableEmbeddableEntityView docView) {
        if (!isFullMode()) {
            SingularChangeModel<UpdatableEmbeddableEntityView> changeModel = evm.getChangeModel(docView);
            ChangeModel<UpdatableEmbeddableEntityEmbeddableViewBase> embeddableChange = changeModel.get("embeddable");
            assertFalse(embeddableChange.isDirty());
            assertEquals(ChangeModel.ChangeKind.NONE, embeddableChange.getKind());

            ChangeModel<SimpleEmbeddableEntityView> manyToOneChange = changeModel.get("embeddable.manyToOne");
            assertFalse(manyToOneChange.isDirty());
            assertEquals(ChangeModel.ChangeKind.NONE, manyToOneChange.getKind());

            assertTrue(changeModel.getDirtyChanges().isEmpty());
        }
    }

    private void fullFetchUpdateAndReload(UpdatableEmbeddableEntityView docView) {
        // Assert that not only the document is loaded and finally also updated
        AssertStatementBuilder builder = assertQuerySequence();

        if (!isQueryStrategy()) {
            fullFetch(builder);
        }

        builder.update(EmbeddableTestEntity.class);

        if (!isQueryStrategy()) {
            // See com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityEmbeddable.elementCollection2
            // Apparently, Hibernate has problems with handling this correctly
            builder.assertDelete()
                        .forRelation(EmbeddableTestEntity.class, "embeddable.elementCollection2")
                    .and();
        }

        builder.validate();

//        assertNoUpdateAndReload(docView);
        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);

        if (isFullMode()) {
            if (isQueryStrategy()) {
                fullUpdate(afterBuilder);
            } else {
                fullFetch(afterBuilder);
//                if (version) {
//                    versionUpdate(afterBuilder);
//                }

                // See com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityEmbeddable.elementCollection2
                // Apparently, Hibernate has problems with handling this correctly
                afterBuilder.assertDelete()
                        .forRelation(EmbeddableTestEntity.class, "embeddable.elementCollection2")
                        .and();
            }
        }
        afterBuilder.validate();
        restartTransactionAndReload();
    }

    protected void assertNoUpdateFullFetchAndReload(UpdatableEmbeddableEntityView docView) {
        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);

        if (isFullMode()) {
            if (isQueryStrategy()) {
                fullUpdate(afterBuilder);
            } else {
                fullFetch(afterBuilder);
                if (version) {
                    afterBuilder.update(EmbeddableTestEntity.class);
                }
            }
        } else {
            if (version) {
                afterBuilder.select(EmbeddableTestEntity.class)
                        .update(EmbeddableTestEntity.class);
            }
        }
        afterBuilder.validate();
        restartTransactionAndReload();
    }

    @Override
    protected AssertStatementBuilder fullFetch(AssertStatementBuilder builder) {
        return builder.assertSelect()
                .fetching(EmbeddableTestEntity.class)
                .and();
    }

    @Override
    protected AssertStatementBuilder fullUpdate(AssertStatementBuilder builder) {
        return builder.assertUpdate()
                .forEntity(EmbeddableTestEntity.class)
                .and();
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder.update(EmbeddableTestEntity.class);
    }
}
