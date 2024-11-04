/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.proxy.model;

import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntity;
import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityEmbeddable;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.PostCreate;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.testsuite.basic.model.IntIdEntityView;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@CreatableEntityView
@UpdatableEntityView
@EntityView(EmbeddableTestEntity.class)
public abstract class UpdatableEmbeddableTestEntityView extends EmbeddableTestEntityView {

    @PostCreate
    private void postCreate(EntityViewManager evm) {
        getEmbeddable().getElementCollection().put("test", evm.create(UpdatableNameObjectView.class));
    }

    @MappingParameter("test")
    public abstract Object getTest();
    public abstract EmbeddableTestEntityEmbeddableView getEmbeddable();
    public abstract void setEmbeddable(EmbeddableTestEntityEmbeddableView embeddable);

    @Mapping("embeddable")
    public abstract ReadOnlyEmbeddableTestEntityEmbeddableView getMyEmbeddable();
    public abstract List<NameObjectView> getElementCollection4();

    @EntityView(EmbeddableTestEntityEmbeddable.class)
    public static interface ReadOnlyEmbeddableTestEntityEmbeddableView {

        public EmbeddableTestEntityView getManyToOne();
        public Set<EmbeddableTestEntityView> getOneToMany();
        public Map<String, ? extends NameObjectView> getElementCollection();
        public Map<String, IntIdEntityView> getManyToMany();
        public EmbeddableTestEntityNestedEmbeddableView getNestedEmbeddable();
    }

    @UpdatableEntityView
    @EntityView(EmbeddableTestEntityEmbeddable.class)
    public static interface EmbeddableTestEntityEmbeddableView extends ReadOnlyEmbeddableTestEntityEmbeddableView {

        public EmbeddableTestEntityView getManyToOne();
        public void setManyToOne(EmbeddableTestEntityView manyToOne);

        public Set<EmbeddableTestEntityView> getOneToMany();
        public void setOneToMany(Set<EmbeddableTestEntityView> oneToMany);

        public Map<String, UpdatableNameObjectView> getElementCollection();
        public void setElementCollection(Map<String, UpdatableNameObjectView> elementCollection);

        public Map<String, IntIdEntityView> getManyToMany();
        public void setManyToMany(Map<String, IntIdEntityView> manyToMany);

        public UpdatableEmbeddableTestEntityNestedEmbeddableView getNestedEmbeddable();
        public void setNestedEmbeddable(UpdatableEmbeddableTestEntityNestedEmbeddableView nestedEmbeddable);
    }
}
