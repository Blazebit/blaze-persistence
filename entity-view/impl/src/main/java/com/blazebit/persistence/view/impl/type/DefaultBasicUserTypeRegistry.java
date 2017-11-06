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

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.impl.EntityMetamodel;
import com.blazebit.persistence.view.spi.BasicUserType;

import javax.persistence.EntityManagerFactory;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class DefaultBasicUserTypeRegistry implements BasicUserTypeRegistry {

    private final Map<Class<?>, BasicUserType<?>> basicUserTypes = new HashMap<>();
    private final EntityMetamodel entityMetamodel;
    private final BasicUserType<Object> entityBasicUserType;

    public DefaultBasicUserTypeRegistry(BasicUserTypeRegistry original, CriteriaBuilderFactory cbf) {
        this.basicUserTypes.putAll(original.getBasicUserTypes());
        this.entityMetamodel = cbf.getService(EntityMetamodel.class);
        this.entityBasicUserType = new EntityBasicUserType(cbf.getService(EntityManagerFactory.class).getPersistenceUnitUtil());
    }

    @Override
    public <X> void registerBasicUserType(Class<X> clazz, BasicUserType<X> userType) {
        basicUserTypes.put(clazz, userType);
    }

    @Override
    public Map<Class<?>, BasicUserType<?>> getBasicUserTypes() {
        return basicUserTypes;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <X> BasicUserType<X> getBasicUserType(Class<X> clazz) {
        BasicUserType<?> userType = basicUserTypes.get(clazz);
        if (userType == null) {
            if (clazz.isEnum()) {
                // Enums are always considered immutable
                userType = ImmutableBasicUserType.INSTANCE;
            } else if (entityMetamodel.getEntity(clazz) != null) {
                userType = entityBasicUserType;
            } else if (Date.class.isAssignableFrom(clazz)) {
                userType = DateBasicUserType.INSTANCE;
            } else if (Calendar.class.isAssignableFrom(clazz)) {
                userType = CalendarBasicUserType.INSTANCE;
            } else {
                userType = MutableBasicUserType.INSTANCE;
            }
        }

        return (BasicUserType<X>) userType;
    }
}
