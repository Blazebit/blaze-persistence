/*
 * Copyright 2014 - 2023 Blazebit.
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

package com.blazebit.persistence.view.testsuite.update.singular;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.entity.SingularListEntity;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateTest;
import com.blazebit.persistence.view.testsuite.update.singular.model.SingularListEntityView;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.6.8
 */
@RunWith(Parameterized.class)
// NOTE: Hibernate 4.2 doesn't support attribute converters
// NOTE: No EclipseLink and Datanucleus support yet
@Category({ NoHibernate42.class, NoDatanucleus.class, NoEclipselink.class})
public class SingularListUpdateTest extends AbstractEntityViewUpdateTest<SingularListEntityView> {

    private SingularListEntity entity1;

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
            SingularListEntity.class
        };
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        cfg.addEntityView(SingularListEntityView.class);
    }

    public SingularListUpdateTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, SingularListEntityView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected void prepareData(EntityManager em) {
        entity1 = new SingularListEntity("doc1");
        entity1.getList().add("abc");

        em.persist(entity1);
    }

    // Test for #1540
    @Test
    public void testUpdateList() {
        SingularListEntityView doc1View = evm.find(em, SingularListEntityView.class, entity1.getId());
        doc1View.getList().add("def");

        update(doc1View);

        List<SingularListEntity> entities = cbf.create(em, SingularListEntity.class, "d")
            .where("id").eq(entity1.getId())
            .getResultList();

        reload();
        // Doc1
        assertEquals(entity1.getName(), entities.get(0).getName());
        assertEquals(Arrays.asList("abc", "def"), entity1.getList());
    }

    // Test for #1540
    @Test
    public void testReplaceList() {
        SingularListEntityView doc1View = evm.find(em, SingularListEntityView.class, entity1.getId());
        doc1View.setList(new ArrayList<>(Arrays.asList("xyz")));

        update(doc1View);

        List<SingularListEntity> entities = cbf.create(em, SingularListEntity.class, "d")
            .where("id").eq(entity1.getId())
            .getResultList();

        reload();
        // Doc1
        assertEquals(entity1.getName(), entities.get(0).getName());
        assertEquals(Arrays.asList("xyz"), entity1.getList());
    }

    @Override
    protected void reload() {
        entity1 = em.find(SingularListEntity.class, entity1.getId());
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
