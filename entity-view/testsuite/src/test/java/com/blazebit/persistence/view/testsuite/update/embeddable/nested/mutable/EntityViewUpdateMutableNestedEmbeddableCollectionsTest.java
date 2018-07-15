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
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntity;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.change.ChangeModel;
import com.blazebit.persistence.view.change.PluralChangeModel;
import com.blazebit.persistence.view.change.SingularChangeModel;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.AbstractEntityViewUpdateNestedEmbeddableCollectionsTest;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.model.SimpleEmbeddableEntityView;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.mutable.model.UpdatableEmbeddableEntityWithCollectionsView;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
public class EntityViewUpdateMutableNestedEmbeddableCollectionsTest extends AbstractEntityViewUpdateNestedEmbeddableCollectionsTest<UpdatableEmbeddableEntityWithCollectionsView> {

    public EntityViewUpdateMutableNestedEmbeddableCollectionsTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableEmbeddableEntityWithCollectionsView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Test
    public void testUpdateReplaceCollection() {
        // Given
        final UpdatableEmbeddableEntityWithCollectionsView docView = updateReplaceCollection();
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
        assertSubviewEquals(entity1.getEmbeddable().getOneToMany2(), docView.getEmbeddable().getOneToMany2());
    }

    @Test
    public void testUpdateAddToCollection() {
        // Given
        final UpdatableEmbeddableEntityWithCollectionsView docView = updateAddToCollection();
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
                    .forRelation(EmbeddableTestEntity.class, "embeddable.oneToMany2")
                .and()
                .validate();
        assertVersionDiff(oldVersion, docView.getVersion(), 1, 1);

        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);

        if (isFullMode()) {
            fullFetch(afterBuilder);
            // Since we detect nothing changed, there is no need to flush for version increments
            if (!isQueryStrategy()) {
                // Note that we delete the collection twice, because entity1 and entity2 are in the persistence context
                // See com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityEmbeddable.elementCollection2
                // Apparently, Hibernate has problems with handling this correctly
                afterBuilder.assertDelete()
                        .forRelation(EmbeddableTestEntity.class, "embeddable.elementCollection2")
                        .and();
                afterBuilder.assertDelete()
                        .forRelation(EmbeddableTestEntity.class, "embeddable.elementCollection2")
                        .and();
            }
        }

        assertVersionDiff(oldVersion, docView.getVersion(), 1, 1);

