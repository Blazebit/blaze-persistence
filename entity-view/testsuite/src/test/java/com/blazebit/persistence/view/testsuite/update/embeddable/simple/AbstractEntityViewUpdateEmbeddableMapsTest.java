/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.embeddable.simple;

import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.NameObject;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateDocumentTest;
import com.blazebit.persistence.view.testsuite.update.embeddable.simple.model.UpdatableDocumentEmbeddableWithMapsViewBase;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.HashMap;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoEclipselink.class})
public abstract class AbstractEntityViewUpdateEmbeddableMapsTest<T extends UpdatableDocumentEmbeddableWithMapsViewBase> extends AbstractEntityViewUpdateDocumentTest<T> {

    public AbstractEntityViewUpdateEmbeddableMapsTest(FlushMode mode, FlushStrategy strategy, boolean version, Class<T> viewType) {
        super(mode, strategy, version, viewType);
    }

    @Override
    protected String[] getFetchedCollections() {
        return new String[] { "nameMap" };
    }

    public T updateReplaceCollection() {
        // Given
        final T docView = getDoc1View();
        clearQueries();
        
        // When
        docView.setNameMap(new HashMap<>(docView.getNameMap()));
        return docView;
    }

    public T updateAddToCollection() {
        // Given
        final T docView = getDoc1View();
        clearQueries();
        
        // When
        docView.getNameMap().put("newPrimaryName", new NameObject("newPrimaryName", "newSecondaryName"));
        return docView;
    }

    public T updateAddToNewCollection() {
        // Given
        final T docView = getDoc1View();
        clearQueries();

        // When
        docView.setNameMap(new HashMap<>(docView.getNameMap()));
        docView.getNameMap().put("newPrimaryName", new NameObject("newPrimaryName", "newSecondaryName"));
        return docView;
    }

    public T updateRemoveNonExisting() {
        // Given
        final T docView = getDoc1View();
        clearQueries();

        // When
        docView.getNameMap().remove("non-existing");
        return docView;
    }

}
