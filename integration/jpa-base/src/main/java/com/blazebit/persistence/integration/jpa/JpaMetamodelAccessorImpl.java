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

package com.blazebit.persistence.integration.jpa;

import com.blazebit.persistence.parser.ListIndexAttribute;
import com.blazebit.persistence.parser.MapKeyAttribute;
import com.blazebit.persistence.spi.AttributePath;
import com.blazebit.persistence.spi.JpaMetamodelAccessor;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Christian Beikov
 * @author Jan-Willem Gmelig Meyling
 * @since 1.3.0
 */
public class JpaMetamodelAccessorImpl implements JpaMetamodelAccessor {

    public static final JpaMetamodelAccessorImpl INSTANCE = new JpaMetamodelAccessorImpl();

    protected JpaMetamodelAccessorImpl() {
    }


    @Override
    public AttributePath getAttributePath(Metamodel metamodel, ManagedType<?> type, String attributePath) {
        List<Attribute<?, ?>> attrPath;

        if (attributePath.indexOf('.') == -1) {
            attrPath = new ArrayList<Attribute<?, ?>>(1);
            Attribute<?, ?> attribute = type.getAttribute(attributePath);
            if (attribute == null) {
                // Well, some implementations might not be fully spec compliant..
                throw new IllegalArgumentException("Attribute '" + attributePath + "' does not exist on '" + com.blazebit.persistence.parser.util.JpaMetamodelUtils.getTypeName(type) + "'!");
            }

            attrPath.add(attribute);
            return new AttributePath(attrPath, com.blazebit.persistence.parser.util.JpaMetamodelUtils.resolveFieldClass(type.getJavaType(), attribute));
        } else {
            attrPath = new ArrayList<Attribute<?, ?>>();
        }

        String[] attributeParts = attributePath.split("\\.");
        ManagedType<?> currentType = type;
        Class<?> currentClass = type.getJavaType();

        for (int i = 0; i < attributeParts.length; i++) {
            Attribute<?, ?> attr = null;
            if (currentType == null) {
                // dereference basic
                break;
            }
            attr = com.blazebit.persistence.parser.util.JpaMetamodelUtils.getAttribute(currentType, attributeParts[i]);
            if (attr == null) {
                attrPath.clear();
                break;
            }

            currentClass = com.blazebit.persistence.parser.util.JpaMetamodelUtils.resolveFieldClass(currentClass, attr);
            if (attr instanceof PluralAttribute<?, ?, ?>) {
                PluralAttribute<?, ?, ?> pluralAttr = (PluralAttribute<?, ?, ?>) attr;
                Type<?> elementType = pluralAttr.getElementType();
                if (elementType.getPersistenceType() == Type.PersistenceType.EMBEDDABLE) {
                    currentType = metamodel.embeddable(currentClass);
                } else if (elementType.getPersistenceType() == Type.PersistenceType.BASIC) {
                    currentType = null;
                } else {
                    currentType = metamodel.entity(currentClass);
                }
            } else if (attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED) {
                currentType = metamodel.embeddable(currentClass);
            } else if (attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.BASIC) {
                currentType = null;
            } else {
                currentType = metamodel.entity(currentClass);
            }

            attrPath.add(attr);
        }

        if (attrPath.isEmpty()) {
            throw new IllegalArgumentException("Path " + attributePath + " does not exist on entity " + com.blazebit.persistence.parser.util.JpaMetamodelUtils.getTypeName(type));
        }

        return new AttributePath(attrPath, currentClass);
    }

    @Override
    public AttributePath getBasicAttributePath(Metamodel metamodel, ManagedType<?> type, String attributePath) {
        List<Attribute<?, ?>> attrPath;

        if (attributePath.indexOf('.') == -1) {
            attrPath = new ArrayList<Attribute<?, ?>>(1);
            Attribute<?, ?> attribute = type.getAttribute(attributePath);
            if (attribute == null) {
                // Well, some implementations might not be fully spec compliant..
                throw new IllegalArgumentException("Attribute '" + attributePath + "' does not exist on '" + com.blazebit.persistence.parser.util.JpaMetamodelUtils.getTypeName(type) + "'!");
            }

            attrPath.add(attribute);
            return new AttributePath(attrPath, com.blazebit.persistence.parser.util.JpaMetamodelUtils.resolveFieldClass(type.getJavaType(), attribute));
        } else {
            attrPath = new ArrayList<Attribute<?, ?>>();
        }

        String[] attributeParts = attributePath.split("\\.");
        ManagedType<?> currentType = type;
        Class<?> currentClass = type.getJavaType();

        boolean joinableAllowed = true;
        for (int i = 0; i < attributeParts.length; i++) {
            Attribute<?, ?> attr = null;
            if (currentType == null) {
                // dereference basic
                break;
            }
            attr = com.blazebit.persistence.parser.util.JpaMetamodelUtils.getAttribute(currentType, attributeParts[i]);
            if (attr == null) {
                attrPath.clear();
                break;
            }

            currentClass = com.blazebit.persistence.parser.util.JpaMetamodelUtils.resolveFieldClass(currentClass, attr);
            if (attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED) {
                currentType = metamodel.embeddable(currentClass);
            } else if (attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.BASIC) {
                currentType = null;
            } else if (isJoinable(attr) && joinableAllowed) {
                joinableAllowed = false;
                if (i + 1 < attributeParts.length) {
                    currentType = metamodel.entity(currentClass);
                    // look ahead
                    Attribute<?, ?> nextAttr = com.blazebit.persistence.parser.util.JpaMetamodelUtils.getAttribute(currentType, attributeParts[i + 1]);
                    if (!com.blazebit.persistence.parser.util.JpaMetamodelUtils.getSingleIdAttribute((EntityType<?>) currentType).getName().equals(nextAttr.getName())) {
                        throw new IllegalArgumentException("Path joining not allowed in returning expression: " + attributePath);
                    }
                }
            } else {
                throw new IllegalArgumentException("Path joining not allowed in returning expression: " + attributePath);
            }

            attrPath.add(attr);
        }

        if (attrPath.isEmpty()) {
            throw new IllegalArgumentException("Path " + attributePath + " does not exist on entity " + com.blazebit.persistence.parser.util.JpaMetamodelUtils.getTypeName(type));
        }

        return new AttributePath(attrPath, currentClass);
    }

