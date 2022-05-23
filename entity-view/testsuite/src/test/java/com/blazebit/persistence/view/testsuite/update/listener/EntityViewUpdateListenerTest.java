/*
 * Copyright 2014 - 2022 Blazebit.
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

package com.blazebit.persistence.view.testsuite.update.listener;

import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatementBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.PostUpdateListener;
import com.blazebit.persistence.view.PreUpdateListener;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateDocumentTest;
import com.blazebit.persistence.view.testsuite.update.listener.model.UpdatableDocumentView;
import com.blazebit.persistence.view.testsuite.update.listener.model.UpdatablePersonView;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.persistence.EntityManager;

import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateListenerTest extends AbstractEntityViewUpdateDocumentTest<UpdatableDocumentView> {

    private static Date POST_UPDATE_DATE = new Date(0);

    public EntityViewUpdateListenerTest(FlushMode mode, FlushStrategy strategy, boolean version) {
        super(mode, strategy, version, UpdatableDocumentView.class);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        cfg.addEntityView(UpdatablePersonView.class);
        cfg.addEntityViewListener(NameUpdatingListener.class);
        cfg.addEntityViewListener(LastModifiedUpdatingListener.class);
    }
    
    public static class NameUpdatingListener implements PreUpdateListener<UpdatableDocumentView> {
        @Override
        public void preUpdate(EntityViewManager entityViewManager, EntityManager entityManager, UpdatableDocumentView view) {
            view.setName(view.getName() + "Updated");
        }
    }

    public static class LastModifiedUpdatingListener implements PostUpdateListener<UpdatableDocumentView> {
        @Override
        public void postUpdate(EntityViewManager entityViewManager, EntityManager entityManager, UpdatableDocumentView view) {
            view.setLastModified(POST_UPDATE_DATE);
        }
    }

    @Test
    public void testSimpleUpdate() {
        // Given
        final UpdatableDocumentView docView = getDoc1View();
        
        // When
        docView.setName("newDoc");
        update(docView);

        // Then
        clearPersistenceContextAndReload();
        assertEquals("newDocUpdated", docView.getName());
        assertEquals(POST_UPDATE_DATE, docView.getLastModified());
        assertEquals(doc1.getName(), docView.getName());
    }

    @Test
    public void testUpdateWithSubview() {
        // Given
        final UpdatableDocumentView docView = getDoc1View();
        UpdatablePersonView newOwner = getP2View(UpdatablePersonView.class);

        // When
        docView.setOwner(newOwner);
        update(docView);

        // Then
        clearPersistenceContextAndReload();
        assertEquals("doc1Updated", docView.getName());
        assertEquals(POST_UPDATE_DATE, docView.getLastModified());
        assertEquals(doc1.getName(), docView.getName());
        assertEquals(p2.getId(), doc1.getOwner().getId());
    }

    @Test
    public void testUpdateWithModifySubview() {
        // Given
        final UpdatableDocumentView docView = getDoc1View();
        UpdatablePersonView newOwner = getP2View(UpdatablePersonView.class);

        // When
        newOwner.setName("newPerson");
        docView.setOwner(newOwner);
        saveWith(docView, flusherBuilder -> {
            flusherBuilder.onPreUpdate(UpdatablePersonView.class, view -> {
                view.setName(view.getName() + "Updated");
            });
        });

        // Then
        clearPersistenceContextAndReload();
        assertEquals("doc1Updated", docView.getName());
        assertEquals(POST_UPDATE_DATE, docView.getLastModified());
        assertEquals(p2.getId(), doc1.getOwner().getId());
        assertEquals("newPersonUpdated", doc1.getOwner().getName());
    }

    @Test
    public void testUpdateWithModifyExisting() {
        // Given
        final UpdatableDocumentView docView = getDoc1View();

        // When
        docView.getOwner().setName("newPerson");
        update(docView);

        // Then
        clearPersistenceContextAndReload();
        assertEquals("doc1Updated", docView.getName());
        assertEquals(POST_UPDATE_DATE, docView.getLastModified());
        assertEquals(doc1.getName(), docView.getName());
        assertEquals("newPerson", doc1.getOwner().getName());
    }

    @Test
    public void testUpdateWithCreatedSubview() {
        // Given
        final UpdatableDocumentView docView = getDoc1View();
        UpdatablePersonView newOwner = evm.create(UpdatablePersonView.class);

        // When
        newOwner.setName("newPerson");
        docView.setOwner(newOwner);
        saveWith(docView, flusherBuilder -> {
            flusherBuilder.onPrePersist(UpdatablePersonView.class, Person.class,(view, entity) -> {
                entity.setAge(10L);
            });
        });

        // Then
        clearPersistenceContextAndReload();
        assertEquals("doc1Updated", docView.getName());
        assertEquals(POST_UPDATE_DATE, docView.getLastModified());
        assertEquals(doc1.getName(), docView.getName());
        assertEquals(newOwner.getId(), doc1.getOwner().getId());
        assertEquals("newPerson", doc1.getOwner().getName());
        assertEquals(10L, doc1.getOwner().getAge());
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
