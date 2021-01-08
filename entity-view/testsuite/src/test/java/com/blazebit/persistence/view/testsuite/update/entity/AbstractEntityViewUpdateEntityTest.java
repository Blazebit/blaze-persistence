/*
 * Copyright 2014 - 2021 Blazebit.
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

package com.blazebit.persistence.view.testsuite.update.entity;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateDocumentTest;
import com.blazebit.persistence.view.testsuite.update.entity.model.UpdatableDocumentEntityViewBase;
import org.junit.runners.Parameterized;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractEntityViewUpdateEntityTest<T extends UpdatableDocumentEntityViewBase> extends AbstractEntityViewUpdateDocumentTest<T> {

    protected final AtomicInteger updateCommitCount = new AtomicInteger();

    public AbstractEntityViewUpdateEntityTest(FlushMode mode, FlushStrategy strategy, boolean version, Class<T> viewType) {
        super(mode, strategy, version, viewType);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        cfg.addEntityView(DocumentRollbackInfoView.class);
    }

    public T simpleUpdate() {
        // Given
        final T docView = getDoc1View();
        clearQueries();
        
        // When
        docView.setName("newDoc");
        update(docView);
        return docView;
    }

    public T updateWithEntity() {
        // Given
        final T docView = getDoc1View();
        Person newOwner = p2;
        clearQueries();

        // When
        docView.setResponsiblePerson(newOwner);
        update(docView);
        return docView;
    }

    public T updateWithModifyEntity() {
        // Given
        final T docView = getDoc1View();
        Person newOwner = p2;
        clearQueries();

        // When
        newOwner.setName("newOwner");
        docView.setResponsiblePerson(newOwner);
        update(docView);
        return docView;
    }

    public T updateWithModifyExisting() {
        // Given
        final T docView = getDoc1View();
        clearQueries();

        // When
        docView.getResponsiblePerson().setName("newOwner");
        update(docView);
        return docView;
    }

    public T updateToNull() {
        // Given
        final T docView = getDoc1View();
        clearQueries();

        // When
        docView.setResponsiblePerson(null);
        update(docView);
        return docView;
    }

    public T updateToNewPerson(boolean rollback) {
        // Given
        final T docView = getDoc1View();
        clearQueries();

        // When
        Person p = new Person("newPerson");
        docView.setResponsiblePerson(p);
        if (rollback) {
            saveWithRollbackWith(docView, flushOperationBuilder -> {
                flushOperationBuilder.onPostRollbackUpdate(DocumentRollbackInfoView.class, (entityViewManager, entityManager, view, transition) -> {
                    assertEquals(10L, view.getAge().longValue());
                    updateCommitCount.incrementAndGet();
                });
                flushOperationBuilder.flush();
                em.flush();
            });
        } else {
            update(docView);
        }
        return docView;
    }

    @EntityView(Document.class)
    public static interface DocumentRollbackInfoView {

        @IdMapping
        public Long getId();

        public Long getAge();
    }

}
