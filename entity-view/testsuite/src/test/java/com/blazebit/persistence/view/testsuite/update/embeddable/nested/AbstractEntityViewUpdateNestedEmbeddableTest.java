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

package com.blazebit.persistence.view.testsuite.update.embeddable.nested;

import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.NameObject;
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
