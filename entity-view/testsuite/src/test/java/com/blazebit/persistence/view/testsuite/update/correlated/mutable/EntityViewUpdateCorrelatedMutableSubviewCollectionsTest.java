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

package com.blazebit.persistence.view.testsuite.update.correlated.mutable;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateDocumentTest;
import com.blazebit.persistence.view.testsuite.update.correlated.mutable.model.DocumentIdView;
import com.blazebit.persistence.view.testsuite.update.correlated.mutable.model.UpdatableDocumentWithCollectionsView;
import com.blazebit.persistence.view.testsuite.update.correlated.mutable.model.UpdatablePersonView;
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
public class EntityViewUpdateCorrelatedMutableSubviewCollectionsTest extends AbstractEntityViewUpdateDocumentTest<UpdatableDocumentWithCollectionsView> {

    public EntityViewUpdateCorrelatedMutableSubviewCollectionsTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentWithCollectionsView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        cfg.addEntityView(UpdatablePersonView.class);
        cfg.addEntityView(DocumentIdView.class);
    }

    @Test
    public void testUpdateReplaceCollection() {
        // Given
        final UpdatableDocumentWithCollectionsView docView = getDoc1View();
        clearQueries();
        
        // When
        docView.setPartners(new ArrayList<>(docView.getPartners()));
        update(docView);

        // Then
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isFullMode()) {
            if (isQueryStrategy()) {
                builder.update(Person.class);
            } else {
                builder.select(Document.class).select(Person.class);
            }
            if (version || isQueryStrategy()) {
                builder.update(Document.class);
            }
        }

        builder.validate();

        AssertStatementBuilder afterBuilder = assertQueriesAfterUpdate(docView);
        if (isQueryStrategy()) {
            if (isFullMode()) {
                afterBuilder.update(Person.class);
                afterBuilder.update(Document.class);
            }
        } else {
            if (isFullMode()) {
                afterBuilder.select(Document.class).select(Person.class);
                if (version) {
                    afterBuilder.update(Document.class);
                }
            }
        }
        afterBuilder.validate();
        assertSubviewEquals(doc1.getPartners(), docView.getPartners());
    }

    @Test
    public void testUpdateAddToCollection() {
        // Given
        final UpdatableDocumentWithCollectionsView docView = getDoc1View();
        UpdatablePersonView newPerson = getP2View(UpdatablePersonView.class);
        clearQueries();
        
        // When
        docView.addPerson(newPerson);
        update(docView);

        // Then
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isFullMode()) {
            if (isQueryStrategy()) {
                builder.update(Person.class);
            } else {
                fullFetch(builder);
            }

            if (version || isQueryStrategy()) {
                builder.update(Document.class);
            }
        } else {
            if (version) {
                builder.update(Document.class);
            }

            if (!isQueryStrategy()) {
                builder.select(Person.class);

                if (version) {
                    builder.select(Document.class);
                }
            }
        }

        builder.update(Person.class);
        builder.validate();

        assertNoUpdateAndReload(docView);
        assertSubviewEquals(doc1.getPartners(), docView.getPartners());
    }

    @Test
    public void testUpdateAddToNewCollection() {
        // Given
        final UpdatableDocumentWithCollectionsView docView = getDoc1View();
        UpdatablePersonView newPerson = getP2View(UpdatablePersonView.class);
        clearQueries();

        // When
        docView.setPartners(new ArrayList<>(docView.getPartners()));
        docView.addPerson(newPerson);
        update(docView);

        // Then
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isFullMode()) {
            if (isQueryStrategy()) {
                builder.update(Person.class);
            } else {
                fullFetch(builder);
            }

            if (version || isQueryStrategy()) {
                builder.update(Document.class);
            }
        } else {
            if (version) {
                builder.update(Document.class);
            }

            if (!isQueryStrategy()) {
                builder.select(Person.class);

                if (version) {
                    builder.select(Document.class);
                }
            }
        }

        builder.update(Person.class);
        builder.validate();

        assertNoUpdateAndReload(docView);
        assertSubviewEquals(doc1.getPartners(), docView.getPartners());
    }

    @Test
    public void testUpdateAddToCollectionAndModify() {
        // Given
        final UpdatableDocumentWithCollectionsView docView = getDoc1View();
        UpdatablePersonView newPerson = getP2View(UpdatablePersonView.class);
        clearQueries();

        // When
        newPerson.setName("newPerson");
        docView.addPerson(newPerson);
        update(docView);

        // Then
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isFullMode()) {
            if (isQueryStrategy()) {
                builder.update(Person.class);
            } else {
                fullFetch(builder);
            }

            if (version || isQueryStrategy()) {
                builder.update(Document.class);
            }
        } else {
            if (version) {
                builder.update(Document.class);
            }

            if (!isQueryStrategy()) {
                builder.select(Person.class);

                if (version) {
                    builder.select(Document.class);
                }
            }
        }

        builder.update(Person.class);
        builder.validate();

        assertNoUpdateAndReload(docView);
        assertSubviewEquals(doc1.getPartners(), docView.getPartners());
        assertEquals("newPerson", p2.getName());
    }

    @Test
    public void testUpdateAddToNewCollectionAndModify() {
        // Given
        final UpdatableDocumentWithCollectionsView docView = getDoc1View();
        UpdatablePersonView newPerson = getP2View(UpdatablePersonView.class);
        clearQueries();

        // When
        newPerson.setName("newPerson");
        docView.setPartners(new ArrayList<>(docView.getPartners()));
        docView.addPerson(newPerson);
        update(docView);

        // Then
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isFullMode()) {
            if (isQueryStrategy()) {
                builder.update(Person.class);
            } else {
                fullFetch(builder);
            }

            if (version || isQueryStrategy()) {
                builder.update(Document.class);
            }
        } else {
            if (version) {
                builder.update(Document.class);
            }

            if (!isQueryStrategy()) {
                builder.select(Person.class);

                if (version) {
                    builder.select(Document.class);
                }
            }
        }

        builder.update(Person.class);
        builder.validate();

        assertNoUpdateAndReload(docView);
        assertSubviewEquals(doc1.getPartners(), docView.getPartners());
        assertEquals("newPerson", p2.getName());
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
        return builder.select(Document.class).select(Person.class).select(Person.class);
    }

    @Override
    protected AssertStatementBuilder fullUpdate(AssertStatementBuilder builder) {
        builder.update(Person.class)
                .update(Person.class);
        if (version || isQueryStrategy()) {
            versionUpdate(builder);
        }
        return builder;
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder.update(Document.class);
    }
}
