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

package com.blazebit.persistence.view.impl.accessor;

import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.proxy.DirtyTracker;
import com.blazebit.persistence.view.impl.metamodel.AbstractMethodAttribute;
import com.blazebit.persistence.view.impl.proxy.DirtyStateTrackable;
import com.blazebit.persistence.view.impl.proxy.MutableStateTrackable;
import com.blazebit.persistence.view.metamodel.BasicType;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.persistence.view.spi.type.BasicUserType;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public final class DirtyStateViewAttributeAccessor extends ViewAttributeAccessor implements InitialValueAttributeAccessor {

    private final int dirtyStateIndex;
    private final BasicUserType<Object> userType;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public DirtyStateViewAttributeAccessor(EntityViewManagerImpl evm, MethodAttribute<?, ?> attribute) {
        super(evm, attribute, false);
        this.dirtyStateIndex = ((AbstractMethodAttribute<?, ?>) attribute).getDirtyStateIndex();
        if (attribute instanceof SingularAttribute<?, ?>) {
            SingularAttribute<?, ?> singularAttribute = (SingularAttribute<?, ?>) attribute;
            if (singularAttribute.getType() instanceof BasicType<?>) {
                userType = ((BasicType) singularAttribute.getType()).getUserType();
            } else {
                userType = null;
            }
        } else {
            userType = null;
        }
    }

    @Override
    public Object getMutableStateValue(Object view) {
        if (view instanceof MutableStateTrackable) {
            return ((MutableStateTrackable) view).$$_getMutableState()[dirtyStateIndex];
        } else {
            return null;
        }
    }

    @Override
    public Object getInitialValue(Object view) {
        if (view instanceof DirtyStateTrackable) {
            return ((DirtyStateTrackable) view).$$_getInitialState()[dirtyStateIndex];
        } else {
            return null;
        }
    }

    @Override
    public void setInitialValue(Object view, Object initialValue) {
        if (view instanceof DirtyStateTrackable) {
            ((DirtyStateTrackable) view).$$_getInitialState()[dirtyStateIndex] = initialValue;
        }
    }

    @Override
    public void setValue(Object view, Object value) {
        super.setValue(view, value);
        if (view instanceof MutableStateTrackable) {
            MutableStateTrackable mutableStateTrackable = (MutableStateTrackable) view;
            if (value instanceof DirtyTracker) {
                ((DirtyTracker) value).$$_setParent(mutableStateTrackable, dirtyStateIndex);
            }
            mutableStateTrackable.$$_getMutableState()[dirtyStateIndex] = value;
            if (view instanceof DirtyStateTrackable) {
                if (userType != null) {
                    ((DirtyStateTrackable) view).$$_getInitialState()[dirtyStateIndex] = userType.deepClone(value);
                } else {
                    ((DirtyStateTrackable) view).$$_getInitialState()[dirtyStateIndex] = value;
                }
            }
        }
    }
}
