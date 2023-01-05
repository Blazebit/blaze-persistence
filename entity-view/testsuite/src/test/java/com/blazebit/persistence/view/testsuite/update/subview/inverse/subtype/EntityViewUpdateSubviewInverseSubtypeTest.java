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

package com.blazebit.persistence.view.testsuite.update.subview.inverse.subtype;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateDocumentTest;
import com.blazebit.persistence.view.testsuite.update.subview.inverse.subtype.model.CreatableDocumentView;
import com.blazebit.persistence.view.testsuite.update.subview.inverse.subtype.model.DocumentIdView;
import com.blazebit.persistence.view.testsuite.update.subview.inverse.subtype.model.PersonIdView;
import com.blazebit.persistence.view.testsuite.update.subview.inverse.subtype.model.DocumentView;
import com.blazebit.persistence.view.testsuite.update.subview.inverse.subtype.model.UpdatablePersonView;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.HashSet;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateSubviewInverseSubtypeTest extends AbstractEntityViewUpdateDocumentTest<UpdatablePersonView> {

    public EntityViewUpdateSubviewInverseSubtypeTest(FlushMode mode, FlushStrategy strategy, boolean version) {
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
        cfg.addEntityView(DocumentView.class);
        cfg.addEntityView(CreatableDocumentView.class);
    }

    @Test
    public void testAddNewElementToCollection() {
        // Given
        UpdatablePersonView newPerson = evm.create(UpdatablePersonView.class);
        newPerson.setName("newPers1");
        update(newPerson);

        // When
        CreatableDocumentView document = evm.create(CreatableDocumentView.class);
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
        CreatableDocumentView document = evm.create(CreatableDocumentView.class);
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
