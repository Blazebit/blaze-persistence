/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.embeddable.nested;

import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.model.SimpleEmbeddableEntityView;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.model.UpdatableEmbeddableEntityWithCollectionsEmbeddableViewBase;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.model.UpdatableEmbeddableEntityWithCollectionsViewBase;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.HashSet;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public abstract class AbstractEntityViewUpdateNestedEmbeddableCollectionsTest<T extends UpdatableEmbeddableEntityWithCollectionsViewBase> extends AbstractEntityViewUpdateNestedEmbeddableEntityTest<T> {

    public AbstractEntityViewUpdateNestedEmbeddableCollectionsTest(FlushMode mode, FlushStrategy strategy, boolean version, Class<T> viewType) {
        super(mode, strategy, version, viewType, UpdatableEmbeddableEntityWithCollectionsEmbeddableViewBase.class);
    }

    public T updateReplaceCollection() {
        // Given
        final T docView = getEnt1View();
        clearQueries();
        
        // When
        docView.getEmbeddable().setOneToMany2(new HashSet<>(docView.getEmbeddable().getOneToMany2()));
        return docView;
    }

    public T updateAddToCollection() {
        // Given
        final T docView = getEnt1View();
        clearQueries();
        
        // When
        docView.getEmbeddable().getOneToMany2().add(evm.getReference(SimpleEmbeddableEntityView.class, entity2.getId()));
        return docView;
    }

    public T updateAddToNewCollection() {
        // Given
        final T docView = getEnt1View();
        clearQueries();

        // When
        docView.getEmbeddable().setOneToMany2(new HashSet<>(docView.getEmbeddable().getOneToMany2()));
        docView.getEmbeddable().getOneToMany2().add(evm.getReference(SimpleEmbeddableEntityView.class, entity2.getId()));
        return docView;
    }

}
