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

package com.blazebit.persistence.view.impl.update;

import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.accessor.Accessors;
import com.blazebit.persistence.view.impl.accessor.AttributeAccessor;
import com.blazebit.persistence.view.impl.entity.EntityTupleizer;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MethodAttribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class DefaultEntityTupleizer implements EntityTupleizer {

    private final AttributeAccessor[] accessors;

    @SuppressWarnings("unchecked")
    public DefaultEntityTupleizer(EntityViewManagerImpl evm, ManagedViewType<?> view) {
        Set<MethodAttribute<?, ?>> attributes = (Set<MethodAttribute<?, ?>>) (Set<?>) view.getAttributes();
        List<AttributeAccessor> accessors = new ArrayList<>(attributes.size());

        for (MethodAttribute<?, ?> attr : attributes) {
            accessors.add(Accessors.forEntityMapping(evm, attr));
        }
        this.accessors = accessors.toArray(new AttributeAccessor[accessors.size()]);
    }

    @Override
    public Object[] tupleize(Object entity) {
        Object[] array = new Object[accessors.length];
        for (int i = 0; i < accessors.length; i++) {
            array[i] = accessors[i].getValue(entity);
        }
        return array;
    }
    
}
