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

import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntity;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityEmbeddable;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityId;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityNestedEmbeddable;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.entity.NameObject;
import com.blazebit.persistence.view.FlushMode;
import com.blazebit.persistence.view.FlushStrategy;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.update.AbstractEntityViewUpdateTest;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.model.SimpleEmbeddableEntityView;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.model.SimpleIntIdEntityView;
import com.blazebit.persistence.view.testsuite.update.embeddable.nested.model.SimpleNameObjectView;

import javax.persistence.EntityManager;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public abstract class AbstractEntityViewUpdateNestedEmbeddableEntityTest<T> extends AbstractEntityViewUpdateTest<T> {

    protected EmbeddableTestEntity entity1;
    protected EmbeddableTestEntity entity2;
    protected IntIdEntity intIdEntity1;
    protected IntIdEntity intIdEntity2;

    public AbstractEntityViewUpdateNestedEmbeddableEntityTest(FlushMode mode, FlushStrategy strategy, boolean version, Class<T> viewType) {
        super(mode, strategy, version, viewType);
    }

    public AbstractEntityViewUpdateNestedEmbeddableEntityTest(FlushMode mode, FlushStrategy strategy, boolean version, Class<T> viewType, Class<?>... views) {
        super(mode, strategy, version, viewType, views);
    }

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
                EmbeddableTestEntity.class,
                IntIdEntity.class,
                EmbeddableTestEntityEmbeddable.class,
                EmbeddableTestEntityId.class,
                EmbeddableTestEntityNestedEmbeddable.class,
                NameObject.class
        };
    }

    @Override
    protected void registerViewTypes(EntityViewConfiguration cfg) {
        cfg.addEntityView(SimpleEmbeddableEntityView.class);
        cfg.addEntityView(SimpleIntIdEntityView.class);
        cfg.addEntityView(SimpleNameObjectView.class);
    }

    @Override
    protected void prepareData(EntityManager em) {
        entity1 = new EmbeddableTestEntity();
        entity1.getId().setKey("e1");
        entity1.getId().setValue("e1");
        entity1.setVersion(1L);

        entity2 = new EmbeddableTestEntity();
        entity2.getId().setKey("e2");
        entity2.getId().setValue("e2");
        entity2.setVersion(1L);

        intIdEntity1 = new IntIdEntity("i1", 1);
        intIdEntity2 = new IntIdEntity("i2", 2);

        em.persist(entity1);
        em.persist(entity2);
        em.persist(intIdEntity1);
        em.persist(intIdEntity2);

        entity1.getEmbeddable().setManyToOne(entity1);
        entity1.getEmbeddable().getManyToMany().put("a", intIdEntity1);
        entity1.getEmbeddable().getOneToMany2().add(entity1);
        entity1.getEmbeddable().getElementCollection().put("a", new NameObject("a", "b", intIdEntity1));
        entity1.getEmbeddable().getNestedEmbeddable().getNestedOneToMany().add(entity1);
    }

    @Override
    protected void restartTransactionAndReload() {
        restartTransaction();
        // Load into PC, then access via find
        cbf.create(em, IntIdEntity.class)
                .getResultList();
        cbf.create(em, EmbeddableTestEntity.class)
                .fetch("embeddable.manyToOne", "embeddable.manyToMany", "embeddable.oneToMany", "embeddable.elementCollection", "embeddable.nestedEmbeddable.nestedOneToMany")
                .getResultList();
        entity1 = em.find(EmbeddableTestEntity.class, entity1.getId());
        entity2 = em.find(EmbeddableTestEntity.class, entity2.getId());
        intIdEntity1 = em.find(IntIdEntity.class, intIdEntity1.getId());
        intIdEntity2 = em.find(IntIdEntity.class, intIdEntity2.getId());
    }

    protected T getEnt1View() {
        return evm.find(em, viewType, entity1.getId());
    }

    protected T getEnt2View() {
        return evm.find(em, viewType, entity2.getId());
    }

    protected <P> P getInt1View(Class<P> intView) {
        return evm.find(em, intView, intIdEntity1.getId());
    }

    protected <P> P getInt2View(Class<P> intView) {
        return evm.find(em, intView, intIdEntity2.getId());
    }

    protected <D> D getEntView(Long id, Class<D> entView) {
        return evm.find(em, entView, id);
    }

    protected <P> P getIntView(Long id, Class<P> intView) {
        return evm.find(em, intView, id);
    }

}
