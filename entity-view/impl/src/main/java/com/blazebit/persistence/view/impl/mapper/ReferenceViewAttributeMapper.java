/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.mapper;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.entity.EntityTupleizer;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class ReferenceViewAttributeMapper<S, T> implements Mapper<S, T> {

    private final EntityViewManager evm;
    private final AttributeAccessor entityAccessor;
    private final EntityTupleizer entityTupleizer;
    private final Class<?> entityViewClass;
    private final AttributeAccessor viewAccessor;
    private final ObjectBuilder<?> idViewBuilder;

    public ReferenceViewAttributeMapper(EntityViewManager evm, AttributeAccessor entityAccessor, Class<?> entityViewClass, EntityTupleizer entityTupleizer, AttributeAccessor viewAccessor, ObjectBuilder<?> idViewBuilder) {
        this.evm = evm;
        this.entityAccessor = entityAccessor;
        this.entityViewClass = entityViewClass;
        this.entityTupleizer = entityTupleizer;
        this.viewAccessor = viewAccessor;
        this.idViewBuilder = idViewBuilder;
    }

    @Override
    public void map(S source, T target) {
        Object id = entityAccessor.getValue(source);
        if (idViewBuilder != null) {
            id =  idViewBuilder.build(entityTupleizer.tupleize(id));
        }
        viewAccessor.setValue(target, evm.getReference(entityViewClass, id));
    }
    
}
