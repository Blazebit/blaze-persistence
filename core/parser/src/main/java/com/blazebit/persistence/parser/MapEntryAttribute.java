/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.parser;

import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;
import java.lang.reflect.Member;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MapEntryAttribute<X> implements SingularAttribute<X, Map.Entry<?, ?>>, QualifiedAttribute {

    private static final Type<Map.Entry<?, ?>> ENTRY_TYPE = new Type<Map.Entry<?, ?>>() {
        @Override
        public PersistenceType getPersistenceType() {
            return PersistenceType.BASIC;
        }

        @Override
        public Class<Map.Entry<?, ?>> getJavaType() {
            return (Class<Map.Entry<?, ?>>) (Class) Map.Entry.class;
        }
    };

    private final MapAttribute<?, ?, ?> attribute;

    public MapEntryAttribute(MapAttribute<?, ?, ?> attribute) {
        this.attribute = attribute;
    }

    @Override
    public PluralAttribute<?, ?, ?> getPluralAttribute() {
        return attribute;
    }

    @Override
    public String getQualificationExpression() {
        return "ENTRY";
    }

    @Override
    public String getName() {
        return attribute.getName() + "_entry";
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
    public Class<Map.Entry<?, ?>> getJavaType() {
        return (Class<Map.Entry<?, ?>>) (Class) Map.Entry.class;
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
    public Type<Map.Entry<?, ?>> getType() {
        return ENTRY_TYPE;
    }

    @Override
    public BindableType getBindableType() {
        return BindableType.SINGULAR_ATTRIBUTE;
    }

    @Override
    public Class<Map.Entry<?, ?>> getBindableJavaType() {
        return (Class<Map.Entry<?, ?>>) (Class) Map.Entry.class;
    }
}
