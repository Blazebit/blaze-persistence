/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.embeddable.nested.graph;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.AbstractEntityViewUpdateNestedEmbeddableEntityTest;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.graph.model.UpdatableEmbeddableEntityWithCollectionsView;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.graph.model.UpdatableEmbeddableEntityWithMultipleCollectionsEmbeddableViewBase;
import jakarta.persistence.EntityManager;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
// NOTE: Only the latest Hibernate 5.2 properly implements support for selecting element collections
@Category({ NoEclipselink.class })
public class EntityViewUpdateNestedEmbeddableCollectionsGraphTest extends AbstractEntityViewUpdateNestedEmbeddableEntityTest<UpdatableEmbeddableEntityWithCollectionsView> {

    public EntityViewUpdateNestedEmbeddableCollectionsGraphTest() {
        super(FlushMode.PARTIAL, FlushStrategy.QUERY, true, UpdatableEmbeddableEntityWithCollectionsView.class, UpdatableEmbeddableEntityWithMultipleCollectionsEmbeddableViewBase.class);
    }

    @Override
    protected void prepareData(EntityManager em) {
        super.prepareData(em);
        // This data is required to reproduce #628
        entity2.getEmbeddable().setManyToOne(entity1);
        entity1.getEmbeddable().getOneToMany2().add(entity2);
        entity1.getEmbeddable().getNestedEmbeddable().getNestedOneToMany().add(entity2);
    }

    @Test
    public void testLoad() {
        UpdatableEmbeddableEntityWithCollectionsView ent1View = getEnt1View();
        Assert.assertEquals(2, ent1View.getEmbeddable().getOneToMany().size());
        Assert.assertEquals(2, ent1View.getEmbeddable().getOneToMany2().size());
        Assert.assertEquals(2, ent1View.getNestedOneToManys().size());
    }

    @Override
    protected AssertStatementBuilder fullFetch(AssertStatementBuilder builder) {
        return builder;
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder;
    }
}
