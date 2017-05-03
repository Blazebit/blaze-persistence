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

package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.persistence.view.metamodel.BasicType;
import com.blazebit.persistence.view.spi.BasicUserType;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class BasicTypeImpl<X> implements BasicType<X> {

    private final Class<X> javaType;
    private final ManagedType<X> managedType;
    private final BasicUserType<X> userType;

    public BasicTypeImpl(Class<X> javaType, ManagedType<X> managedType, BasicUserType<X> userType) {
        this.javaType = javaType;
        this.managedType = managedType;
        this.userType = userType;
    }

    @Override
    public Class<X> getJavaType() {
        return javaType;
    }

    @Override
    public MappingType getMappingType() {
        return MappingType.BASIC;
    }

    @Override
    public BasicUserType<X> getUserType() {
        return userType;
    }

    public ManagedType<X> getManagedType() {
        return managedType;
    }

    public boolean isJpaManaged() {
        return managedType != null;
    }

    public boolean isJpaEntity() {
        return managedType instanceof EntityType<?>;
    }
}
