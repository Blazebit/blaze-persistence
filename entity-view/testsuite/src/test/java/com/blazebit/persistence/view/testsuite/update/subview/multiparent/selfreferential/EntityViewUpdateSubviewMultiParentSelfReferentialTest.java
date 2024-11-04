/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.subview.multiparent.selfreferential;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.ConfigurationProperties;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateTest;
import com.blazebit.persistence.view.testsuite.update.subview.multiparent.selfreferential.model.PersonView;
import com.blazebit.persistence.view.testsuite.update.subview.multiparent.selfreferential.model.UpdatablePersonView;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateSubviewMultiParentSelfReferentialTest extends AbstractEntityViewUpdateTest<UpdatablePersonView> {

    public EntityViewUpdateSubviewMultiParentSelfReferentialTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatablePersonView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        cfg.setProperty(ConfigurationProperties.UPDATER_DISALLOW_OWNED_UPDATABLE_SUBVIEW, "true");
        cfg.addEntityView(PersonView.class);
    }

    @Override
    protected void reload() {

    }

    @Test
    public void testCreateNewSelfReferential() {
        // Given
        UpdatablePersonView p = evm.create(UpdatablePersonView.class);
        p.setName("p1");
        p.setFriend(p);

        // When
        update(p);

        // Then
        Person person = em.find(Person.class, p.getId());
        assertEquals(person, person.getFriend());
    }

    @Test
    public void testUpdateSelfReferential() {
        // Given
        UpdatablePersonView p = evm.create(UpdatablePersonView.class);
        p.setName("p1");
        update(p);

        // When
        p.setFriend(p);
        update(p);

        // Then
        Person person = em.find(Person.class, p.getId());
        assertEquals(person, person.getFriend());
    }

    @Override
    protected AssertStatementBuilder fullFetch(AssertStatementBuilder builder) {
        return null;
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return null;
    }
}