    @Override
    public AttributePath getJoinTableCollectionAttributePath(Metamodel metamodel, EntityType<?> type, String attributePath, String collectionName) {
        String trimmedPath = attributePath.trim();
        String indexStart = "index(";
        String keyStart = "key(";
        int collectionArgumentStart;
        Attribute<?, ?> collectionFunction;
        if (trimmedPath.regionMatches(true, 0, indexStart, 0, indexStart.length())) {
            collectionArgumentStart = indexStart.length();
            collectionFunction = new ListIndexAttribute<>(type.getList(collectionName));
        } else if (trimmedPath.regionMatches(true, 0, keyStart, 0, keyStart.length())) {
            collectionArgumentStart = keyStart.length();
            collectionFunction = new MapKeyAttribute<>(type.getMap(collectionName));
        } else {
            int dotIndex = trimmedPath.indexOf('.');
            if (!trimmedPath.equals(collectionName) && (dotIndex == -1 || !trimmedPath.substring(0, dotIndex).equals(collectionName))) {
                SingularAttribute<?, ?> idAttribute = com.blazebit.persistence.parser.util.JpaMetamodelUtils.getSingleIdAttribute(type);
                if (!idAttribute.getName().equals(attributePath)) {
                    throw new IllegalArgumentException("Only access to the owner type's id attribute '" + idAttribute.getName() + "' is allowed. Invalid access to different attribute through the expression: " + attributePath);
                }
                return new AttributePath(new ArrayList<Attribute<?, ?>>(Collections.singletonList(idAttribute)), com.blazebit.persistence.parser.util.JpaMetamodelUtils.resolveFieldClass(type.getJavaType(), idAttribute));
            }

            Attribute<?, ?> collectionAttribute = com.blazebit.persistence.parser.util.JpaMetamodelUtils.getAttribute(type, collectionName);
            Class<?> targetClass = com.blazebit.persistence.parser.util.JpaMetamodelUtils.resolveFieldClass(type.getJavaType(), collectionAttribute);
            if (dotIndex == -1) {
                return new AttributePath(new ArrayList<Attribute<?, ?>>(Collections.singletonList(collectionAttribute)), com.blazebit.persistence.parser.util.JpaMetamodelUtils.resolveFieldClass(targetClass, collectionAttribute));
            }

            String collectionElementAttributeName = trimmedPath.substring(dotIndex + 1);
            ManagedType<?> targetManagedType = metamodel.managedType(targetClass);
            if (targetManagedType instanceof EntityType<?>) {
                EntityType<?> targetEntityType = (EntityType<?>) targetManagedType;
                SingularAttribute<?, ?> idAttribute = com.blazebit.persistence.parser.util.JpaMetamodelUtils.getSingleIdAttribute(targetEntityType);
                String actualIdAttributeName = idAttribute.getName();
                if (!actualIdAttributeName.equals(collectionElementAttributeName)) {
                    throw new IllegalArgumentException("Only access to the target element type's id attribute '" + actualIdAttributeName + "' is allowed. Invalid access to different attribute through the expression: " + attributePath);
                }
                return new AttributePath(new ArrayList<>(Arrays.asList(collectionAttribute, idAttribute)), com.blazebit.persistence.parser.util.JpaMetamodelUtils.resolveFieldClass(targetClass, idAttribute));
            } else {
                Attribute<?, ?> attribute = null;
                Throwable cause = null;
                try {
                    attribute = targetManagedType.getAttribute(collectionElementAttributeName);
                } catch (IllegalArgumentException ex) {
                    cause = ex;
                }
                if (attribute == null) {
                    throw new IllegalArgumentException("Couldn't find attribute '" + collectionElementAttributeName + "' on managed type '" + targetClass.getName() + "'. Invalid access through the expression: " + attributePath, cause);
                }
                return new AttributePath(new ArrayList<>(Arrays.asList(collectionAttribute, attribute)), com.blazebit.persistence.parser.util.JpaMetamodelUtils.resolveFieldClass(targetClass, attribute));
            }
        }

        // assume the last character is the closing parenthesis
        String collectionAttributeName = trimmedPath.substring(collectionArgumentStart, trimmedPath.length() - 1);
        if (!collectionAttributeName.equals(collectionName)) {
            throw new IllegalArgumentException("Collection functions are only allowed to be used with the collection '" + collectionName + "'!. Invalid use in the expression: " + attributePath);
        }

        return new AttributePath(new ArrayList<Attribute<?, ?>>(Collections.singletonList(collectionFunction)), collectionFunction.getJavaType());
    }

    @Override
    public boolean isJoinable(Attribute<?, ?> attr) {
        return attr.isCollection()
            || attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_ONE
            || attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_ONE;
    }

    @Override
    public boolean isCompositeNode(Attribute<?, ?> attr) {
        if (attr.isCollection()) {
            PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) attr;
            if (pluralAttribute.getElementType().getPersistenceType() == Type.PersistenceType.BASIC) {
                return false;
            }
            return true;
        }

        return attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_ONE
                || attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_ONE;
    }
}
