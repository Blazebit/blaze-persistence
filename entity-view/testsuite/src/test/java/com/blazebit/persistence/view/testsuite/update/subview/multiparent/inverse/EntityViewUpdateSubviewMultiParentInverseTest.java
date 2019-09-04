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

package com.blazebit.persistence.view.testsuite.update.subview.multiparent.inverse;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.ConfigurationProperties;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateDocumentTest;
import com.blazebit.persistence.view.testsuite.update.subview.multiparent.inverse.model.DocumentIdView;
import com.blazebit.persistence.view.testsuite.update.subview.multiparent.inverse.model.PersonView;
import com.blazebit.persistence.view.testsuite.update.subview.multiparent.inverse.model.UpdatableDocumentWithGraphView;
import com.blazebit.persistence.view.testsuite.update.subview.multiparent.inverse.model.UpdatablePersonView;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

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
public class EntityViewUpdateSubviewMultiParentInverseTest extends AbstractEntityViewUpdateDocumentTest<UpdatableDocumentWithGraphView> {

    public EntityViewUpdateSubviewMultiParentInverseTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentWithGraphView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        cfg.setProperty(ConfigurationProperties.UPDATER_DISALLOW_OWNED_UPDATABLE_SUBVIEW, "true");
        cfg.addEntityView(DocumentIdView.class);
        cfg.addEntityView(PersonView.class);
        cfg.addEntityView(UpdatablePersonView.class);
    }

    @Override
    protected String[] getFetchedCollections() {
        return new String[] { "partners" };
    }

    @Test
    public void testUpdateAddToCollectionAndSetNonCascading() {
        // Given
        final UpdatableDocumentWithGraphView docView = getDoc1View();
        UpdatablePersonView p2 = getP2View(UpdatablePersonView.class);
        clearQueries();

        // When
        docView.getPartners().add(p2);
        docView.setOwner(p2);
        update(docView);

        // Then
        verifyAdd(docView);
    }

    @Test
    public void testUpdateAddToCollectionAndSetMultipleNonCascading() {
        // Given
        final UpdatableDocumentWithGraphView docView = getDoc1View();
        UpdatablePersonView p2 = getP2View(UpdatablePersonView.class);
        clearQueries();

        // When
        docView.getPartners().add(p2);
        docView.setOwner(p2);
        docView.setResponsiblePerson(p2);
        update(docView);

        // Then
        verifyAdd(docView);
    }

    private void verifyAdd(UpdatableDocumentWithGraphView docView) {
        // Assert that the document and the people are loaded, but only a relation insert is done
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isQueryStrategy()) {
            if (isFullMode()) {
                builder.update(Person.class);
            }
        } else {
            if (isFullMode()) {
                fullFetch(builder);
            } else {
                builder.select(Document.class);
            }
            builder.select(Person.class);
        }

        builder.update(Document.class);

        builder.update(Person.class)
                .validate();

        assertNoUpdateAndReload(docView, true);
        assertSubviewEquals(doc1.getPartners(), docView.getPartners());
    }

    @Test
    public void testUpdateAddNewToCollectionAndSetNonCascading() {
        // Given
        final UpdatableDocumentWithGraphView docView = getDoc1View();
        UpdatablePersonView pNew = evm.create(UpdatablePersonView.class);
        pNew.setName("newPers");
        clearQueries();

        // When
        docView.getPartners().add(pNew);
        docView.setOwner(pNew);
        update(docView);

        // Then
        verifyAddNew(docView);
    }

    @Test
    public void testUpdateAddNewToCollectionAndSetMultipleNonCascading() {
        // Given
        final UpdatableDocumentWithGraphView docView = getDoc1View();
        UpdatablePersonView pNew = evm.create(UpdatablePersonView.class);
        pNew.setName("newPers");
        clearQueries();

        // When
        docView.getPartners().add(pNew);
        docView.setOwner(pNew);
        docView.setResponsiblePerson(pNew);
        update(docView);

        // Then
        verifyAddNew(docView);
    }

    private void verifyAddNew(UpdatableDocumentWithGraphView docView) {
        // Assert that the document and the people are loaded, but only a relation insert is done
        AssertStatementBuilder builder = assertUnorderedQuerySequence();

        if (isQueryStrategy()) {
            if (isFullMode()) {
                builder.update(Person.class);
                builder.update(Person.class);
            }
        } else {
            if (isFullMode()) {
                fullFetch(builder);
            } else {
                builder.select(Document.class);
            }
        }

        builder.update(Document.class);

        builder.insert(Person.class)
                .validate();

        assertNoUpdateAndReload(docView, true);
        assertSubviewEquals(doc1.getPartners(), docView.getPartners());
    }

    @Test
    public void testUnsetParentWithReadOnlyParent() {
        // Given
        final UpdatableDocumentWithGraphView docView = getDoc1View();
        UpdatablePersonView p2 = getP2View(UpdatablePersonView.class);
        docView.getPartners().add(p2);
        docView.setOwner(p2);
        try {
            docView.getPartners().remove(p2);
            Assert.fail("Expected failure!");
        } catch (IllegalStateException ex) {
            Assert.assertTrue(ex.getMessage(), ex.getMessage().contains("Can't unset writable parent"));
        }
    }

    public static void assertSubviewEquals(Collection<Person> persons, Collection<? extends PersonView> personSubviews) {
        if (persons == null) {
            assertNull(personSubviews);
            return;
        }

        assertNotNull(personSubviews);
        assertEquals(persons.size(), personSubviews.size());
        for (Person p : persons) {
            boolean found = false;
            for (PersonView pSub : personSubviews) {
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
    protected AssertStatementBuilder fullUpdate(AssertStatementBuilder builder) {
        builder.update(Person.class);
        builder.update(Person.class);
        return versionUpdate(builder);
    }

    @Override
    protected AssertStatementBuilder fullFetch(AssertStatementBuilder builder) {
        return builder.assertSelect()
                .fetching(Document.class)
                .fetching(Person.class)
                .and();
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder.update(Document.class);
    }
}
