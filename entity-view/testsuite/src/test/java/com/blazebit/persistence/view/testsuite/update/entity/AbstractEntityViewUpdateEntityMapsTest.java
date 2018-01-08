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

package com.blazebit.persistence.view.testsuite.update.entity;

import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateDocumentTest;
import com.blazebit.persistence.view.testsuite.update.entity.model.UpdatableDocumentEntityWithMapsViewBase;
import org.junit.runners.Parameterized;

import java.util.HashMap;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractEntityViewUpdateEntityMapsTest<T extends UpdatableDocumentEntityWithMapsViewBase> extends AbstractEntityViewUpdateDocumentTest<T> {

    public AbstractEntityViewUpdateEntityMapsTest(FlushMode mode, FlushStrategy strategy, boolean version, Class<T> viewType) {
        super(mode, strategy, version, viewType);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    public T replaceCollection() {
        // Given
        final T docView = getDoc1View();
        clearQueries();
        
        // When
        docView.setContacts(new HashMap<>(docView.getContacts()));
        update(docView);
        return docView;
    }

    public T addToCollection() {
        // Given
        final T docView = getDoc1View();
        Person p = p2;
        clearQueries();
        
        // When
        docView.getContacts().put(2, p);
        update(docView);
        return docView;
    }

    public T addToNewCollection() {
        // Given
        final T docView = getDoc1View();
        Person p = p2;
        clearQueries();

        // When
        docView.setContacts(new HashMap<>(docView.getContacts()));
        docView.getContacts().put(2, p);
        update(docView);
        return docView;
    }

    public T addToCollectionAndModifyEntity() {
        // Given
        final T docView = getDoc1View();
        Person p = p2;
        clearQueries();

        // When
        p.setName("newPerson");
        docView.getContacts().put(2, p);
        update(docView);
        return docView;
    }

    public T addToNewCollectionAndModifyEntity() {
        // Given
        final T docView = getDoc1View();
        Person p = p2;
        clearQueries();

        // When
        p.setName("newPerson");
        docView.setContacts(new HashMap<>(docView.getContacts()));
        docView.getContacts().put(2, p);
        update(docView);
        return docView;
    }

    public T modifyEntityInCollection() {
        // Given
        final T docView = getDoc1View();
        clearQueries();

        // When
        docView.getContacts().get(1).setName("newPerson");
        update(docView);
        return docView;
    }

    public T addNullToCollection() {
        // Given
        final T docView = getDoc1View();
        clearQueries();

        // When
        docView.getContacts().put(2, null);
        update(docView);
        return docView;
    }

    public T setCollectionToNull() {
        // Given
        final T docView = getDoc1View();
        clearQueries();

        // When
        docView.setContacts(null);
        update(docView);
        return docView;
    }

    public T addNewEntityToCollection() {
        // Given
        final T docView = getDoc1View();
        clearQueries();

        // When
        docView.getContacts().put(2, new Person("newPerson"));
        update(docView);
        return docView;
    }

}
