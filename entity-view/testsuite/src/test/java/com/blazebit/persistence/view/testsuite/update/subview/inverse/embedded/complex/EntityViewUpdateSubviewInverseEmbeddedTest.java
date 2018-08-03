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

package com.blazebit.persistence.view.testsuite.update.subview.inverse.embedded.complex;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.change.PluralChangeModel;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrder;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPosition;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPositionDefault;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPositionDefaultId;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPositionId;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateTest;
import com.blazebit.persistence.view.testsuite.update.subview.inverse.embedded.complex.model.LegacyOrderIdView;
import com.blazebit.persistence.view.testsuite.update.subview.inverse.embedded.complex.model.LegacyOrderPositionDefaultIdView;
import com.blazebit.persistence.view.testsuite.update.subview.inverse.embedded.complex.model.LegacyOrderPositionIdView;
import com.blazebit.persistence.view.testsuite.update.subview.inverse.embedded.complex.model.UpdatableLegacyOrderPositionDefaultView;
import com.blazebit.persistence.view.testsuite.update.subview.inverse.embedded.complex.model.UpdatableLegacyOrderPositionView;
import com.blazebit.persistence.view.testsuite.update.subview.inverse.embedded.complex.model.UpdatableLegacyOrderView;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Iterator;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateSubviewInverseEmbeddedTest extends AbstractEntityViewUpdateTest<UpdatableLegacyOrderView> {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
                LegacyOrder.class,
                LegacyOrderPosition.class,
                LegacyOrderPositionId.class,
                LegacyOrderPositionDefault.class,
                LegacyOrderPositionDefaultId.class
        };
    }

    public EntityViewUpdateSubviewInverseEmbeddedTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableLegacyOrderView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        cfg.addEntityView(LegacyOrderIdView.class);
        cfg.addEntityView(LegacyOrderPositionIdView.class);
        cfg.addEntityView(LegacyOrderPositionIdView.Id.class);
        cfg.addEntityView(LegacyOrderPositionDefaultIdView.class);
        cfg.addEntityView(LegacyOrderPositionDefaultIdView.Id.class);
        cfg.addEntityView(UpdatableLegacyOrderView.class);
        cfg.addEntityView(UpdatableLegacyOrderPositionView.class);
        cfg.addEntityView(UpdatableLegacyOrderPositionDefaultView.class);
    }

    @Test
    public void testAddNewElementToCollection() {
        // Given
        UpdatableLegacyOrderView newOrder = evm.create(UpdatableLegacyOrderView.class);
        update(newOrder);

        // When
        UpdatableLegacyOrderPositionView position = evm.create(UpdatableLegacyOrderPositionView.class);
        position.getId().setPositionId(0);
        position.setArticleNumber("123");
        newOrder.getPositions().add(position);
        update(newOrder);

        // Then
        restartTransaction();
        LegacyOrder legacyOrder = em.find(LegacyOrder.class, newOrder.getId());
        Assert.assertEquals(1, legacyOrder.getPositions().size());
        Assert.assertEquals(new LegacyOrderPositionId(newOrder.getId(), 0), legacyOrder.getPositions().iterator().next().getId());
    }

    @Test
    public void testAddNewElementToCollectionAndUpdate() {
        // Given
        UpdatableLegacyOrderView newOrder = evm.create(UpdatableLegacyOrderView.class);
        UpdatableLegacyOrderPositionView position = evm.create(UpdatableLegacyOrderPositionView.class);
        position.getId().setPositionId(0);
        position.setArticleNumber("123");
        newOrder.getPositions().add(position);
        UpdatableLegacyOrderPositionDefaultView positionDefault = evm.create(UpdatableLegacyOrderPositionDefaultView.class);
        positionDefault.getId().setSupplierId(1);
        position.getDefaults().add(positionDefault);
        update(newOrder);

        // When
        newOrder.getPositions().iterator().next().getDefaults().iterator().next().setValue("NEW");
        position = evm.create(UpdatableLegacyOrderPositionView.class);
        position.getId().setPositionId(1);
        position.setArticleNumber("123");
        newOrder.getPositions().add(position);
        positionDefault = evm.create(UpdatableLegacyOrderPositionDefaultView.class);
        positionDefault.getId().setSupplierId(2);
        position.getDefaults().add(positionDefault);
        update(newOrder);

        // Then
        restartTransaction();
        LegacyOrder legacyOrder = em.find(LegacyOrder.class, newOrder.getId());
        Assert.assertEquals(2, legacyOrder.getPositions().size());
        Iterator<LegacyOrderPosition> iterator = legacyOrder.getPositions().iterator();
        LegacyOrderPosition actualPosition = iterator.next();
        if (actualPosition.getId().getPositionId().equals(0)) {
            actualPosition = iterator.next();
        }
        Assert.assertEquals(new LegacyOrderPositionId(newOrder.getId(), 1), actualPosition.getId());
    }

    @Test
    public void testRemoveReadOnlyElementFromCollection() {
        // Given
        UpdatableLegacyOrderView newOrder = evm.create(UpdatableLegacyOrderView.class);
        UpdatableLegacyOrderPositionView position = evm.create(UpdatableLegacyOrderPositionView.class);
        position.getId().setPositionId(0);
        position.setArticleNumber("123");
        newOrder.getPositions().add(position);
        update(newOrder);

        // When
        restartTransaction();
        newOrder = evm.applySetting(EntityViewSetting.create(UpdatableLegacyOrderView.class), cbf.create(em, LegacyOrder.class)).getSingleResult();
        newOrder.getPositions().remove(newOrder.getPositions().iterator().next());
        PluralChangeModel<Object, Object> positionsChangeModel = (PluralChangeModel<Object, Object>) evm.getChangeModel(newOrder).get("positions");
        Assert.assertEquals(1, positionsChangeModel.getRemovedElements().size());
        update(newOrder);

        // Then
        restartTransaction();
        LegacyOrder legacyOrder = em.find(LegacyOrder.class, newOrder.getId());
        Assert.assertEquals(0, legacyOrder.getPositions().size());
    }

    @Override
    protected void restartTransactionAndReload() {
        restartTransaction();
    }

    @Override
    protected AssertStatementBuilder fullFetch(AssertStatementBuilder builder) {
        return builder.assertSelect()
                .fetching(Document.class)
                .fetching(Person.class)
                .fetching(Person.class)
                .fetching(Document.class)
                .fetching(Document.class, "people")
                .fetching(Person.class)
                .fetching(Person.class)
                .fetching(Person.class)
                .and();
    }

    @Override
    protected AssertStatementBuilder fullUpdate(AssertStatementBuilder builder) {
        fullFetch(builder)
                .update(Person.class)
                .update(Person.class)
                .update(Person.class)
                .update(Person.class)
                .update(Document.class)
                .update(Person.class)
                .update(Person.class)
                .update(Person.class);
        if (version) {
            builder.update(Document.class);
        }

        return builder;
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder.update(Document.class);
    }
}
