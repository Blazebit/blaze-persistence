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

package com.blazebit.persistence.view.testsuite.update.embeddable.nested.graph;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate50;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate51;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.AbstractEntityViewUpdateNestedEmbeddableEntityTest;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.graph.model.UpdatableEmbeddableEntityWithCollectionsView;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.graph.model.UpdatableEmbeddableEntityWithMultipleCollectionsEmbeddableViewBase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
// NOTE: Only the latest Hibernate 5.2 properly implements support for selecting element collections
// NOTE: No Datanucleus support yet
@Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class, NoDatanucleus.class, NoEclipselink.class})
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
