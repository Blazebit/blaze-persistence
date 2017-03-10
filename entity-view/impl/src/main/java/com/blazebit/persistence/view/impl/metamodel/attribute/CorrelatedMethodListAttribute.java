/*
 * Copyright 2014 - 2017 Blazebit.
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

package com.blazebit.persistence.view.impl.metamodel.attribute;

import com.blazebit.persistence.impl.EntityMetamodel;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.view.metamodel.CorrelatedAttribute;
import com.blazebit.persistence.view.metamodel.ManagedViewType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CorrelatedMethodListAttribute<X, Y> extends AbstractMethodListAttribute<X, Y> implements CorrelatedAttribute<X, List<Y>> {

    public CorrelatedMethodListAttribute(ManagedViewType<X> viewType, Method method, Annotation mapping, Set<Class<?>> entityViews, EntityMetamodel metamodel, ExpressionFactory expressionFactory, Set<String> errors) {
        super(viewType, method, mapping, entityViews, metamodel, expressionFactory, errors);
    }

    @Override
    public boolean isCorrelated() {
        return true;
    }
}
