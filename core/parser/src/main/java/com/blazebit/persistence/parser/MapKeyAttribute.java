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

package com.blazebit.persistence.parser;

import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;
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
