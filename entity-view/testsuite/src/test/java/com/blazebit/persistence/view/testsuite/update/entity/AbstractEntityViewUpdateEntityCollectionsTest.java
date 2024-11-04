/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.entity;

import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateDocumentTest;
import com.blazebit.persistence.view.testsuite.update.entity.model.UpdatableDocumentEntityWithCollectionsViewBase;
import org.junit.runners.Parameterized;

import java.util.ArrayList;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractEntityViewUpdateEntityCollectionsTest<T extends UpdatableDocumentEntityWithCollectionsViewBase> extends AbstractEntityViewUpdateDocumentTest<T> {

    public AbstractEntityViewUpdateEntityCollectionsTest(FlushMode mode, FlushStrategy strategy, boolean version, Class<T> viewType) {
        super(mode, strategy, version, viewType);
    }

    @Parameterized.Parameters(name = "{0} - {1} - VERSIONED={2}")
    public static Object[][] combinations() {
        return MODE_STRATEGY_VERSION_COMBINATIONS;
    }

    @Override
    protected String[] getFetchedCollections() {
        return new String[] { "people" };
    }

    public T replaceCollection() {
        // Given
        final T docView = getDoc1View();
        clearQueries();
        
        // When
        docView.setPeople(new ArrayList<>(docView.getPeople()));
        update(docView);
        return docView;
    }

    public T addToCollection() {
        // Given
        final T docView = getDoc1View();
        Person p = p2;
        clearQueries();
        
        // When
        docView.getPeople().add(p);
        update(docView);
        return docView;
    }

    public T addToNewCollection() {
        // Given
        final T docView = getDoc1View();
        Person p = p2;
        clearQueries();

        // When
        docView.setPeople(new ArrayList<>(docView.getPeople()));
        docView.getPeople().add(p);
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
        docView.getPeople().add(p);
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
        docView.setPeople(new ArrayList<>(docView.getPeople()));
        docView.getPeople().add(p);
        update(docView);
        return docView;
    }

    public T modifyEntityInCollection() {
        // Given
        final T docView = getDoc1View();
        clearQueries();

        // When
        docView.getPeople().get(0).setName("newPerson");
        update(docView);
        return docView;
    }

    public T addNullToCollection() {
        // Given
        final T docView = getDoc1View();
        clearQueries();

        // When
        docView.getPeople().add(null);
        update(docView);
        return docView;
    }

    public T setCollectionToNull() {
        // Given
        final T docView = getDoc1View();
        clearQueries();

        // When
        docView.setPeople(null);
        update(docView);
        return docView;
    }

    public T addNewEntityToCollection() {
        // Given
        final T docView = getDoc1View();
        clearQueries();

        // When
        docView.getPeople().add(new Person("newPerson"));
        update(docView);
        return docView;
    }

}
