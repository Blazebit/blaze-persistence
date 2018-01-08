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

package com.blazebit.persistence.criteria.impl.path;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;

import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.PluralJoin;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.Type;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractPluralAttributeJoin<O, C, E> extends AbstractJoin<O, E> implements PluralJoin<O, C, E> {

    private static final long serialVersionUID = 1L;

    protected AbstractPluralAttributeJoin(BlazeCriteriaBuilderImpl criteriaBuilder, AbstractJoin<O, ? super E> original, EntityType<E> treatType) {
        super(criteriaBuilder, original, treatType);
    }

    public AbstractPluralAttributeJoin(BlazeCriteriaBuilderImpl criteriaBuilder, Class<E> javaType, AbstractPath<O> pathSource, Attribute<? super O, ?> joinAttribute, JoinType joinType) {
        super(criteriaBuilder, javaType, pathSource, joinAttribute, joinType);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public PluralAttribute<? super O, C, E> getAttribute() {
        return (PluralAttribute<? super O, C, E>) super.getAttribute();
    }

    public PluralAttribute<? super O, C, E> getModel() {
        return getAttribute();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected ManagedType<E> getManagedType() {
        if (treatJoinType != null) {
            return (ManagedType<E>) treatJoinType;
        }
        return isBasicCollection() ? null : (ManagedType<E>) getAttribute().getElementType();
    }

    public boolean isBasicCollection() {
        return Type.PersistenceType.BASIC.equals(getAttribute().getElementType().getPersistenceType());
    }

    @Override
    protected boolean isDereferencable() {
        return !isBasicCollection();
    }

    @Override
    protected boolean isJoinAllowed() {
        return !isBasicCollection();
    }
}
