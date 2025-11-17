/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser;

import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.MapAttribute;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.Type;
import java.lang.reflect.Member;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MapKeyAttribute<X, Y> implements SingularAttribute<X, Y>, QualifiedAttribute {

    private final MapAttribute<?, Y, ?> attribute;
    private final BindableType jpaBindableType;
    private final PersistentAttributeType persistentAttributeType;

    public MapKeyAttribute(MapAttribute<?, Y, ?> attribute) {
        this.attribute = attribute;
        Type<Y> jpaType = attribute.getKeyType();
        this.jpaBindableType = Type.PersistenceType.ENTITY
                .equals(jpaType.getPersistenceType()) ? BindableType.ENTITY_TYPE : BindableType.SINGULAR_ATTRIBUTE;

        this.persistentAttributeType = Type.PersistenceType.ENTITY
                .equals(jpaType.getPersistenceType()) ? PersistentAttributeType.MANY_TO_ONE : Type.PersistenceType.EMBEDDABLE
                .equals(jpaType.getPersistenceType()) ? PersistentAttributeType.EMBEDDED : PersistentAttributeType.BASIC;
    }

    @Override
    public PluralAttribute<?, ?, ?> getPluralAttribute() {
        return attribute;
    }

    @Override
    public String getQualificationExpression() {
        return "KEY";
    }

    @Override
    public String getName() {
        return attribute.getName() + "_key";
    }

    @Override
    public PersistentAttributeType getPersistentAttributeType() {
        return persistentAttributeType;
    }

    @Override
    public ManagedType<X> getDeclaringType() {
        return null;
    }

    @Override
    public Class<Y> getJavaType() {
        return attribute.getKeyJavaType();
    }

    @Override
    public Member getJavaMember() {
        return null;
    }

    @Override
    public boolean isAssociation() {
        return persistentAttributeType == PersistentAttributeType.MANY_TO_ONE;
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
    public Type<Y> getType() {
        return attribute.getKeyType();
    }

    @Override
    public BindableType getBindableType() {
        return jpaBindableType;
    }

    @Override
    public Class<Y> getBindableJavaType() {
        return attribute.getKeyJavaType();
    }
}
