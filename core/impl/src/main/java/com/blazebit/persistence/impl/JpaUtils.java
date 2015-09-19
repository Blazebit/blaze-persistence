/*
 * Copyright 2015 Blazebit.
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
package com.blazebit.persistence.impl;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public final class JpaUtils {

    public static <T> Attribute<? super T, ?> getAttribute(ManagedType<T> type, String attributeName) {
        try {
            return type.getAttribute(attributeName);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public static Set<Attribute<?, ?>> getAttributesPolymorphic(Metamodel metamodel, ManagedType<?> type, String attributeName) {
        Attribute<?, ?> attr = getAttribute(type, attributeName);

        if (attr != null) {
            Set<Attribute<?, ?>> set = new HashSet<Attribute<?, ?>>(1);
            set.add(attr);
            return set;
        }

        // Try again polymorphic
        Class<?> javaType = type.getJavaType();
        Set<ManagedType<?>> possibleSubTypes = new HashSet<ManagedType<?>>();

        // Collect all possible subtypes of the given type
        for (ManagedType<?> subType : metamodel.getManagedTypes()) {
            if (javaType.isAssignableFrom(subType.getJavaType()) && javaType != subType.getJavaType()) {
                possibleSubTypes.add(subType);
            }
        }

        Set<Attribute<?, ?>> resolvedAttributes = new HashSet<Attribute<?, ?>>();

        // Collect all the attributes that resolve on every possible subtype
        for (ManagedType<?> subType : possibleSubTypes) {
            attr = JpaUtils.getAttribute(subType, attributeName);

            if (attr != null) {
                resolvedAttributes.add(attr);
            }
        }

        return resolvedAttributes;
    }

    public static boolean isMap(Attribute<?, ?> attr) {
        return attr instanceof MapAttribute<?, ?, ?>;
    }

    public static boolean isOptional(Attribute<?, ?> attribute) {
        if (attribute instanceof SingularAttribute<?, ?>) {
            return ((SingularAttribute<?, ?>) attribute).isOptional();
        }

        return true;
    }

	public static Attribute<?, ?> getIdAttribute(EntityType<?> entityType) {
		return entityType.getId(entityType.getIdType().getJavaType());
	}
}
