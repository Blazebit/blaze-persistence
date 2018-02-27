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

import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;
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
