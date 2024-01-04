/*
 * Copyright 2014 - 2024 Blazebit.
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

package com.blazebit.persistence.view.testsuite.update.subview.inverse.simple;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateDocumentTest;
import com.blazebit.persistence.view.testsuite.update.subview.inverse.simple.model.DocumentIdView;
import com.blazebit.persistence.view.testsuite.update.subview.inverse.simple.model.PersonIdView;
import com.blazebit.persistence.view.testsuite.update.subview.inverse.simple.model.UpdatableDocumentView;
import com.blazebit.persistence.view.testsuite.update.subview.inverse.simple.model.UpdatablePersonView;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.HashSet;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateSubviewInverseSimpleTest extends AbstractEntityViewUpdateDocumentTest<UpdatablePersonView> {

    public EntityViewUpdateSubviewInverseSimpleTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatablePersonView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        cfg.addEntityView(PersonIdView.class);
        cfg.addEntityView(DocumentIdView.class);
        cfg.addEntityView(UpdatablePersonView.class);
        cfg.addEntityView(UpdatableDocumentView.class);
    }

    @Test
    public void testAddNewElementToCollection() {
        // Given
        UpdatablePersonView newPerson = evm.create(UpdatablePersonView.class);
        newPerson.setName("newPers1");
        update(newPerson);

        // When
        UpdatableDocumentView document = evm.create(UpdatableDocumentView.class);
        document.setName("newDoc123");
        document.setOwner(getP1View(PersonIdView.class));
        newPerson.getOwnedDocuments2().add(document);
        update(newPerson);

        newPerson.getOwnedDocuments2().add(document);
        Assert.assertFalse(evm.getChangeModel(newPerson).get("ownedDocuments2").isDirty());

        // Then
        em.clear();
        Document newDocument = em.find(Document.class, document.getId());
        Assert.assertEquals(newPerson.getId(), newDocument.getResponsiblePerson().getId());
    }

    @Test
    public void testReplaceCollection() {
        // Given
        UpdatablePersonView newPerson = evm.create(UpdatablePersonView.class);
        newPerson.setName("newPers1");
        UpdatableDocumentView document = evm.create(UpdatableDocumentView.class);
        document.setName("newDoc123");
        document.setOwner(getP1View(PersonIdView.class));
        newPerson.getOwnedDocuments2().add(document);
        update(newPerson);

        // When
        newPerson.setOwnedDocuments2(new HashSet<>(newPerson.getOwnedDocuments2()));
        update(newPerson);

        // Then
        em.clear();
        Document newDocument = em.find(Document.class, document.getId());
        Assert.assertEquals(newPerson.getId(), newDocument.getResponsiblePerson().getId());
    }

    @Test
    public void testReplaceElementWithCopyInCollection() {
        // Given
        UpdatablePersonView newPerson = evm.create(UpdatablePersonView.class);
        newPerson.setName("newPers1");
        UpdatableDocumentView document = evm.create(UpdatableDocumentView.class);
        document.setName("newDoc123");
        document.setOwner(getP1View(PersonIdView.class));
        newPerson.getOwnedDocuments2().add(document);
        update(newPerson);

        // When
        UpdatableDocumentView copy = evm.convert(document, UpdatableDocumentView.class);
        newPerson.getOwnedDocuments2().remove(document);
        newPerson.getOwnedDocuments2().add(copy);
        update(newPerson);

        // Then
        em.clear();
        Document newDocument = em.find(Document.class, document.getId());
        Assert.assertEquals(newPerson.getId(), newDocument.getResponsiblePerson().getId());
    }

    @Test
    public void testUpdateRemoveNonExisting() {
        // Given
        UpdatablePersonView newPerson = evm.create(UpdatablePersonView.class);
        newPerson.setName("newPers1");
        UpdatableDocumentView document = evm.create(UpdatableDocumentView.class);
        document.setName("newDoc123");
        document.setOwner(getP1View(PersonIdView.class));
        newPerson.getOwnedDocuments2().add(document);
        update(newPerson);

        // When
        newPerson.getOwnedDocuments2().remove("non-existing");
        update(newPerson);

        // Then
        em.clear();
        Person pers = em.find(Person.class, newPerson.getId());
        Assert.assertEquals(1, pers.getOwnedDocuments2().size());
    }

    @Test
    public void testUpdateRemoveNull() {
        // Given
        UpdatablePersonView newPerson = evm.create(UpdatablePersonView.class);
        newPerson.setName("newPers1");
        UpdatableDocumentView document = evm.create(UpdatableDocumentView.class);
        document.setName("newDoc123");
        document.setOwner(getP1View(PersonIdView.class));
        newPerson.getOwnedDocuments2().add(document);
        update(newPerson);

        // When
        newPerson.getOwnedDocuments2().remove(null);
        update(newPerson);

        // Then
        em.clear();
        Person pers = em.find(Person.class, newPerson.getId());
        Assert.assertEquals(1, pers.getOwnedDocuments2().size());
    }

    @Override
    protected void reload() {
    }

    @Override
    protected AssertStatementBuilder fullFetch(AssertStatementBuilder builder) {
        return builder;
    }

    @Override
    protected AssertStatementBuilder fullUpdate(AssertStatementBuilder builder) {
        return builder;
    }

    @Override
    protected AssertStatementBuilder versionUpdate(AssertStatementBuilder builder) {
        return builder.update(Document.class);
    }
}
