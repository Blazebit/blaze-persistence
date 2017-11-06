/*
 * Copyright 2014 - 2017 Blazebit.
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

package com.blazebit.persistence.view.testsuite.update.flatview.nested.mutable;

import com.blazebit.persistence.testsuite.base.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.category.NoHibernate43;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.NameObjectContainer;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateDocumentTest;
import com.blazebit.persistence.view.testsuite.update.flatview.nested.mutable.model.UpdatableDocumentWithMapsView;
import com.blazebit.persistence.view.testsuite.update.flatview.nested.mutable.model.UpdatableNameObjectContainerView;
import com.blazebit.persistence.view.testsuite.update.flatview.nested.mutable.model.UpdatableNameObjectView;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Map;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
// NOTE: Hibernate 4 does not support a list of embeddables containing embeddables
@Category({NoHibernate42.class, NoHibernate43.class, NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateNestedMutableFlatViewMapsTest extends AbstractEntityViewUpdateDocumentTest<UpdatableDocumentWithMapsView> {

    public EntityViewUpdateNestedMutableFlatViewMapsTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentWithMapsView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        cfg.addEntityView(UpdatableNameObjectView.class);
        cfg.addEntityView(UpdatableNameObjectContainerView.class);
    }

    @Test
    public void testUpdateCollectionElement() {
        // Given
        final UpdatableDocumentWithMapsView docView = getDoc1View();
        clearQueries();
        
        // When

        docView.getNameContainerMap().get("doc1").getNameObject().setPrimaryName("newPers");
        update(docView);

        // Then
        // Assert that the document and the people are loaded i.e. a full fetch
        // Finally the person is updated because the primary name changed
        AssertStatementBuilder builder = assertQuerySequence();

        fullFetch(builder);
        if (version) {
            builder.update(Document.class);
        }

        if (supportsMapInplaceUpdate()) {
            builder.assertUpdate()
                    .forRelation(Document.class, "nameContainerMap")
                    .and();
        } else {
            builder.assertDelete()
                    .forRelation(Document.class, "nameContainerMap")
                    .and()
                    .assertInsert()
                    .forRelation(Document.class, "nameContainerMap")
                    .and();

        }

        assertNoUpdateAndReload(docView);
        assertEquals("newPers", doc1.getNameContainerMap().get("doc1").getNameObject().getPrimaryName());
        assertSubviewEquals(doc1.getNameContainerMap(), docView.getNameContainerMap());
    }

    public static void assertSubviewEquals(Map<String, NameObjectContainer> persons, Map<String, ? extends UpdatableNameObjectContainerView> personSubviews) {
        if (persons == null) {
            assertNull(personSubviews);
            return;
        }

        assertNotNull(personSubviews);
        assertEquals(persons.size(), personSubviews.size());
        for (Map.Entry<String, NameObjectContainer> entry : persons.entrySet()) {
            NameObjectContainer p = entry.getValue();
            boolean found = false;
            UpdatableNameObjectContainerView pSub = personSubviews.get(entry.getKey());
            if (pSub != null) {
                if (p.getNameObject().getPrimaryName().equals(pSub.getNameObject().getPrimaryName())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                Assert.fail("Could not find a person subview instance with the name: " + p.getNameObject().getPrimaryName());
            }
        }
    }

    @Override
    protected AssertStatementBuilder fullFetch(AssertStatementBuilder builder) {
        return builder.assertSelect()
                .fetching(Document.class)
                .fetching(Document.class, "nameContainerMap")
                .and();
    }

    @Override
    protected AssertStatementBuilder fullUpdate(AssertStatementBuilder builder) {
        fullFetch(builder);
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
