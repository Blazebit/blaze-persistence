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

package com.blazebit.persistence.view.testsuite.update.subview.inverse.unmapped;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.change.PluralChangeModel;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateTest;
import com.blazebit.persistence.view.testsuite.update.subview.inverse.unmapped.model.DocumentIdView;
import com.blazebit.persistence.view.testsuite.update.subview.inverse.unmapped.model.PersonIdView;
import com.blazebit.persistence.view.testsuite.update.subview.inverse.unmapped.model.UpdatableDocumentView;
import com.blazebit.persistence.view.testsuite.update.subview.inverse.unmapped.model.UpdatablePersonView;
import com.blazebit.persistence.view.testsuite.update.subview.inverse.unmapped.model.UpdatableVersionView;
import com.blazebit.persistence.view.testsuite.update.subview.inverse.unmapped.model.VersionIdView;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateSubviewInverseUmappedTest extends AbstractEntityViewUpdateTest<UpdatableDocumentView> {

    public EntityViewUpdateSubviewInverseUmappedTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        cfg.addEntityView(DocumentIdView.class);
        cfg.addEntityView(UpdatableDocumentView.class);
        cfg.addEntityView(PersonIdView.class);
        cfg.addEntityView(UpdatablePersonView.class);
        cfg.addEntityView(VersionIdView.class);
        cfg.addEntityView(UpdatableVersionView.class);
    }

    @Test
    public void testAddNewElementToCollection() {
        // Given
        UpdatableDocumentView newDocument = evm.create(UpdatableDocumentView.class);
        UpdatablePersonView owner = evm.create(UpdatablePersonView.class);
        owner.setName("test");
        newDocument.setName("doc");
        newDocument.setOwner(owner);
        update(newDocument);

        // When
        UpdatableVersionView version = evm.create(UpdatableVersionView.class);
        newDocument.getVersions().add(version);
        update(newDocument);

        // Then
        restartTransaction();
        Document doc = em.find(Document.class, newDocument.getId());
        Assert.assertEquals(1, doc.getVersions().size());
        Assert.assertEquals(version.getId(), doc.getVersions().iterator().next().getId());
    }

    @Test
    public void testRemoveReadOnlyElementFromCollection() {
        // Given
        UpdatableDocumentView newDocument = evm.create(UpdatableDocumentView.class);
        UpdatablePersonView owner = evm.create(UpdatablePersonView.class);
        owner.setName("test");
        newDocument.setName("doc");
        newDocument.setOwner(owner);
        UpdatableVersionView version = evm.create(UpdatableVersionView.class);
        newDocument.getVersions().add(version);
        update(newDocument);

        // When
        restartTransaction();
        newDocument = evm.applySetting(EntityViewSetting.create(UpdatableDocumentView.class), cbf.create(em, Document.class)).getSingleResult();
        newDocument.getVersions().remove(newDocument.getVersions().iterator().next());
        PluralChangeModel<Object, Object> positionsChangeModel = (PluralChangeModel<Object, Object>) evm.getChangeModel(newDocument).get("versions");
        Assert.assertEquals(1, positionsChangeModel.getRemovedElements().size());
        update(newDocument);

        // Then
        restartTransaction();
        Document doc = em.find(Document.class, newDocument.getId());
        Assert.assertEquals(0, doc.getVersions().size());
    }

    @Test
    public void testPersistAndAddNewElementToCollection() {
        // When
        UpdatableDocumentView newDocument = evm.create(UpdatableDocumentView.class);
        UpdatablePersonView owner = evm.create(UpdatablePersonView.class);
        owner.setName("test");
        newDocument.setName("doc");
        newDocument.setOwner(owner);
        UpdatableVersionView version = evm.create(UpdatableVersionView.class);
        newDocument.getVersions().add(version);
        update(newDocument);

        // Then
        restartTransaction();
        Document doc = em.find(Document.class, newDocument.getId());
        Assert.assertEquals(1, doc.getVersions().size());
        Assert.assertEquals(version.getId(), doc.getVersions().iterator().next().getId());
    }

    @Test
    public void testAddExistingViewToInverseCollection() {
        // Given
        UpdatableDocumentView newDocument = evm.create(UpdatableDocumentView.class);
        UpdatablePersonView owner = evm.create(UpdatablePersonView.class);
        owner.setName("test");
        newDocument.setName("doc");
        newDocument.setOwner(owner);
        update(newDocument);

        // When
        restartTransaction();
        newDocument = evm.applySetting(EntityViewSetting.create(UpdatableDocumentView.class), cbf.create(em, Document.class)).getSingleResult();
        UpdatablePersonView newPerson = evm.create(UpdatablePersonView.class);
        newPerson.setName("newPerson");
        update(newPerson);
        newDocument.getPartners().add(newPerson);
        update(newDocument);

        // Then
        restartTransaction();
        Document doc = em.find(Document.class, newDocument.getId());
        Assert.assertEquals(1, doc.getPartners().size());
    }

    @Test
    public void testAddExistingViewWithMapChangesToInverseCollection() {
        // Given
        UpdatableDocumentView newDocument = evm.create(UpdatableDocumentView.class);
        UpdatablePersonView owner = evm.create(UpdatablePersonView.class);
        owner.setName("test");
        newDocument.setName("doc");
        newDocument.setOwner(owner);
        update(newDocument);

        // When
        restartTransaction();
        newDocument = evm.applySetting(EntityViewSetting.create(UpdatableDocumentView.class), cbf.create(em, Document.class)).getSingleResult();
        UpdatablePersonView newPerson = evm.create(UpdatablePersonView.class);
        newPerson.setName("newPerson");
        update(newPerson);
        newPerson.getLocalized().put(1, "key1");
        newDocument.getPartners().add(newPerson);
        update(newDocument);

        // Then
        restartTransaction();
        Document doc = em.find(Document.class, newDocument.getId());
        Assert.assertEquals(1, doc.getPartners().iterator().next().getLocalized().size());
    }

    @Override
    protected void restartTransactionAndReload() {
        restartTransaction();
    }

    @Override
    protected AssertStatementBuilder fullFetch(AssertStatementBuilder builder) {
        return builder.assertSelect()
                .fetching(Document.class)
                .fetching(Person.class)
                .fetching(Person.class)
                .fetching(Document.class)
                .fetching(Document.class, "people")
                .fetching(Person.class)
                .fetching(Person.class)
                .fetching(Person.class)
                .and();
    }

    @Override
    protected AssertStatementBuilder fullUpdate(AssertStatementBuilder builder) {
        fullFetch(builder)
                .update(Person.class)
                .update(Person.class)
                .update(Person.class)
                .update(Person.class)
                .update(Document.class)
                .update(Person.class)
                .update(Person.class)
                .update(Person.class);
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
