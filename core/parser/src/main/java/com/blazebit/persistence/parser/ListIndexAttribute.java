/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser;

import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.Type;
import java.lang.reflect.Member;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ListIndexAttribute<X> implements SingularAttribute<X, Integer>, QualifiedAttribute {

    private static final Type<Integer> INDEX_TYPE = new Type<Integer>() {
        @Override
        public PersistenceType getPersistenceType() {
            return PersistenceType.BASIC;
        }

        @Override
        public Class<Integer> getJavaType() {
            return Integer.class;
        }
    };

    private final ListAttribute<?, ?> attribute;

    public ListIndexAttribute(ListAttribute<?, ?> attribute) {
        this.attribute = attribute;
    }

    @Override
    public PluralAttribute<?, ?, ?> getPluralAttribute() {
        return attribute;
    }

    @Override
    public String getQualificationExpression() {
        return "INDEX";
    }

    @Override
    public String getName() {
        return attribute.getName() + "_index";
    }

    @Override
    public PersistentAttributeType getPersistentAttributeType() {
        return PersistentAttributeType.BASIC;
    }

    @Override
    public ManagedType<X> getDeclaringType() {
        return null;
    }

    @Override
    public Class<Integer> getJavaType() {
        return Integer.class;
    }

    @Override
    public Member getJavaMember() {
        return null;
    }

    @Override
    public boolean isAssociation() {
        return false;
    }

    @Override
    public boolean isCollection() {
        return false;
    }

    @Override
    public boolean isId() {
        return false;
    }

    @Override
    public boolean isVersion() {
        return false;
    }

    @Override
    public boolean isOptional() {
        return false;
    }

    @Override
    public Type<Integer> getType() {
        return INDEX_TYPE;
    }

    @Override
    public BindableType getBindableType() {
        return BindableType.SINGULAR_ATTRIBUTE;
    }

    @Override
    public Class<Integer> getBindableJavaType() {
        return Integer.class;
    }
}
