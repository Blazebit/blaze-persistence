/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl.path;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.PluralJoin;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.Type;

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
