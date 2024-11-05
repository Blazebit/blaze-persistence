/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.basic;

import java.util.Date;

import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateDocumentTest;
import com.blazebit.persistence.view.testsuite.update.basic.model.UpdatableDocumentBasicViewBase;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
@Category({ NoEclipselink.class})
public abstract class AbstractEntityViewUpdateBasicTest<T extends UpdatableDocumentBasicViewBase> extends AbstractEntityViewUpdateDocumentTest<T> {

    public AbstractEntityViewUpdateBasicTest(FlushMode mode, FlushStrategy strategy, boolean version, Class<T> viewType) {
        super(mode, strategy, version, viewType);
    }

    public T simpleUpdate() {
        // Given
        final T docView = getDoc1View();
        clearQueries();
        
        // When
        docView.setName("newDoc");
        return docView;
    }

    public T updateMutable() {
        // Given
        final T docView = getDoc1View();
        clearQueries();

        // When
        docView.setLastModified(new Date(0));
        return docView;
    }

    public T mutateMutable() {
        // Given
        final T docView = getDoc1View();
        clearQueries();

        // When
        docView.getLastModified().setTime(0);
        return docView;
    }

}
