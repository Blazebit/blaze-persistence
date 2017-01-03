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

package com.blazebit.persistence.criteria.impl.path;

import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SingularAttributePath<X> extends AbstractPath<X> {

    private static final long serialVersionUID = 1L;
    
    private final SingularAttribute<?, X> attribute;
    private final ManagedType<X> managedType;

    public SingularAttributePath(BlazeCriteriaBuilderImpl criteriaBuilder, Class<X> javaType, AbstractPath<?> pathSource, SingularAttribute<?, X> attribute) {
        super(criteriaBuilder, javaType, pathSource);
        this.attribute = attribute;
        this.managedType = getManagedType(attribute);
    }

    private SingularAttributePath(SingularAttributePath<X> origin, String alias) {
        super(origin.criteriaBuilder, origin.getJavaType(), origin.getBasePath());
        this.attribute = origin.attribute;
        this.managedType = origin.managedType;
        this.setAlias(alias);
    }

    @Override
    public Selection<X> alias(String alias) {
        return new SingularAttributePath<X>(this, alias);
    }

    private ManagedType<X> getManagedType(SingularAttribute<?, X> attribute) {
        if (Attribute.PersistentAttributeType.BASIC == attribute.getPersistentAttributeType()) {
            return null;
        } else if (Attribute.PersistentAttributeType.EMBEDDED == attribute.getPersistentAttributeType()) {
            return (EmbeddableType<X>) attribute.getType();
        } else {
            return (IdentifiableType<X>) attribute.getType();
        }
    }

    @Override
    public SingularAttribute<?, X> getAttribute() {
        return attribute;
    }

    @Override
    public Bindable<X> getModel() {
        return getAttribute();
    }

    @Override
    protected boolean isDereferencable() {
        return managedType != null;
    }

    @Override
    protected Attribute findAttribute(String attributeName) {
        final Attribute attribute = managedType.getAttribute(attributeName);
        // Some old hibernate versions don't throw an exception but return null
        if (attribute == null) {
            throw new IllegalArgumentException("Could not resolve attribute named: " + attributeName);
        }
        return attribute;
    }
}
