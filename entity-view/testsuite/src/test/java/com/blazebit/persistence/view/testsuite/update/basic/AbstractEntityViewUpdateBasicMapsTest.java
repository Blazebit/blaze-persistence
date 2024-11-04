/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.basic;

import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateDocumentTest;
import com.blazebit.persistence.view.testsuite.update.basic.model.UpdatableDocumentBasicWithMapsViewBase;
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
@Category({ NoDatanucleus.class, NoEclipselink.class})
public abstract class AbstractEntityViewUpdateBasicMapsTest<T extends UpdatableDocumentBasicWithMapsViewBase> extends AbstractEntityViewUpdateDocumentTest<T> {

    public AbstractEntityViewUpdateBasicMapsTest(FlushMode mode, FlushStrategy strategy, boolean version, Class<T> viewType) {
        super(mode, strategy, version, viewType);
    }

    @Override
    protected String[] getFetchedCollections() {
        return new String[] { "stringMap" };
    }

    public T updateReplaceCollection() {
        // Given
        final T docView = getDoc1View();
        clearQueries();
        
        // When
        docView.setStringMap(new HashMap<>(docView.getStringMap()));
        return docView;
    }

    public T updateAddToCollection() {
        // Given
        final T docView = getDoc1View();
        clearQueries();
        
        // When
        docView.getStringMap().put("newString", "newString");
        return docView;
    }

    public T updateAddToNewCollection() {
        // Given
        final T docView = getDoc1View();
        clearQueries();

        // When
        docView.setStringMap(new HashMap<>(docView.getStringMap()));
        docView.getStringMap().put("newString", "newString");
        return docView;
    }

    public T updateRemoveNonExisting() {
        // Given
        final T docView = getDoc1View();
        clearQueries();

        // When
        docView.getStringMap().remove("non-existing");
        return docView;
    }

    public T updateRemoveNull() {
        // Given
        final T docView = getDoc1View();
        clearQueries();

        // When
        docView.getStringMap().remove(null);
        return docView;
    }

}
