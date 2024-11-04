/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.embeddable.nested;

import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.model.SimpleEmbeddableEntityView;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.model.UpdatableEmbeddableEntityEmbeddableViewBase;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.model.UpdatableEmbeddableEntityViewBase;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public abstract class AbstractEntityViewUpdateNestedEmbeddableTest<T extends UpdatableEmbeddableEntityViewBase> extends AbstractEntityViewUpdateNestedEmbeddableEntityTest<T> {

    public AbstractEntityViewUpdateNestedEmbeddableTest(FlushMode mode, FlushStrategy strategy, boolean version, Class<T> viewType) {
        super(mode, strategy, version, viewType, UpdatableEmbeddableEntityEmbeddableViewBase.class);
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        super.registerViewTypes(cfg);
        cfg.addEntityView(UpdatableEmbeddableEntityViewBase.Id.class);
    }

    public T simpleUpdate() {
        // Given
        final T docView = getEnt1View();
        clearQueries();
        
        // When
        docView.setEmbeddable(null);
        return docView;
    }

    public T updateMutable() {
        // Given
        final T docView = getEnt1View();
        clearQueries();

        // When
        docView.getEmbeddable().setManyToOne(evm.getReference(SimpleEmbeddableEntityView.class, entity2.getId()));
        return docView;
    }

}
