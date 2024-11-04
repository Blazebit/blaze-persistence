/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.embeddable.nested;

import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.NameObject;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.model.SimpleEmbeddableEntityView;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.model.SimpleIntIdEntityView;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.model.SimpleNameObjectView;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.model.UpdatableEmbeddableEntityWithMapsEmbeddableViewBase;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.model.UpdatableEmbeddableEntityWithMapsViewBase;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.HashMap;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@RunWith(Parameterized.class)
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public abstract class AbstractEntityViewUpdateNestedEmbeddableMapsTest<T extends UpdatableEmbeddableEntityWithMapsViewBase> extends AbstractEntityViewUpdateNestedEmbeddableEntityTest<T> {

    public AbstractEntityViewUpdateNestedEmbeddableMapsTest(FlushMode mode, FlushStrategy strategy, boolean version, Class<T> viewType) {
        super(mode, strategy, version, viewType, UpdatableEmbeddableEntityWithMapsEmbeddableViewBase.class);
    }

    public T updateReplaceCollection() {
        // Given
        final T docView = getEnt1View();
        clearQueries();
        
        // When
        docView.getEmbeddable().setManyToMany(new HashMap<>(docView.getEmbeddable().getManyToMany()));
        docView.getEmbeddable().setElementCollection(new HashMap<>(docView.getEmbeddable().getElementCollection()));
        return docView;
    }

    public T updateAddToCollection() {
        // Given
        final T docView = getEnt1View();
        clearQueries();
        
        // When
        docView.getEmbeddable().getManyToMany().put("b", evm.getReference(SimpleIntIdEntityView.class, intIdEntity2.getId()));
        SimpleNameObjectView nameObjectView = evm.create(SimpleNameObjectView.class);
        nameObjectView.setPrimaryName("newPrimaryName");
        nameObjectView.setSecondaryName("newSecondaryName");
        docView.getEmbeddable().getElementCollection().put("newPrimaryName", nameObjectView);
        return docView;
    }

    public T updateAddToNewCollection() {
        // Given
        final T docView = getEnt1View();
        clearQueries();

        // When
        docView.getEmbeddable().setManyToMany(new HashMap<>(docView.getEmbeddable().getManyToMany()));
        docView.getEmbeddable().setElementCollection(new HashMap<>(docView.getEmbeddable().getElementCollection()));
        docView.getEmbeddable().getManyToMany().put("b", evm.getReference(SimpleIntIdEntityView.class, intIdEntity2.getId()));
        SimpleNameObjectView nameObjectView = evm.create(SimpleNameObjectView.class);
        nameObjectView.setPrimaryName("newPrimaryName");
        nameObjectView.setSecondaryName("newSecondaryName");
        docView.getEmbeddable().getElementCollection().put("newPrimaryName", nameObjectView);
        return docView;
    }

}