        afterBuilder.validate();
        assertSubviewEquals(entity1.getEmbeddable().getOneToMany2(), docView.getEmbeddable().getOneToMany2());
    }

    @Test
    public void testUpdateAddToNewCollection() {
        // Given
        final UpdatableEmbeddableEntityWithCollectionsView docView = updateAddToNewCollection();
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
                .forRelation(EmbeddableTestEntity.class, "embeddable.oneToMany2")
            .validate();
        assertVersionDiff(oldVersion, docView.getVersion(), 1, 1);

        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);

        if (isFullMode()) {
            fullFetch(afterBuilder);
            // Since we detect nothing changed, there is no need to flush for version increments
            if (!isQueryStrategy()) {
                // Note that we delete the collection twice, because entity1 and entity2 are in the persistence context
                // See com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityEmbeddable.elementCollection2
                // Apparently, Hibernate has problems with handling this correctly
                afterBuilder.assertDelete()
                        .forRelation(EmbeddableTestEntity.class, "embeddable.elementCollection2")
                        .and();
                afterBuilder.assertDelete()
                        .forRelation(EmbeddableTestEntity.class, "embeddable.elementCollection2")
                        .and();
            }
        }

        assertVersionDiff(oldVersion, docView.getVersion(), 1, 1);

        afterBuilder.validate();
        assertSubviewEquals(entity1.getEmbeddable().getOneToMany2(), docView.getEmbeddable().getOneToMany2());
    }

    protected void assertChangesUpdateAndFlush(UpdatableEmbeddableEntityWithCollectionsView docView) {
        if (!isFullMode()) {
            SingularChangeModel<UpdatableEmbeddableEntityWithCollectionsView> changeModel = evm.getChangeModel(docView);
            PluralChangeModel<?, ?> oneToManyChange = (PluralChangeModel<?, ?>) changeModel.get("embeddable.oneToMany2");

            assertTrue(changeModel.isDirty());
            assertEquals(ChangeModel.ChangeKind.MUTATED, changeModel.getKind());

            assertTrue(oneToManyChange.isDirty());
            assertEquals(ChangeModel.ChangeKind.MUTATED, oneToManyChange.getKind());

            assertEquals(1, oneToManyChange.getAddedElements().size());
            assertEquals(0, oneToManyChange.getRemovedElements().size());
            assertEquals(0, oneToManyChange.getMutatedElements().size());

            assertUnorderedEquals(Arrays.asList(oneToManyChange.getAddedElements().get(0)), oneToManyChange.getElementChanges());

            assertEquals(ChangeModel.ChangeKind.UPDATED, oneToManyChange.getAddedElements().get(oneToManyChange.getAddedElements().size() - 1).getKind());
            assertNull(oneToManyChange.getAddedElements().get(oneToManyChange.getAddedElements().size() - 1).getInitialState());
            assertEquals(evm.getReference(SimpleEmbeddableEntityView.class, entity2.getId()), oneToManyChange.getAddedElements().get(oneToManyChange.getAddedElements().size() - 1).getCurrentState());
            assertEquals(Arrays.asList(oneToManyChange), ((SingularChangeModel<?>) changeModel.get("embeddable")).getDirtyChanges());
        }
        update(docView);
        if (!isFullMode()) {
            SingularChangeModel<UpdatableEmbeddableEntityWithCollectionsView> changeModel = evm.getChangeModel(docView);
            PluralChangeModel<?, ?> oneToMany2Change = (PluralChangeModel<?, ?>) changeModel.get("embeddable.oneToMany2");

            assertFalse(changeModel.isDirty());
            assertEquals(ChangeModel.ChangeKind.NONE, changeModel.getKind());

            assertFalse(oneToMany2Change.isDirty());
            assertEquals(ChangeModel.ChangeKind.NONE, oneToMany2Change.getKind());

            assertEquals(0, oneToMany2Change.getAddedElements().size());
            assertEquals(0, oneToMany2Change.getRemovedElements().size());
            assertEquals(0, oneToMany2Change.getMutatedElements().size());
            assertEquals(0, oneToMany2Change.getElementChanges().size());

            assertEquals(0, changeModel.getDirtyChanges().size());
        }
    }

    private void assertMutableChangeModel(UpdatableEmbeddableEntityWithCollectionsView docView) {
        if (!isFullMode()) {
            SingularChangeModel<UpdatableEmbeddableEntityWithCollectionsView> changeModel = evm.getChangeModel(docView);
            PluralChangeModel<?, ?> oneToMany2Change = (PluralChangeModel<?, ?>) changeModel.get("embeddable.oneToMany2");

            assertFalse(changeModel.isDirty());
            assertEquals(ChangeModel.ChangeKind.NONE, changeModel.getKind());

            assertFalse(oneToMany2Change.isDirty());
            assertEquals(ChangeModel.ChangeKind.NONE, oneToMany2Change.getKind());

            assertEquals(0, oneToMany2Change.getAddedElements().size());
            assertEquals(0, oneToMany2Change.getRemovedElements().size());
            assertEquals(0, oneToMany2Change.getMutatedElements().size());
            assertEquals(0, oneToMany2Change.getElementChanges().size());

            assertEquals(0, changeModel.getDirtyChanges().size());
        }
    }

    private AssertStatementBuilder assertReplaceAnd(AssertStatementBuilder builder) {
        return builder.assertDelete()
                    .forRelation(EmbeddableTestEntity.class, "embeddable.oneToMany2")
                .and()
                .assertInsert()
                    .forRelation(EmbeddableTestEntity.class, "embeddable.oneToMany2")
                .and();
    }

    private void assertSubviewEquals(Set<EmbeddableTestEntity> entities, Set<SimpleEmbeddableEntityView> views) {
        assertEquals(entities.size(), views.size());
        Set<SimpleEmbeddableEntityView> unmatched = new HashSet<>(views);
        OUTER: for (EmbeddableTestEntity entity : entities) {
            Iterator<SimpleEmbeddableEntityView> iterator = unmatched.iterator();
            while (iterator.hasNext()) {
                SimpleEmbeddableEntityView view = iterator.next();
                if (entity.getId().getKey().equals(view.getId().getKey()) && entity.getId().getValue().equals(view.getId().getValue())) {
                    iterator.remove();
                    continue OUTER;
                }
            }

            Assert.fail("Unmatched entity: " + entity);
        }

        if (!unmatched.isEmpty()) {
            Assert.fail("Unmatched views: " + unmatched);
        }
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
                .fetching(EmbeddableTestEntity.class, "embeddable.oneToMany2")
                .fetching(EmbeddableTestEntity.class)
                .and();
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder.update(EmbeddableTestEntity.class);
    }
}
