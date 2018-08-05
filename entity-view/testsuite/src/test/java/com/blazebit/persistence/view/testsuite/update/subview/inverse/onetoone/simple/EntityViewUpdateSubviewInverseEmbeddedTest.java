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

package com.blazebit.persistence.view.testsuite.update.subview.inverse.onetoone.simple;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.DocumentForSimpleOneToOne;
import com.blazebit.persistence.testsuite.entity.DocumentInfoSimple;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateTest;
import com.blazebit.persistence.view.testsuite.update.subview.inverse.onetoone.simple.model.DocumentForOneToOneIdView;
import com.blazebit.persistence.view.testsuite.update.subview.inverse.onetoone.simple.model.DocumentInfoIdView;
import com.blazebit.persistence.view.testsuite.update.subview.inverse.onetoone.simple.model.UpdatableDocumentInfoView;
import com.blazebit.persistence.view.testsuite.update.subview.inverse.onetoone.simple.model.UpdatableDocumentForOneToOneView;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.persistence.EntityManager;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateSubviewInverseEmbeddedTest extends AbstractEntityViewUpdateTest<UpdatableDocumentForOneToOneView> {

    private DocumentForSimpleOneToOne doc1;
    private DocumentForSimpleOneToOne doc2;

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class[]{
                DocumentForSimpleOneToOne.class,
                DocumentInfoSimple.class
        };
    }

    public EntityViewUpdateSubviewInverseEmbeddedTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentForOneToOneView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        cfg.addEntityView(DocumentForOneToOneIdView.class);
        cfg.addEntityView(DocumentInfoIdView.class);
        cfg.addEntityView(UpdatableDocumentForOneToOneView.class);
        cfg.addEntityView(UpdatableDocumentInfoView.class);
    }

    @Override
    protected void prepareData(EntityManager em) {
        doc1 = new DocumentForSimpleOneToOne("doc1");
        em.persist(doc1);
        em.persist(new DocumentInfoSimple(1L, doc1, "doc1"));
        doc2 = new DocumentForSimpleOneToOne("doc2");
        em.persist(doc2);
        em.persist(new DocumentInfoSimple(2L, doc2, "doc2"));
    }


    @Override
    protected void restartTransactionAndReload() {
        restartTransaction();
        doc1 = em.find(DocumentForSimpleOneToOne.class, doc1.getId());
        doc2 = em.find(DocumentForSimpleOneToOne.class, doc2.getId());
    }

    @Test
    public void testSetNewElementWhenNull() {
        // Given
        UpdatableDocumentForOneToOneView docView = evm.find(em, UpdatableDocumentForOneToOneView.class, doc1.getId());

        // When
        UpdatableDocumentInfoView info = evm.create(UpdatableDocumentInfoView.class);
        info.setId(10L);
        info.setSomeInfo("123");
        docView.setDocumentInfo(info);
        update(docView);

        // Then
        restartTransactionAndReload();
        Assert.assertEquals("123", doc1.getDocumentInfo().getSomeInfo());
    }

    @Test
    public void testSetNewElementReplaceExisting() {
        // Given
        UpdatableDocumentForOneToOneView docView = evm.find(em, UpdatableDocumentForOneToOneView.class, doc2.getId());

        // When
        UpdatableDocumentInfoView info = evm.create(UpdatableDocumentInfoView.class);
        info.setId(10L);
        info.setSomeInfo("123");
        docView.setDocumentInfo(info);
        update(docView);

        // Then
        restartTransactionAndReload();
        Assert.assertEquals("123", doc2.getDocumentInfo().getSomeInfo());
    }

    @Test
    public void testReplaceWithNull() {
        // Given
        UpdatableDocumentForOneToOneView docView = evm.find(em, UpdatableDocumentForOneToOneView.class, doc1.getId());

        // When
        docView.setDocumentInfo(null);
        update(docView);

        // Then
        restartTransactionAndReload();
        Assert.assertNull(doc1.getDocumentInfo());
    }

    @Test
    public void testSetExistingElementReplaceExisting() {
        // Given
        UpdatableDocumentForOneToOneView docView = evm.find(em, UpdatableDocumentForOneToOneView.class, doc1.getId());

        // When
        docView.setDocumentInfo(evm.getReference(DocumentInfoIdView.class, 2L));
        update(docView);

        // Then
        restartTransactionAndReload();
        Assert.assertEquals("doc2", doc1.getDocumentInfo().getSomeInfo());
    }

    @Test
    public void testSetAndModifyExistingElementReplaceExisting() {
        // Given
        UpdatableDocumentForOneToOneView docView = evm.find(em, UpdatableDocumentForOneToOneView.class, doc1.getId());

        // When
        UpdatableDocumentInfoView infoView = evm.find(em, UpdatableDocumentInfoView.class, 2L);
        infoView.setSomeInfo("newDoc2");
        docView.setDocumentInfo(infoView);
        update(docView);

        // Then
        restartTransactionAndReload();
        Assert.assertEquals("newDoc2", doc1.getDocumentInfo().getSomeInfo());
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
