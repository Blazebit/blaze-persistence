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

package com.blazebit.persistence.view.testsuite.update.correlated.mutableonly;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateDocumentTest;
import com.blazebit.persistence.view.testsuite.update.correlated.mutableonly.model.UpdatableDocumentWithCollectionsView;
import com.blazebit.persistence.view.testsuite.update.correlated.mutableonly.model.UpdatablePersonView;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateCorrelatedMutableOnlySubviewCollectionsTest extends AbstractEntityViewUpdateDocumentTest<UpdatableDocumentWithCollectionsView> {

    public EntityViewUpdateCorrelatedMutableOnlySubviewCollectionsTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentWithCollectionsView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        cfg.addEntityView(UpdatablePersonView.class);
    }

    @Test
    public void testUpdateReplaceCollection() {
        // Given
        final UpdatableDocumentWithCollectionsView docView = getDoc1View();
        clearQueries();
        
        // When
        try {
            docView.setPartners(new ArrayList<>(docView.getPartners()));
            fail("Expected the setter of a mutable only field to fail!");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("Updating the mutable-only attribute 'partners'"));
        }
    }

    private void validateMutableOnlyNoChange(UpdatableDocumentWithCollectionsView docView) {
        AssertStatementBuilder builder = assertQuerySequence();
        if (isFullMode()) {
            fullFetch(builder);
        }

        builder.validate();

        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);
        if (isFullMode()) {
            fullFetch(afterBuilder);
        }
        afterBuilder.validate();
    }

    @Test
    public void testUpdateAddToCollection() {
        // Given
        final UpdatableDocumentWithCollectionsView docView = getDoc1View();
        UpdatablePersonView newPerson = getP2View(UpdatablePersonView.class);
        clearQueries();

        // When & Then
        try {
            docView.getPartners().add(newPerson);
            fail("Expected mutating collection operations to fail!");
        } catch (UnsupportedOperationException ex) {
            assertTrue(ex.getMessage().contains("Collection is not updatable"));
        }
    }

    @Test
    public void testModifyCollectionElement() {
        // Given
        final UpdatableDocumentWithCollectionsView docView = getDoc1View();
        clearQueries();
        restartTransaction();

        // When
        docView.getPartners().get(0).setName("newPerson");
        update(docView);

        // Then
        AssertStatementBuilder builder = assertQuerySequence();

        fullFetch(builder);

        if (isQueryStrategy()) {
            if (isFullMode()) {
                builder.update(Person.class);
            }

            builder.update(Person.class);

            if (version) {
                builder.update(Document.class);
            }
        } else {
            builder.select(Person.class);
            if (version) {
                builder.update(Document.class);
            }

            builder.update(Person.class);
        }

        assertNoUpdateAndReload(docView);
        assertSubviewEquals(doc1.getPeople(), docView.getPartners());
        assertEquals("newPerson", p1.getName());
    }

    public void assertSubviewEquals(Collection<Person> persons, Collection<UpdatablePersonView> personSubviews) {
        if (persons == null) {
            assertNull(personSubviews);
            return;
        }

        assertNotNull(personSubviews);
        assertEquals(persons.size(), personSubviews.size());
        for (Person p : persons) {
            boolean found = false;
            for (UpdatablePersonView pSub : personSubviews) {
                if (p.getName().equals(pSub.getName())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                Assert.fail("Could not find a person subview instance with the name: " + p.getName());
            }
        }
    }

    @Override
    protected AssertStatementBuilder fullFetch(AssertStatementBuilder builder) {
        return builder.select(Document.class)
                .select(Person.class);
    }

    @Override
    protected AssertStatementBuilder fullUpdate(AssertStatementBuilder builder) {
        if (!isQueryStrategy()) {
            fullFetch(builder);
        }
        return builder.update(Document.class)
                .update(Person.class);
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder.update(Document.class);
    }
}
