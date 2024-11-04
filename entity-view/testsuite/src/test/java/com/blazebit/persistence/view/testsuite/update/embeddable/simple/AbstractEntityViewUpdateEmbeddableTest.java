/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.embeddable.simple;

import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.NameObject;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateDocumentTest;
import com.blazebit.persistence.view.testsuite.update.embeddable.simple.model.UpdatableDocumentEmbeddableViewBase;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public abstract class AbstractEntityViewUpdateEmbeddableTest<T extends UpdatableDocumentEmbeddableViewBase> extends AbstractEntityViewUpdateDocumentTest<T> {

    public AbstractEntityViewUpdateEmbeddableTest(FlushMode mode, FlushStrategy strategy, boolean version, Class<T> viewType) {
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
        docView.setNameObject(new NameObject("newPrimaryName", "newSecondaryName"));
        return docView;
    }

    public T mutateMutable() {
        // Given
        final T docView = getDoc1View();
        clearQueries();

        // When
        docView.getNameObject().setPrimaryName("newPrimaryName");
        docView.getNameObject().setSecondaryName("newSecondaryName");
        return docView;
    }

}
