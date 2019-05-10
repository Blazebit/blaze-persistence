/*
 * Copyright 2014 - 2019 Blazebit.
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

package com.blazebit.persistence.view.testsuite.removeunmapped;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.testsuite.removeunmapped.model.FileLink;
import com.blazebit.persistence.view.testsuite.removeunmapped.model.Template;
import com.blazebit.persistence.view.testsuite.removeunmapped.model.TemplateView;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.persistence.EntityManager;


/**
 *
 * @author Harald Eibensteiner
 * @since 1.4.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewRemoveViewWithUnmappedCollectionAndWhereClauseTest extends AbstractEntityViewUpdateTest<TemplateView> {

    public EntityViewRemoveViewWithUnmappedCollectionAndWhereClauseTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, TemplateView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class[]{
                Template.class,
                FileLink.class
        };
    }

    @Test
    public void testSimpleRemove() {
        // Given
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Template t = new Template(1L);
                em.persist(t);
            }
        });
        // When
        remove(TemplateView.class, 1L);

        // Then
    }

    @Override
    protected void reload() {

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
