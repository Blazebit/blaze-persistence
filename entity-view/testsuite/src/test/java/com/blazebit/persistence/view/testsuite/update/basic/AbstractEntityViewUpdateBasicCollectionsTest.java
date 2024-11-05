/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.basic;

import java.util.ArrayList;

import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateDocumentTest;
import com.blazebit.persistence.view.testsuite.update.basic.model.UpdatableDocumentBasicWithCollectionsViewBase;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
@Category({ NoEclipselink.class})
public abstract class AbstractEntityViewUpdateBasicCollectionsTest<T extends UpdatableDocumentBasicWithCollectionsViewBase> extends AbstractEntityViewUpdateDocumentTest<T> {

    public AbstractEntityViewUpdateBasicCollectionsTest(FlushMode mode, FlushStrategy strategy, boolean version, Class<T> viewType) {
        super(mode, strategy, version, viewType);
    }

    @Override
    protected String[] getFetchedCollections() {
        return new String[] { "strings" };
    }

    public T updateReplaceCollection() {
        // Given
        final T docView = getDoc1View();
        clearQueries();
        
        // When
        docView.setStrings(new ArrayList<>(docView.getStrings()));
        return docView;
    }

    public T updateAddToCollection() {
        // Given
        final T docView = getDoc1View();
        clearQueries();

        // When
        docView.getStrings().add("newString");
        return docView;
    }

    public T updateAddToNewCollection() {
        // Given
        final T docView = getDoc1View();
        clearQueries();

        // When
        docView.setStrings(new ArrayList<>(docView.getStrings()));
        docView.getStrings().add("newString");
        return docView;
    }

    public T updateRemoveNonExisting() {
        // Given
        final T docView = getDoc1View();
        clearQueries();

        // When
        docView.getStrings().remove("non-existing");
        return docView;
    }

    public T updateRemoveNull() {
        // Given
        final T docView = getDoc1View();
        clearQueries();

        // When
        docView.getStrings().remove(null);
        return docView;
    }

}
