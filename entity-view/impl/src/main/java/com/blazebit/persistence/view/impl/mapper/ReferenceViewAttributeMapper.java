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
